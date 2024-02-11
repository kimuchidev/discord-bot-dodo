package ffxiv.roh.discord.bot.dodo.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ConfigKey {
    readName("read-name", "名前を読み上げるか"),
    partyChannelName("party-channel-name", "パーティー募集用チャンネル名"),
    autoCleanMessageDelay("auto-clean-message-delay", "パーティー募集用チャンネルのメッセージを削除するまでの待ち日数"),
    cheerChannelName("cheer-channel-name", "褒め言葉返信の対象チャンネル名"),
    doCheer("do-cheer", "褒め言葉を返信するか");

    public static final String DEFAULT_PARTY_CHANNEL_NAME = "コンテンツ募集";
    public static final String DEFAULT_AUTO_CLEAN_MESSAGE_DELAY = "1";
    public static final String DEFAULT_CHEER_CHANNEL_NAME = "ガチダイエットch";

    private final String code;
    private final String description;

    public static String getKey(String code) {
        return Arrays.stream(values()).filter(c -> c.code.equals(code)).findFirst().orElseThrow().name();
    }
}
