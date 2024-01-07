package ffxiv.roh.discord.bot.dodo.domain.listener;

import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.config.TextReadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

/**
 * テキスト読み上げ処理用リスナー
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class TextReadListener extends MessageListener {
    private final TextReadProperties textReadProperties;
    private final DiscordProperties discordProperties;

    @Override
    protected void processMessage(MessageReceivedEvent event) {
        log.debug("TextReadListener.processMessage:{}", event.getMessage().getContentRaw());
    }

    @Override
    protected boolean isTarget(MessageReceivedEvent event) {
        // bot 通知は対象外
        if (event.getAuthor().isBot()) {
            return false;
        }

        // コマンドメッセージは対象外
        if (event.getMessage().getContentRaw().startsWith(discordProperties.getCommandPrefix())) {
            return false;
        }

        // 読み上げ対象チャンネルの場合 OK
        if (textReadProperties.readTargetChanelIds().contains(event.getChannel().getId())) {
            return true;
        }

        return false;
    }
}
