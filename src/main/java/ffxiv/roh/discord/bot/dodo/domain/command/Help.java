package ffxiv.roh.discord.bot.dodo.domain.command;


import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class Help {
    private final DiscordProperties discordProperties;
    private final List<Command> commands;

    public void execute(MessageReceivedEvent event, String... options) {
        if (options.length == 0) {
            showAllCommandHelp(event);
        } else {
            String commandName = options[0];
            Optional<Command> command = commands.stream()
                    .filter(c -> c.getCommand().equals(commandName))
                    .findFirst();
            command.ifPresentOrElse(c -> showCommandHelp(event, c), () -> showAllCommandHelp(event));
        }
    }

    private void showAllCommandHelp(MessageReceivedEvent event) {
        // 全コマンドの Description を組み合わせて返信する。
        StringBuilder sb = new StringBuilder();
        sb.append("""
                実行コマンドを説明します。
                =================================
                """);
        for (Command command : commands) {
            sb.append("- %s: %s\n".formatted(command.getCommand(), command.getDescription()));
        }
        sb.append("""
                =================================
                各コマンドの更に詳細な説明が必要な場合は `${prefix} help <コマンド名>` (例: `${prefix} help party`) を実行してください。
                """.replace("${prefix}", discordProperties.getCommandPrefix())
        );
        event.getMessage().reply(sb.toString()).queue();
    }

    private void showCommandHelp(MessageReceivedEvent event, Command command) {
        String helpMessage = """
                ### 概要
                %s
                ### コマンド説明
                %s
                ### 実行例
                %s
                """.formatted(command.getDescription(), command.getUsage(), command.getExample());
        event.getMessage().reply(helpMessage).queue();
    }
}
