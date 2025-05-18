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
    ZUNDAMON_NORMAL(Type.VOICE_VOX, "3", "ずんだもん-ノーマル"),
    ZUNDAMON_AMAAMA(Type.VOICE_VOX, "1", "ずんだもん-あまあま"),
    ZUNDAMON_SASAYAKI(Type.VOICE_VOX, "22", "ずんだもん-ささやき"),
    ZUNDAMON_HEROHERO(Type.VOICE_VOX, "75", "ずんだもん-ヘロヘロ"),
    METAN_NORMAL(Type.VOICE_VOX, "2", "四国めたん-ノーマル"),
    METAN_AMAAMA(Type.VOICE_VOX, "0", "四国めたん-あまあま"),
    METAN_SASAYAKI(Type.VOICE_VOX, "36", "四国めたん-ささやき"),
    SORA_NORMAL(Type.VOICE_VOX, "16", "九州そら-ノーマル"),
    SORA_AMAAMA(Type.VOICE_VOX, "15", "九州そら-あまあま"),
    SORA_SASAYAKI(Type.VOICE_VOX, "19", "九州そら-ささやき"),
    TSUMUGI_SASAYAKI(Type.VOICE_VOX, "8", "春日部つむぎ-ノーマル"),
    HAU_SASAYAKI(Type.VOICE_VOX, "10", "雨晴はう-ノーマル"),
    CUL_SASAYAKI(Type.VOICE_VOX, "23", "WhiteCUL-ノーマル"),
    ;

    private final Type type;
    private final String code;
    private final String nameJp;

    /**
     * 選択肢表示用説明文
     */
    public String description() {
        return "%s (%s)".formatted(name(), getNameJp());
    }

    public enum Type {
        AZURE,

        // https://github.com/ts-klassen/ttsQuestV3Voicevox
        VOICE_VOX
    }
}
