package ffxiv.roh.discord.bot.dodo.domain.read;

import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.config.TextReadProperties;
import ffxiv.roh.discord.bot.dodo.domain.BotListener;
import ffxiv.roh.discord.bot.dodo.domain.entity.User;
import ffxiv.roh.discord.bot.dodo.domain.entity.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * テキスト読み上げ処理用リスナー
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class TextReadListener extends BotListener {
    private final TextReadProperties textReadProperties;
    private final DiscordProperties discordProperties;
    private final TextReadService textReadService;
    private final UserRepository userRepository;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!isTarget(event)) {
            return;
        }

        String id = event.getAuthor().getId();
        String name = event.getAuthor().getEffectiveName();

        User user = userRepository.findOrCreate(id, name);

        log.debug("TextReadListener.processMessage:{}", event.getMessage().getContentRaw());
        textReadService.read(user, event.getMessage().getContentRaw());
    }

    protected boolean isTarget(MessageReceivedEvent event) {
        // bot 通知は対象外
        if (event.getAuthor().isBot()) {
            return false;
        }

        // 読み上げ対象チャンネルの場合 OK
        if (textReadProperties.getReadTargetChanelId().equals(event.getChannel().getId())) {
            return true;
        }

        return false;
    }
}
