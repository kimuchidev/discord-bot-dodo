package ffxiv.roh.discord.bot.dodo.domain.command;


import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class Read extends Command {
    private final DiscordProperties discordProperties;

    @Override
    public void execute(MessageReceivedEvent event, String... options) {
        //todo
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
                - s[tart]: 読み上げ開始。
                - b[ye]: 読み上げ終了。""";
    }

    @Override
    public String getExample() {
        return """
                - ${prefix} ${command} s: 読み上げ開始。
                - ${prefix} ${command} bye: 読み上げ終了。"""
                .replace("${prefix}", discordProperties.getCommandPrefix())
                .replace("${command}", getCommand());
    }

}
