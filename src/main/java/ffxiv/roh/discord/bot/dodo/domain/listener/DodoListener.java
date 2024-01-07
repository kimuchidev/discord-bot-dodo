package ffxiv.roh.discord.bot.dodo.domain.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * プロジェクト内で定義したすべての Listener をまとめる親クラス
 */
@Slf4j
public abstract class DodoListener extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.debug("API is ready!");
    }
}
