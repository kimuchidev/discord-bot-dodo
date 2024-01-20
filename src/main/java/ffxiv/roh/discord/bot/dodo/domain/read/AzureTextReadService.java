package ffxiv.roh.discord.bot.dodo.domain.read;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import ffxiv.roh.discord.bot.dodo.config.SpeechProperties;
import ffxiv.roh.discord.bot.dodo.domain.entity.Voice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static ffxiv.roh.discord.bot.dodo.domain.DirUtils.getCacheAudioFileName;

@RequiredArgsConstructor
@Service
@Slf4j
public class AzureTextReadService {
    private final SpeechProperties speechProperties;
    private final GuildMusicManager musicManager;

    public void read(Voice voice, String text) {
        SpeechSynthesisOutputFormat format = SpeechSynthesisOutputFormat.Audio48Khz192KBitRateMonoMp3;

        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechProperties.getKey(), speechProperties.getRegion());
        speechConfig.setSpeechSynthesisVoiceName(voice.getCode());
        speechConfig.setSpeechSynthesisOutputFormat(format);

        String fileName = getCacheAudioFileName();
        AudioConfig fileOutputConfig = AudioConfig.fromWavFileOutput(fileName);

        SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, fileOutputConfig);

        {
            SpeechSynthesisResult result = null;
            try {
                result = synthesizer.SpeakTextAsync(text).get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("テキストの音声変換で予期せぬエラー発生：{}", e.getMessage());
            }

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                log.debug("Speech synthesized for text [{}]", text);
                musicManager.queueLocalMp3(fileName);
            } else if (result.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(result);
                log.warn("CANCELED: Reason={}", cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    log.error("CANCELED: ErrorCode={}", cancellation.getErrorCode());
                    log.error("CANCELED: ErrorDetails={}", cancellation.getErrorDetails());
                    log.error("CANCELED: Did you update the subscription info?");
                }
            }
            result.close();
        }
        synthesizer.close();
    }
}
