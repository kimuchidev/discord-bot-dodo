package ffxiv.roh.discord.bot.dodo.domain;


import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.domain.exception.StopProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

@Slf4j
@RequiredArgsConstructor
public abstract class CommandListener extends BotListener {
    protected final DiscordProperties discordProperties;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(discordProperties.getCommandPrefix())
                && event.getSubcommandGroup() != null
                && event.getSubcommandGroup().equals(getCommandCode())) {
            try {
                execute(event);
            } catch (StopProcessException ignored) {
                log.error("コマンド処理が何らかの理由により中止されました。コマンド：{}", event.getFullCommandName());
            }
        }
    }

    public abstract void execute(SlashCommandInteractionEvent event) throws StopProcessException;

    public abstract String getCommandCode();

    public abstract String getDescription();

    public abstract SubcommandGroupData getCommandData();
}
