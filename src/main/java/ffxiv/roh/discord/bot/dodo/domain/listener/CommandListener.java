package ffxiv.roh.discord.bot.dodo.domain.listener;

import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.domain.command.Command;
import ffxiv.roh.discord.bot.dodo.domain.command.Help;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * コマンド処理をコントロールするリスナー
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Scope("singleton")
public class CommandListener extends MessageListener {
    private static final String SPLIT_REGEX = " ";

    private final DiscordProperties discordProperties;
    private final List<Command> commands;
    private final Help help;

    @Override
    protected void processMessage(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        String[] args = content.split(SPLIT_REGEX);
        String prefix = args[0];

        if (!discordProperties.getCommandPrefix().equals(prefix)) {
            return;
        }

        try {
            String command = args[1].toLowerCase();
            String[] options = new String[args.length - 2];
            System.arraycopy(args, 2, options, 0, args.length - 2);
            log.info("command: {}, options: {}", command, options);

            // コマンド実行
            commands.stream()
                    .filter(c -> c.getCommand().equals(command))
                    .findFirst()
                    .ifPresentOrElse(c -> c.execute(event, options), () -> help.execute(event, options));
        } catch (Exception e) {
            log.info("Not found command: {}", content);
            help.execute(event);
        }
    }

    @Override
    protected boolean isTarget(MessageReceivedEvent event) {
        // bot 通知は対象外
        if (event.getAuthor().isBot()) {
            return false;
        }

        // コマンド判定用 prefix 始まりの場合対象とする
        if (event.getMessage().getContentRaw().startsWith(discordProperties.getCommandPrefix())) {
            return true;
        }

        return false;
    }
}
