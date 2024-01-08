package ffxiv.roh.discord.bot.dodo.domain.entity;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import com.google.cloud.spring.data.datastore.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Entity(name = "users")
@AllArgsConstructor
public class User {
    @Id
    @Field(name = "id")
    private String id;
    private String name;
    private Voice voice;
    private String spell;

    public static User getDefaultInstance(String id, String name) {
        return new User(id, name, Voice.boy1, name);
    }
}