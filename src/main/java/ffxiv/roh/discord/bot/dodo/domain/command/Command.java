package ffxiv.roh.discord.bot.dodo.domain.command;


import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
    public abstract void execute(MessageReceivedEvent event, String... options);

    public abstract String getCommand();

    public abstract String getDescription();

    public abstract String getUsage();

    public abstract String getExample();
}
