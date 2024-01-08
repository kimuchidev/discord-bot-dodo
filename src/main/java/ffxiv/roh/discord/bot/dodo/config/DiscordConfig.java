package ffxiv.roh.discord.bot.dodo.config;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import ffxiv.roh.discord.bot.dodo.domain.DodoListener;
import ffxiv.roh.discord.bot.dodo.domain.read.GuildMusicManager;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;


@Configuration
@RequiredArgsConstructor
public class DiscordConfig {

    @Bean
    public DiscordProperties discordProperties() {
        return new DiscordProperties();
    }

    @Bean
    @Scope("singleton")
    public TextReadProperties textReadProperties() {
        return new TextReadProperties();
    }

    @Bean
    public JDA jda(DiscordProperties discordProperties,
                   List<DodoListener> dodoListeners) throws InterruptedException {
        var builder = JDABuilder.createDefault(discordProperties.getToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(dodoListeners.toArray());

        JDA jda = builder.build();
        jda.awaitReady();
        return jda;
    }

    @Bean
    AudioPlayerManager audioPlayerManager() {
        AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        return audioPlayerManager;
    }

    @Bean
    GuildMusicManager guildMusicManager(AudioPlayerManager audioPlayerManager) {
        return new GuildMusicManager(audioPlayerManager);
    }
}
