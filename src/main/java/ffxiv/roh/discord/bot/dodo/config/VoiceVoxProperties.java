package ffxiv.roh.discord.bot.dodo.config;


import ffxiv.roh.discord.bot.dodo.domain.exception.NoMoreApiKeyException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("voicevox")
public class VoiceVoxProperties {
    @Getter
    @Setter
    private String url;
    @Setter
    private List<String> apiKeys;

    private int mainApiKeyIndex = 0;

    public String getApiKey() {
        return apiKeys.get(mainApiKeyIndex);
    }

    public void shiftApiKey() throws NoMoreApiKeyException {
        if (mainApiKeyIndex == apiKeys.size() - 1) {
            mainApiKeyIndex = 0;
            throw new NoMoreApiKeyException();
        }
        mainApiKeyIndex = mainApiKeyIndex + 1;
    }
}