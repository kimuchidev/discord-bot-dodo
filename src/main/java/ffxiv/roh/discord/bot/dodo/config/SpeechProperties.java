package ffxiv.roh.discord.bot.dodo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("azure.speech")
public class SpeechProperties {
    String Key;
    String region;
}
