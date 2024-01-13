package ffxiv.roh.discord.bot.dodo.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ConfigKey {
    readName,
    partyChannelName;

    public static final String DEFAULT_PARTY_CHANNEL_NAME = "コンテンツ募集";
}
