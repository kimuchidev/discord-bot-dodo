package ffxiv.roh.discord.bot.dodo.domain.cheer;

import ffxiv.roh.discord.bot.dodo.config.CheerProperties;
import ffxiv.roh.discord.bot.dodo.domain.BotListener;
import ffxiv.roh.discord.bot.dodo.domain.entity.Config;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigKey;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import static ffxiv.roh.discord.bot.dodo.domain.entity.ConfigKey.DEFAULT_CHEER_CHANNEL_NAME;

@RequiredArgsConstructor
@Slf4j
@Service
public class CheerListener extends BotListener {
    private final ConfigRepository configRepository;
    private final CheerProperties cheerProperties;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!isTarget(event)) {
            return;
        }
        event.getMessage().reply(cheerProperties.getRandomMessage()).queue();
    }

    protected boolean isTarget(MessageReceivedEvent event) {
        // bot 通知は対象外
        if (event.getAuthor().isBot()) {
            return false;
        }

        // 添付画像ない場合対象外
        if (event.getMessage().getAttachments().isEmpty()) {
            return false;
        }

        Config doCheerConfig = configRepository.findById(ConfigKey.doCheer.name())
                .orElseGet(() -> new Config(ConfigKey.doCheer.name(), "true"));

        if (!doCheerConfig.getValue().equals("true")) {
            return false;
        }

        Config cheerChannelNameConfig = configRepository.findById(ConfigKey.cheerChannelName.name())
                .orElseGet(() -> new Config(ConfigKey.cheerChannelName.name(), DEFAULT_CHEER_CHANNEL_NAME));

        // チャンネルで対象外
        if (!cheerChannelNameConfig.getValue().equals(event.getMessage().getChannel().getName())) {
            return false;
        }

        return true;
    }
}
