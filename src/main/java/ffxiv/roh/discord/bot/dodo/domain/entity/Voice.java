package ffxiv.roh.discord.bot.dodo.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Voice {
    boy1("ja-JP-KeitaNeural", "男性-Keita"),
    boy2("ja-JP-DaichiNeural", "男性-Daichi"),
    boy3("ja-JP-NaokiNeural", "男性-Naoki"),
    girl1("ja-JP-NanamiNeural", "女性-Nanami"),
    girl2("ja-JP-AoiNeural", "女性-Aoi"),
    girl3("ja-JP-MayuNeural", "女性-Mayu"),
    girl4("ja-JP-ShioriNeural", "女性-Shiori");

    private final String voiceName;
    private final String voiceNameJp;
}
