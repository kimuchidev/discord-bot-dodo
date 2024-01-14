package ffxiv.roh.discord.bot.dodo.domain.party;


import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.domain.CommandListener;
import ffxiv.roh.discord.bot.dodo.domain.CommandUtils;
import ffxiv.roh.discord.bot.dodo.domain.entity.*;
import ffxiv.roh.discord.bot.dodo.domain.exception.CantParseRoleException;
import ffxiv.roh.discord.bot.dodo.domain.exception.StopProcessException;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static ffxiv.roh.discord.bot.dodo.domain.entity.ConfigKey.DEFAULT_PARTY_CHANNEL_NAME;

@Slf4j
@Service
public class PartyCommandListener extends CommandListener {
    private final PartyRepository partyRepository;
    private final ConfigRepository configRepository;

    public PartyCommandListener(DiscordProperties discordProperties, PartyRepository partyRepository, ConfigRepository configRepository) {
        super(discordProperties);
        this.partyRepository = partyRepository;
        this.configRepository = configRepository;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        switch (subcommand) {
            case "new" -> requestNewParty(event);
            case "edit" -> requestEditParty(event);
            case "delete" -> deleteParty(event);
            case null, default -> log.error("Unknown subcommand: {}", subcommand);
        }
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        try {
            String modalId = event.getModalId();
            switch (modalId) {
                case "newPartyForm" -> newParty(event);
                case "editPartyForm" -> editParty(event);
                default -> log.error("Unknown modalId: {}", modalId);
            }
        } catch (StopProcessException e) {
            log.error("StopProcessException: {}", e.getMessage());
        }
    }

    void requestNewParty(SlashCommandInteractionEvent event) {
        List<ActionComponent> inputs = new ArrayList<>();

        inputs.add(TextInput.create("content", "コンテンツ", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("例：天獄零式４層")
                .setRequiredRange(1, 100)
                .build());

        inputs.add(TextInput.create("comment", "募集メッセージ", TextInputStyle.PARAGRAPH)
                .setRequired(false)
                .setMaxLength(1000)
                .build());

        inputs.add(TextInput.create("datetime", "開始時間（yyyy/MM/dd hh:mm）", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("例：202401011030 or 2024/01/01 10:30")
                .setRequiredRange(12, 16)
                .build());

        inputs.add(TextInput.create("role", "募集ロール(コードと人数をロール別にカンマ区切りで指定)", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("例：MT,ST,PH,BH,MD,RD1,CD or T2,H2,D4 or F8")
                .setRequiredRange(1, 100)
                .build());

        Modal modal = Modal.create("newPartyForm", "パーティー募集")
                .addComponents(inputs.stream().map(ActionRow::of).toList())
                .build();

        event.replyModal(modal).queue();
    }

    void newParty(ModalInteractionEvent event) throws StopProcessException {
        // フォームの入力値を取得する
        String content = CommandUtils.getInputString(event, "content");
        LocalDateTime datetime = CommandUtils.getInputDateTime(event, "datetime");
        String rolesString = CommandUtils.getInputString(event, "role");
        Roles roles;
        try {
            roles = Roles.parse(rolesString);
        } catch (CantParseRoleException e) {
            event.reply("指定の Role を正しく解析できませんでした。入力値：%s".formatted(e.getRoleString())).setEphemeral(true).queue();
            return;
        }
        String comment = CommandUtils.getInputString(event, "comment");

        String requestId = event.getId();
        String nickname = CommandUtils.getUsername(event);
        Party party = Party.newInstance(requestId, content, nickname, datetime, roles, comment);
        partyRepository.save(party);

        Config config = configRepository.findById(ConfigKey.partyChannelName.name()).orElseGet(() -> new Config(ConfigKey.partyChannelName.name(), DEFAULT_PARTY_CHANNEL_NAME));
        Objects.requireNonNull(event.getGuild())
                .getTextChannelsByName(config.getValue(), true)
                .stream().findFirst()
                .ifPresentOrElse(channel -> channel.sendMessageEmbeds(
                                List.of(party.toEmbed(event.getGuild()))).queue((c) ->
                                event.reply("パーティー募集を作成しました。").setEphemeral(true).queue()
                        ),
                        () -> event.reply("パーティー募集用チャンネルが見つかりませんでした。管理者に連絡してください。")
                                .setEphemeral(true).queue()
                );
    }

    void requestEditParty(SlashCommandInteractionEvent event) {
        String requestId = CommandUtils.getOptionString(event, "party-id");

        Optional<Party> optionalParty = partyRepository.findById(requestId);
        if (optionalParty.isEmpty()) {
            event.reply("指定の募集掲示板のID(%s)が記憶に存在しません。".formatted(requestId)).setEphemeral(true).queue();
            return;
        }
        Party party = optionalParty.get();

        List<ActionComponent> inputs = new ArrayList<>();

        inputs.add(TextInput.create("party-id", "募集掲示板のID", TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(1, 100)
                .setValue(requestId)
                .build());

        inputs.add(TextInput.create("content", "コンテンツ", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("例：天獄零式４層")
                .setRequiredRange(1, 100)
                .setValue(party.getContent())
                .build());

        inputs.add(TextInput.create("comment", "募集メッセージ", TextInputStyle.PARAGRAPH)
                .setRequired(false)
                .setMaxLength(1000)
                .setValue(party.getComment())
                .build());

        inputs.add(TextInput.create("datetime", "開始時間（yyyy/MM/dd hh:mm）", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("例：202401011030 or 2024/01/01 10:30")
                .setRequiredRange(12, 16)
                .setValue(party.getDatetime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm")))
                .build());

        inputs.add(TextInput.create("role", "募集ロール(コードと人数をロール別にカンマ区切りで指定)", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("例：MT,ST,PH,BH,MD,RD1,CD or T2,H2,D4 or F8")
                .setRequiredRange(1, 100)
                .setValue(party.getRecruitRoles().toInputValue())
                .build());

        Modal modal = Modal.create("editPartyForm", "パーティー募集(編集)")
                .addComponents(inputs.stream().map(ActionRow::of).toList())
                .build();

        event.replyModal(modal).queue();
    }

    void editParty(ModalInteractionEvent event) throws StopProcessException {
        // フォームの入力値を取得する
        String requestId = CommandUtils.getInputString(event, "party-id");
        String content = CommandUtils.getInputString(event, "content");
        LocalDateTime datetime = CommandUtils.getInputDateTime(event, "datetime");
        String rolesString = CommandUtils.getInputString(event, "role");
        Roles roles;
        try {
            roles = Roles.parse(rolesString);
        } catch (CantParseRoleException e) {
            event.reply("指定の Role を正しく解析できませんでした。入力値：%s".formatted(e.getRoleString())).setEphemeral(true).queue();
            return;
        }
        String comment = CommandUtils.getInputString(event, "comment");

        // 指定のパーティー募集がない場合終了
        if (!partyRepository.existsById(requestId)) {
            event.reply("指定の募集掲示板のID(%s)が記憶に存在しません。".formatted(requestId)).setEphemeral(true).queue();
            return;
        }

        Optional<Party> optionalParty = partyRepository.findById(requestId);
        if (optionalParty.isEmpty()) {
            event.reply("指定の募集掲示板のID(%s)が記憶に存在しません。".formatted(requestId)).setEphemeral(true).queue();
            return;
        }
        Party party = optionalParty.get();
        party.setContent(content);
        party.setDatetime(datetime);
        boolean roleCategoryChanged = !party.getRecruitRoles().getSortedRoleSet().equals(roles.getSortedRoleSet());
        party.setRecruitRoles(roles);
        party.setComment(comment);
        partyRepository.save(party);

        Config config = configRepository.findById(ConfigKey.partyChannelName.name()).orElseGet(() -> new Config(ConfigKey.partyChannelName.name(), DEFAULT_PARTY_CHANNEL_NAME));
        Objects.requireNonNull(event.getGuild())
                .getTextChannelsByName(config.getValue(), true)
                .stream().findFirst()
                .ifPresentOrElse(channel -> channel.retrieveMessageById(party.getBordMessageId())
                                .queue(message -> {
                                    message.editMessageEmbeds(List.of(party.toEmbed(event.getGuild()))).queue((c) ->
                                            event.reply("パーティー募集を更新しました。").setEphemeral(true).queue()
                                    );
                                    if (roleCategoryChanged) {
                                        message.clearReactions().queue();
                                        party.getRecruitRoles().getSortedRoleSet()
                                                .stream().map(role -> role.getEmoji(event.getGuild()))
                                                .forEach(emoji -> message.addReaction(emoji).queue());
                                        var thread = message.getStartedThread();
                                        if (thread != null) {
                                            thread.getManager().setArchived(false).queue();
                                            thread.sendMessage("@everyone 募集ロールが変更されたため、リアクションをリセットしました。お手数ですが、リアクションを再度クリックしてください。").queue();
                                        }
                                    }
                                }),
                        () -> event.reply("パーティー募集用チャンネルが見つかりませんでした。管理者に連絡してください。")
                                .setEphemeral(true).queue()
                );
    }

    void deleteParty(SlashCommandInteractionEvent event) {
        String requestId = CommandUtils.getOptionString(event, "party-id");
        Optional<Party> optionalParty = partyRepository.findById(requestId);
        if (optionalParty.isEmpty()) {
            event.reply("指定の募集掲示板のID(%s)が記憶に存在しません。".formatted(requestId)).setEphemeral(true).queue();
            return;
        }
        Party party = optionalParty.get();

        partyRepository.deleteById(requestId);

        Config config = configRepository.findById(ConfigKey.partyChannelName.name()).orElseGet(() -> new Config(ConfigKey.partyChannelName.name(), DEFAULT_PARTY_CHANNEL_NAME));
        Objects.requireNonNull(event.getGuild())
                .getTextChannelsByName(config.getValue(), true)
                .stream().findFirst()
                .ifPresentOrElse(channel -> channel.retrieveMessageById(party.getBordMessageId())
                                .queue(message -> {
                                    var thread = message.getStartedThread();
                                    if (thread != null)
                                        thread.getManager().setArchived(true).queue();
                                    message.delete().queue((c) ->
                                            event.reply("パーティー募集を削除しました。").setEphemeral(true).queue());
                                }),
                        () -> event.reply("パーティー募集用チャンネルが見つかりませんでした。管理者に連絡してください。")
                                .setEphemeral(true).queue()
                );
    }

    @Override
    public String getCommandCode() {
        return "party";
    }

    @Override
    public String getDescription() {
        return "募集掲示用メッセージを作成します。";
    }

    @Override
    public SubcommandGroupData getCommandData() {
        return new SubcommandGroupData("party", "パーティー募集機能")
                .addSubcommands(
                        new SubcommandData("new", "パーティー募集の掲示板を作成する"),
                        new SubcommandData("edit", "パーティー募集の掲示板を編集する")
                                .addOption(OptionType.STRING, "party-id", "募集掲示板のID", true, true),
                        new SubcommandData("delete", "パーティー募集の掲示板を削除する")
                                .addOption(OptionType.STRING, "party-id", "募集掲示板のID", true, true)
                );
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals(discordProperties.getCommandPrefix())
                && event.getFocusedOption().getName().equals("party-id")) {
            List<Command.Choice> options = StreamSupport.stream(partyRepository.findAll().spliterator(), false)
                    .filter(party -> party.getRequestId().startsWith(event.getFocusedOption().getValue()))
                    .map(party -> new Command.Choice(
                            "%s-%s(%s)".formatted(party.getContent(), party.getAuthorName(), party.getRequestId()),
                            party.getRequestId()))
                    .toList();
            event.replyChoices(options).queue();
        }
    }
}
