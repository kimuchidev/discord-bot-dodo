package ffxiv.roh.discord.bot.dodo.domain.party;

import ffxiv.roh.discord.bot.dodo.domain.entity.Config;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigKey;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigRepository;
import ffxiv.roh.discord.bot.dodo.domain.entity.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static ffxiv.roh.discord.bot.dodo.domain.entity.ConfigKey.DEFAULT_AUTO_CLEAN_MESSAGE_DELAY;
import static ffxiv.roh.discord.bot.dodo.domain.entity.ConfigKey.DEFAULT_PARTY_CHANNEL_NAME;
import static ffxiv.roh.discord.bot.dodo.domain.entity.Party.PARTY_ID_FIELD_NAME;


@RequiredArgsConstructor
@Slf4j
@Service
public class PartyChannelCleaner {
    private final JDA jda;
    private final ConfigRepository configRepository;
    private final PartyRepository partyRepository;

    @Scheduled(fixedDelay = 600000)
    public void cleanPartyChannelMessage() {
        Config partyChannelNameConfig = configRepository.findById(ConfigKey.partyChannelName.name())
                .orElseGet(() -> new Config(ConfigKey.partyChannelName.name(), DEFAULT_PARTY_CHANNEL_NAME));

        Config autoCleanMessageDelayConfig = configRepository.findById(ConfigKey.autoCleanMessageDelay.name())
                .orElseGet(() -> new Config(ConfigKey.autoCleanMessageDelay.name(), DEFAULT_AUTO_CLEAN_MESSAGE_DELAY));
        int delayDay = Integer.parseInt(autoCleanMessageDelayConfig.getValue());
        if (delayDay == 0) {
            return;
        }

        jda.getGuilds().forEach(guild -> {
                    guild.getTextChannelsByName(partyChannelNameConfig.getValue(), true)
                            .stream().findFirst()
                            .ifPresent(channel -> {
                                        // とりあえず取れる最大件数の100件を取得する
                                        channel.getHistory().retrievePast(100).queue(
                                                messages -> {
                                                    var deletableUserMessages = messages.stream()
                                                            .filter(m -> isDeletableUserMessage(m, delayDay) || isDeletableBotMessage(m, delayDay))
                                                            .toList();
                                                    channel.purgeMessages(deletableUserMessages);
                                                }
                                        );
                                    }
                            );
                }
        );
    }

    boolean isDeletableBotMessage(Message message, int delayDay) {
        if (!message.getAuthor().isBot()) {
            return false;
        }

        if (message.isPinned()) {
            return false;
        }

        // メッセージ自体１日経ってなかったら削除対象外
        ZonedDateTime messageDeleteTime = message.getTimeCreated().atZoneSameInstant(ZoneId.systemDefault()).plusDays(delayDay);
        if (messageDeleteTime.isAfter(ZonedDateTime.now())) {
            return false;
        }

        var embeds = message.getEmbeds();
        if (embeds.isEmpty()) {
            return true;
        }

        // party 募集の内容から削除予定時間経過したかを判断する
        var optionalPartyRequestId = embeds.getFirst().getFields().stream().filter(
                field -> PARTY_ID_FIELD_NAME.equals(field.getName())
        ).findFirst();
        if (optionalPartyRequestId.isEmpty()) {
            return true;
        }
        var partyRequestId = optionalPartyRequestId.get().getValue();
        assert partyRequestId != null;
        var optionalParty = partyRepository.findById(partyRequestId);
        if (optionalParty.isEmpty()) {
            return true;
        }
        var party = optionalParty.get();

        // 削除時間が現時間より過去の場合、削除
        return party.getDeleteDatetime().isBefore(LocalDateTime.now());
    }

    boolean isDeletableUserMessage(Message message, int delayDay) {
        if (message.getAuthor().isBot()) {
            return false;
        }
        if (message.isPinned()) {
            return false;
        }

        ZonedDateTime messageDeleteTime = message.getTimeCreated().atZoneSameInstant(ZoneId.systemDefault()).plusDays(delayDay);
        if (messageDeleteTime.isAfter(ZonedDateTime.now())) {
            return false;
        }

        return true;
    }
}
