package ffxiv.roh.discord.bot.dodo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class AzureConfig {

    @Bean
    public SpeechProperties speechProperties() {
        return new SpeechProperties();
    }


}
