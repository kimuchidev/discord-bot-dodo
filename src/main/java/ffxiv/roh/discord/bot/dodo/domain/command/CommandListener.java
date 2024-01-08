package ffxiv.roh.discord.bot.dodo.domain.command;


import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.domain.BotListener;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

@RequiredArgsConstructor
public abstract class CommandListener extends BotListener {
    private final DiscordProperties discordProperties;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(discordProperties.getCommandPrefix())
                && event.getSubcommandGroup() != null
                && event.getSubcommandGroup().equals(getCommandCode())) {
            execute(event);
        }
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    public abstract String getCommandCode();

    public abstract String getDescription();

    public abstract SubcommandGroupData getCommandData();
}
