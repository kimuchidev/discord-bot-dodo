package ffxiv.roh.discord.bot.dodo.domain.entity;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import ffxiv.roh.discord.bot.dodo.domain.party.Role;
import ffxiv.roh.discord.bot.dodo.domain.party.RolePair;
import ffxiv.roh.discord.bot.dodo.domain.party.Roles;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@Data
@Entity(name = "parties")
public class Party {
    public static final String TITLE_PREFIX = "☆募集:";
    public static final String PARTY_ID_FIELD_NAME = ":lock:ID：";

    @Id
    private String requestId;
    private String content;
    private String authorName;
    private LocalDateTime datetime;
    private Roles recruitRoles;
    private String comment;
    private List<EntryInfo> entryInfoList;
    private String bordMessageId;
    private LocalDateTime deleteDatetime;

    @Transient
    private List<RolePair> recruitedRolePairs = new ArrayList<>();
    @Transient
    private List<EntryInfo> notRecruitedEntries = new ArrayList<>();

    public Party(String requestId, String content, String authorName, LocalDateTime datetime, Roles recruitRoles, String comment, List<EntryInfo> entryInfoList, String bordMessageId) {
        this.requestId = requestId;
        this.content = content;
        this.authorName = authorName;
        this.recruitRoles = recruitRoles;
        this.comment = comment;
        this.entryInfoList = entryInfoList;
        this.bordMessageId = bordMessageId;
        setDatetime(datetime);
    }

    public static Party newInstance(
            String requestId,
            String content,
            String authorName,
            LocalDateTime datetime,
            Roles recruitRoles,
            String comment) {
        return new Party(
                requestId,
                content,
                authorName,
                datetime,
                recruitRoles,
                comment,
                List.of(),
                null
        );
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
        this.deleteDatetime = datetime.plusDays(1);
    }

    public MessageEmbed toEmbed(Guild guild) {
        doAutoComposition();
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        var builder = new EmbedBuilder()
                .setTitle(TITLE_PREFIX + content)
                .setColor(0x0099ff)
                .setAuthor(authorName)
                .setDescription(comment)
                .addField(new MessageEmbed.Field(PARTY_ID_FIELD_NAME, requestId, false))
                .addField(new MessageEmbed.Field(":clock10:開始日時：", datetime.format(dateTimeFormatter), false))
                .addField(new MessageEmbed.Field(":crossed_swords:コンテンツ：", content, false))
                .addField(new MessageEmbed.Field(":star2:募集人数", "<%s>".formatted(recruitRoles.getEmojis(guild)), false))
                .addField(new MessageEmbed.Field(":dizzy:自動編成結果", recruitedResult(guild), false))
                .addField(new MessageEmbed.Field(":hourglass:候補メンバー", waitingMemberResult(), false))
                .setFooter("""
                        出せるロール（複数可）にリアクションしてください。
                        自動削除予定：%s
                        """.formatted(deleteDatetime.format(dateTimeFormatter)));

        return builder.build();
    }

    /**
     * 自動編成の結果を取得する。
     */
    String recruitedResult(Guild guild) {
        var builder = new StringBuilder();
        recruitRoles.getSortedRoleSet().forEach(role ->
                builder.append("%s: %s\n".formatted(
                        role.getEmojiAsMention(guild),
                        recruitedRolePairs.stream()
                                .filter(p -> p.getRole() == role)
                                .map(RolePair::getEntryName)
                                .filter(Objects::nonNull)
                                .collect(joining("、"))
                ))
        );
        return builder.toString();
    }

    /**
     * 自動編成の結果を取得する。
     */
    String waitingMemberResult() {
        if (notRecruitedEntries.isEmpty()) {
            return "なし";
        }
        return notRecruitedEntries.stream()
                .map(EntryInfo::getName)
                .collect(joining("、"));
    }


    /**
     * 自動編成する
     */
    void doAutoComposition() {
        recruitedRolePairs = recruitRoles.getSortedRoles()
                .stream()
                .map(recruitRole -> new RolePair(recruitRole, null))
                .toList();
        notRecruitedEntries = new ArrayList<>();

        // entryInfoList の要素を順番に探索する。
        for (var entryInfo : entryInfoList) {
            boolean recruited = false;
            // entry されてない recruitRoles の要素を順番に探索する。
            List<RolePair> tempNotRecruitedRolePairs = recruitedRolePairs.stream().filter(p -> p.getEntryInfo() == null).toList();
            for (var recruitRolePair : tempNotRecruitedRolePairs) {
                // entry が recruitRole に設定可能の場合、設定する。
                if (recruitRolePair.isEntryAbleBy(entryInfo)) {
                    recruitRolePair.setEntryInfo(entryInfo);
                    recruited = true;
                    break;
                }
            }

            if (recruited) {
                continue;
            }

            // recruitRole をすべて探索しても設定できる entryRoles が存在しない場合
            // 前の recruitRole で自分が設定可能 ＆ 前の recruitRole の entry が当 recruitRole に設定可能の場合の対象を探し、設定をチェンジする。
            List<RolePair> tempRecruitedRolePairs = this.recruitedRolePairs.stream().filter(p -> p.getEntryInfo() != null).toList();
            for (RolePair tempRecruitedRolePair : tempRecruitedRolePairs) {
                if (tempRecruitedRolePair.isEntryAbleBy(entryInfo)) {
                    Optional<RolePair> nextRolePair = this.recruitedRolePairs.stream()
                            .filter(p -> p.getEntryInfo() == null)
                            .filter(p -> p.isEntryAbleBy(tempRecruitedRolePair.getEntryInfo()))
                            .findFirst();
                    if (nextRolePair.isPresent()) {
                        nextRolePair.get().setEntryInfo(tempRecruitedRolePair.getEntryInfo());
                        tempRecruitedRolePair.setEntryInfo(entryInfo);
                        recruited = true;
                        break;
                    }
                }
            }

            if (recruited) {
                continue;
            }
            notRecruitedEntries.add(entryInfo);
        }
    }

    public void clearEntry() {
        this.entryInfoList = new ArrayList<>();
    }

    public void entry(String userId, String userName, Role entryRole) {
        entryInfoList.stream()
                .filter(e -> e.getUserId().equals(userId))
                .findFirst()
                .ifPresentOrElse(
                        e -> e.getEntryRoles().add(entryRole)
                        , () -> {
                            List<Role> entryRoles = new ArrayList<>();
                            entryRoles.add(entryRole);
                            entryInfoList.add(new EntryInfo(userId, userName, entryRoles));
                        });
    }

}