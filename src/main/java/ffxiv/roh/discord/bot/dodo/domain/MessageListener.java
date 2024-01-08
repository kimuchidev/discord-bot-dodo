package ffxiv.roh.discord.bot.dodo.domain;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

/**
 * テキスト読み上げ処理用リスナー
 */
public abstract class MessageListener extends BotListener {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (isTarget(event)) {
            processMessage(event);
        }
    }

    protected abstract void processMessage(MessageReceivedEvent event);

    /**
     * 処理対象であるかを定義する
     *
     * @param event 受け取ったメッセージイベント
     * @return 対象の場合 true
     */
    protected abstract boolean isTarget(MessageReceivedEvent event);
}
