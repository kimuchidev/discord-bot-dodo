package ffxiv.roh.discord.bot.dodo.domain.read;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ffxiv.roh.discord.bot.dodo.config.SpeechProperties;
import ffxiv.roh.discord.bot.dodo.domain.entity.Config;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigKey;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigRepository;
import ffxiv.roh.discord.bot.dodo.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Service
@Slf4j
public class TextReadService {
    private final SpeechProperties speechProperties;
    private final GuildMusicManager musicManager;
    private final ConfigRepository configRepository;


    public void read(User user, String text) {
        read(transformText(user, text),
                user.getVoice().getVoiceName(),
                SpeechSynthesisOutputFormat.Audio48Khz192KBitRateMonoMp3);
    }

    public void read(String text, String voice, SpeechSynthesisOutputFormat format) {
        deleteOldCache();

        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechProperties.getKey(), speechProperties.getRegion());
        speechConfig.setSpeechSynthesisVoiceName(voice);
        speechConfig.setSpeechSynthesisOutputFormat(format);

        String fileName = "cache/Audio_%s.mp3".formatted(Clock.systemUTC().millis());
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
                Optional<Mp3AudioTrack> localAudioTrack = localAudioTrack(fileName);
                localAudioTrack.ifPresent(musicManager.scheduler::queue);
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

    private String transformText(User user, String text) {
        Config config = configRepository.findById(ConfigKey.readName.name()).orElseGet(() -> new Config(ConfigKey.readName.name(), "true"));
        if (config.getValue().equals("true")) {
            return "%s: %s".formatted(user.getSpell(), text);
        }
        return text;
    }

    private static Optional<Mp3AudioTrack> localAudioTrack(String fileName) {
        try {
            return Optional.of(new Mp3AudioTrack(
                    dummyTrackInfo(),
                    new NonSeekableInputStream(new FileInputStream(fileName))
            ));
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException:{}", e.getMessage());
            return Optional.empty();
        }
    }

    private static AudioTrackInfo dummyTrackInfo() {
        return new AudioTrackInfo("", "", 0, "", true, "");
    }

    public void deleteOldCache() {
        File cacheDir = new File("cache");
        Date oldestAllowedFileDate = DateUtils.addMinutes(new Date(), -1); //minus days from current date
        Iterator<File> targetFiles = FileUtils.iterateFiles(cacheDir, new AgeFileFilter(oldestAllowedFileDate), null);
        while (targetFiles.hasNext()) {
            var targetFile = targetFiles.next();
            log.debug("Deleting file: {}", targetFile.getName());
            FileUtils.deleteQuietly(targetFile);
        }
    }
}
