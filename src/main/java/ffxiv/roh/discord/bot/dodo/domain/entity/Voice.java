package ffxiv.roh.discord.bot.dodo.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Voice {
    boy1(Type.AZURE, "ja-JP-KeitaNeural", "男性-Keita"),
    boy2(Type.AZURE, "ja-JP-DaichiNeural", "男性-Daichi"),
    boy3(Type.AZURE, "ja-JP-NaokiNeural", "男性-Naoki"),
    girl1(Type.AZURE, "ja-JP-NanamiNeural", "女性-Nanami"),
    girl2(Type.AZURE, "ja-JP-AoiNeural", "女性-Aoi"),
    girl3(Type.AZURE, "ja-JP-MayuNeural", "女性-Mayu"),
    girl4(Type.AZURE, "ja-JP-ShioriNeural", "女性-Shiori"),
    ZUNDAMON_AMAAMA(Type.VOICE_VOX, "1", "ずんだもん-あまあま"),
    ZUNDAMON_SASAYAKI(Type.VOICE_VOX, "22", "ずんだもん-ささやき"),
    ZUNDAMON_HISOHISO(Type.VOICE_VOX, "38", "ずんだもん-ひそひそ"),
    ZUNDAMON_HELOHELO(Type.VOICE_VOX, "75", "ずんだもん-ヘロヘロ"),
    ZUNDAMON_NAMIDAME(Type.VOICE_VOX, "76", "ずんだもん-なみだめ");

    private final Type type;
    private final String voiceName;
    private final String voiceNameJp;

    public enum Type {
        AZURE,

        // https://github.com/ts-klassen/ttsQuestV3Voicevox
        VOICE_VOX
    }
}
