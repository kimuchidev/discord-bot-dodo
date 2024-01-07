package ffxiv.roh.discord.bot.dodo.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("discord")
public class DiscordProperties {
    private String token;
    private String commandPrefix;
}