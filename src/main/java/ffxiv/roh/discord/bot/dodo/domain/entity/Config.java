package ffxiv.roh.discord.bot.dodo.domain.entity;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import com.google.cloud.spring.data.datastore.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Entity(name = "configs")
@AllArgsConstructor
public class Config {
    @Id
    @Field(name = "key")
    private String key;
    private String value;
}