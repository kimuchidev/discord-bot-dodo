package ffxiv.roh.discord.bot.dodo.domain.agent;

import ffxiv.roh.discord.bot.dodo.domain.CommandUtils;
import ffxiv.roh.discord.bot.dodo.domain.MessageCommandListener;
import ffxiv.roh.discord.bot.dodo.domain.exception.StopProcessException;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReplyAgentCommandListener extends MessageCommandListener {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
    }

    @Override
    public void execute(MessageContextInteractionEvent event) throws StopProcessException {
        requestInputMessage(event);
    }

    void requestInputMessage(MessageContextInteractionEvent event) {
        List<ActionComponent> inputs = new ArrayList<>();

        inputs.add(TextInput.create("targetMessageId", "返信対象メッセージid", TextInputStyle.SHORT)
                .setRequired(true)
                .setMaxLength(100)
                .setValue(event.getTarget().getId())
                .build());
        inputs.add(TextInput.create("message", "伝言内容", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMaxLength(1000)
                .build());

        Modal modal = Modal.create("replyAgentMessageForm", getCommandCode())
                .addComponents(inputs.stream().map(ActionRow::of).toList())
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals("replyAgentMessageForm")) {
            String targetMessageId = CommandUtils.getInputString(event, "targetMessageId");
            String inputMessage = CommandUtils.getInputString(event, "message");

            event.getChannel().getHistory()
                    // とりあえず取れる最大件数の100件を取得する
                    .retrievePast(100).queue(
                            messages -> messages.stream()
                                    .filter(m -> m.getId().equals(targetMessageId))
                                    .findFirst()
                                    .ifPresentOrElse(m -> m.reply(inputMessage).queue(
                                                    e -> event.reply("伝言完了").setEphemeral(true).queue(),
                                                    e -> event.reply("伝言失敗").setEphemeral(true).queue()
                                            ), () -> event.reply("伝言対象メッセージが見つかりません。").setEphemeral(true).queue()
                                    )
                    );
        }
    }

    @Override
    public String getCommandCode() {
        return "Dodo に伝言依頼";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.message(getCommandCode());
    }
}
