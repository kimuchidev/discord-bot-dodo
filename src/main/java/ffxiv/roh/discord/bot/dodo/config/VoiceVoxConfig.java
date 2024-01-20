package ffxiv.roh.discord.bot.dodo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
@RequiredArgsConstructor
public class VoiceVoxConfig {
    @Bean
    @Scope("singleton")
    public VoiceVoxProperties voiceVoxProperties() {
        return new VoiceVoxProperties();
    }
}
