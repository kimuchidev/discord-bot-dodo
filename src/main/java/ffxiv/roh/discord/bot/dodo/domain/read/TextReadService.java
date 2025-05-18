package ffxiv.roh.discord.bot.dodo.domain.read;

import ffxiv.roh.discord.bot.dodo.domain.entity.*;
import ffxiv.roh.discord.bot.dodo.domain.read.azure.AzureTextReadService;
import ffxiv.roh.discord.bot.dodo.domain.read.voicevox.VoiceVoxTextReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static ffxiv.roh.discord.bot.dodo.domain.DirUtils.deleteOldCache;
import static java.lang.Thread.sleep;

@RequiredArgsConstructor
@Service
@Slf4j
public class TextReadService {
    private final ConfigRepository configRepository;
    private final AzureTextReadService azureTextReadService;
    private final VoiceVoxTextReadService voiceVoxTextReadService;

    public void read(User user, String text) throws Exception {
        if (text.equals("__DO__DEBUG__VOICE__VOX")) {
            for (Voice v : Arrays.stream(Voice.values()).filter(v -> v.getType() == Voice.Type.VOICE_VOX).toList()) {
                read(user, v, "「%s」。こんにちは。".formatted(v.getNameJp()));
                sleep(5000);
            }
        } else {
            read(user, user.getVoice(), transformText(user, text));
        }
        deleteOldCache();
    }

    private void read(User user, Voice voice, String text) throws Exception {
        switch (voice.getType()) {
            case AZURE -> azureTextReadService.read(voice, transformText(user, text));
            case VOICE_VOX -> voiceVoxTextReadService.read(voice, transformText(user, text));
        }
    }

    private String transformText(User user, String text) {
        String result = String.valueOf(text);
        // URL を消す
        result = result.replaceAll("https://game8.jp/.*", "[ゲームエイトurl]");
        result = result.replaceAll("https://www.youtube.com/.*", "[ユーチューブurl]");
        result = result.replaceAll("http://.*", "[url]");
        result = result.replaceAll("https://.*", "[url]");
        // discord の絵文字を消す
        result = result.replaceAll("<:.*:[0-9]*>", "[絵文字]");

        Config config = configRepository.findById(ConfigKey.readName.name()).orElseGet(() -> new Config(ConfigKey.readName.name(), "true"));
        if (config.getValue().equals("true")) {
            return user.getSpell() + "\n" + result;
        }
        return result;
    }
}
