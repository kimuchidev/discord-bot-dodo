package ffxiv.roh.discord.bot.dodo.domain;


import ffxiv.roh.discord.bot.dodo.domain.exception.StopProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Slf4j
@RequiredArgsConstructor
public abstract class MessageCommandListener extends BotListener {
    
    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (event.getName().equals(getCommandCode())) {
            try {
                execute(event);
            } catch (StopProcessException ignored) {
                log.error("コマンド処理が何らかの理由により中止されました。コマンド：{}", event.getFullCommandName());
            }
        }
    }

    public abstract void execute(MessageContextInteractionEvent event) throws StopProcessException;

    public abstract String getCommandCode();

    public abstract CommandData getCommandData();
}
