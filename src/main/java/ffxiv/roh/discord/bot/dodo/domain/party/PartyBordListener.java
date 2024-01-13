package ffxiv.roh.discord.bot.dodo.domain.party;

import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.domain.BotListener;
import ffxiv.roh.discord.bot.dodo.domain.entity.Party;
import ffxiv.roh.discord.bot.dodo.domain.entity.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

import static ffxiv.roh.discord.bot.dodo.domain.entity.Party.PARTY_ID_FIELD_NAME;


@RequiredArgsConstructor
@Slf4j
@Service
public class PartyBordListener extends BotListener {
    private final DiscordProperties discordProperties;
    private final PartyRepository partyRepository;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!isTarget(event)) {
            return;
        }
        MessageEmbed embed = event.getMessage().getEmbeds().getFirst();

        // 募集掲示板のメッセージIDをDBに保存する。
        String partyRequestId = embed.getFields().stream().filter(
                field -> PARTY_ID_FIELD_NAME.equals(field.getName())
        ).findFirst().orElseThrow().getValue();
        assert partyRequestId != null;
        Party party = partyRepository.findById(partyRequestId).orElseThrow();
        party.setBordMessageId(event.getMessage().getId());
        partyRepository.save(party);

        // 応募用リアクションを付与する。
        party.getRecruitRoles().getSortedRoleSet()
                .stream().map(role -> role.getEmoji(event.getGuild()))
                .forEach(emoji -> event.getMessage().addReaction(emoji).queue());

        // メッセージにスレッドを作成する。
        event.getMessage().createThreadChannel("%s-%s".formatted(party.getContent(), party.getAuthorName())).queue();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!isTarget(event)) {
            return;
        }
        rebuildBord(event);
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!isTarget(event)) {
            return;
        }
        rebuildBord(event);
    }

    void rebuildBord(GenericMessageReactionEvent event) {
        List<Party> parties = partyRepository.findByBordMessageId(event.getMessageId());
        if (parties.size() != 1) {
            log.error("Parties size is not 1.");
            return;
        }

        Party party = parties.getFirst();
        User user = event.getUser();
        if (user == null) {
            user = event.retrieveUser().complete();
        }
        assert user != null;

        Message message = event.retrieveMessage().complete();
        List<MessageReaction> reactions = message.getReactions();

        party.clearEntry();
        for (var reaction : reactions) {
            Role reactionRole = Role.emojiOf(reaction.getEmoji().getName());
            if (reactionRole == null || (reaction.hasCount() && reaction.getCount() == 1)) {
                // ロール絵文字以外は無視する。
                // リアクションの数が 1 の場合は bot リアクションなので、無視する。
                continue;
            }
            // TODO users の取得リアクション毎に順番に取っているのでレスポンスが悪い。できれば全リアクションに対してパラレール取得するように変えたい。
            List<User> users = reaction.retrieveUsers().complete();
            users.stream()
                    .filter(u -> !u.isBot())
                    .forEach(u -> party.entry(u.getId(), u.getEffectiveName(), reactionRole));
        }

        partyRepository.save(party);

        message.editMessageEmbeds(List.of(party.toEmbed(event.getGuild()))).queue();
        var thread = message.getStartedThread();
        if (thread != null) {
            thread.getManager().setArchived(false).queue();
            thread.addThreadMember(user).queue();
        }

    }

    boolean isTarget(MessageReceivedEvent event) {
        // 非 bot 通知は対象外
        if (!event.getAuthor().isBot()) {
            return false;
        }

        // メッセージに募集文言が付いている場合のみ対象
        if (!event.getMessage().getEmbeds().isEmpty()
                && event.getMessage().getEmbeds().stream().anyMatch(embed ->
                embed.getTitle() != null && embed.getTitle().startsWith(Party.TITLE_PREFIX)
        )) {
            return true;
        }

        return false;
    }

    boolean isTarget(GenericMessageReactionEvent event) {
        // bot 通知は対象外
        User user = event.getUser();
        if (user == null) {
            user = event.retrieveUser().complete();
        }
        assert user != null;
        if (user.isBot()) {
            return false;
        }

        // リアクションしたメッセージが募集掲示板の場合、対象とする
        if (partyRepository.existsByBordMessageId(event.getMessageId())) {
            return true;
        }

        return false;
    }
}
