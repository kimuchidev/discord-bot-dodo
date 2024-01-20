package ffxiv.roh.discord.bot.dodo.domain.read.voicevox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ffxiv.roh.discord.bot.dodo.config.VoiceVoxProperties;
import ffxiv.roh.discord.bot.dodo.domain.DirUtils;
import ffxiv.roh.discord.bot.dodo.domain.entity.Voice;
import ffxiv.roh.discord.bot.dodo.domain.read.GuildMusicManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@RequiredArgsConstructor
@Service
@Slf4j
public class VoiceVoxTextReadService {

    private final VoiceVoxProperties voiceVoxProperties;
    private final GuildMusicManager musicManager;

    public void read(Voice voice, String text) throws Exception {
        requestSynthesis(voice, text);
    }

    void requestSynthesis(Voice voice, String text) throws Exception {
        HttpClient client = HttpClient.newBuilder().build();
        String url = "%s?key=%s&speaker=%s&text=%s".formatted(
                voiceVoxProperties.getUrl(),
                voiceVoxProperties.getApiKey(),
                voice.getCode(),
                URLEncoder.encode(text, StandardCharsets.UTF_8)
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        var synthesisResult = gson.fromJson(response.body(), SynthesisResult.class);

        // API 問題の場合、 key を変えてリトライ
        if (!synthesisResult.isApiKeyValid()) {
            // url 取れてる場合は 低速モードなので、続く。
            if (!StringUtils.hasLength(synthesisResult.mp3DownloadUrl())) {
                voiceVoxProperties.shiftApiKey();
                requestSynthesis(voice, text);
            }
        }

        RetryTemplate template = RetryTemplate.builder()
                .maxAttempts(10)
                .fixedBackoff(1000)
                .retryOn(RuntimeException.class)
                .build();
        template.execute(ctx -> {
            checkStatus(synthesisResult);
            return null;
        });

        playMp3(synthesisResult);
    }

    void playMp3(SynthesisResult synthesisResult) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(synthesisResult.mp3DownloadUrl()))
                .build();
        String fileName = DirUtils.getCacheAudioFileName();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(Path.of(fileName)))
                .thenAccept(response -> {
                    if (response.statusCode() == 200)
                        musicManager.queueLocalMp3(fileName);
                });
    }

    void checkStatus(SynthesisResult requestSynthesis) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestSynthesis.audioStatusUrl()))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("音声変換失敗。");
        }

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        var audioStatus = gson.fromJson(response.body(), AudioStatus.class);

        if (audioStatus.isAudioReady()) {
            return;
        }
        throw new RuntimeException("音声変換待ち。");
    }
}
