package ffxiv.roh.discord.bot.dodo.domain.command;


import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.config.TextReadProperties;
import ffxiv.roh.discord.bot.dodo.domain.exception.NoSuchCommandOptionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class Read extends Command {
    private final DiscordProperties discordProperties;
    private final TextReadProperties textReadProperties;

    @Override
    public void execute(MessageReceivedEvent event, String... options) {
        if (options.length == 0 || options[0].startsWith("s")) {
            textReadProperties.readTargetChanelIds().add(event.getChannel().getId());
            event.getMessage().reply("<#%s> のメッセージ読み上げを開始します。".formatted(event.getChannel().getId())).queue();
        } else if (options[0].startsWith("b")) {
            textReadProperties.readTargetChanelIds().remove(event.getChannel().getId());
            event.getMessage().reply("<#%s> のメッセージ読み上げを終了します。".formatted(event.getChannel().getId())).queue();
        } else {
            throw new NoSuchCommandOptionException();
        }
    }

    @Override
    public String getCommand() {
        return "read";
    }

    @Override
    public String getDescription() {
        return "コマンドを実行したチャンネルのメッセージを読み上げます。";
    }

    @Override
    public String getUsage() {
        return """
                - ${command}: 読み上げ開始。
                - ${command} s[tart]: 読み上げ開始。
                - ${command} b[ye]: 読み上げ終了。"""
                .replace("${command}", getCommand());
    }

    @Override
    public String getExample() {
        return """
                - `${prefix} ${command} s`: 読み上げ開始。
                - `${prefix} ${command} bye`: 読み上げ終了。"""
                .replace("${prefix}", discordProperties.getCommandPrefix())
                .replace("${command}", getCommand());
    }

}
