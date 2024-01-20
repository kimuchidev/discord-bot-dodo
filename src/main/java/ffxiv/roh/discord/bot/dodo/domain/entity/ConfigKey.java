package ffxiv.roh.discord.bot.dodo.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ConfigKey {
    readName,
    partyChannelName,
    autoCleanMessageDelay,
    cheerChannelName,
    doCheer;

    public static final String DEFAULT_PARTY_CHANNEL_NAME = "コンテンツ募集";
    public static final String DEFAULT_AUTO_CLEAN_MESSAGE_DELAY = "1";
    public static final String DEFAULT_CHEER_CHANNEL_NAME = "ガチダイエットch";
}
