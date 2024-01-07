package ffxiv.roh.discord.bot.dodo.config;

import ffxiv.roh.discord.bot.dodo.domain.listener.DodoListener;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.HashSet;
import java.util.List;


@Configuration
@RequiredArgsConstructor
public class JdaConfig {

    @Bean
    public DiscordProperties discordProperties() {
        return new DiscordProperties();
    }

    @Bean
    @Scope("singleton")
    public TextReadProperties textReadProperties() {
        return new TextReadProperties(new HashSet<>());
    }

    @Bean
    public JDA jda(DiscordProperties discordProperties,
                   List<DodoListener> dodoListeners) throws InterruptedException {
        var builder = JDABuilder.createDefault(discordProperties.getToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(dodoListeners.toArray());

        JDA jda = builder.build();
        jda.awaitReady();
        return jda;
    }
}
