package ffxiv.roh.discord.bot.dodo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("cheer")
@PropertySource(value = "classpath:cheer.yaml", factory = YamlPropertySourceFactory.class)
public class CheerProperties {
    private List<String> messages;

    public String getRandomMessage() {
        //index をランダム生成して、対象メッセージを返す
        int index = (int) (Math.random() * messages.size());
        return messages.get(index);
    }
}
