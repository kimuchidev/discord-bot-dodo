package ffxiv.roh.discord.bot.dodo.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("voicevox")
@NoArgsConstructor
@Data
public class VoiceVoxProperties {
    private String url;
    private String[] apiKeys;

    public void setApiKeys(String apiKeys) {
        this.apiKeys = apiKeys.split(",");
    }
}