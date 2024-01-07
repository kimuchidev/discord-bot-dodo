package ffxiv.roh.discord.bot.dodo.domain.listener;

import ffxiv.roh.discord.bot.dodo.config.TextReadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * テキスト読み上げ処理用リスナー
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Scope("singleton")
public class TextReadListener extends MessageListener {
    private final TextReadProperties textReadProperties;

    @Override
    protected void processMessage(MessageReceivedEvent event) {
    }

    @Override
    protected boolean isTarget(MessageReceivedEvent event) {
        // bot 通知は対象外
        if (event.getAuthor().isBot()) {
            return false;
        }

        // コマンドメッセージは対象外
        if (event.getMessage().getContentRaw().startsWith("!")) {
            return false;
        }

        // 読み上げ対象チャンネル以外は対象外
        if (!textReadProperties.readTargetChanelIds().contains(event.getChannel().getId())) {
            return true;
        }

        return false;
    }
}
