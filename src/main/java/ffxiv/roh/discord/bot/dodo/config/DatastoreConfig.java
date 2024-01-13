package ffxiv.roh.discord.bot.dodo.config;

import com.google.cloud.spring.data.datastore.core.convert.DatastoreCustomConversions;
import ffxiv.roh.discord.bot.dodo.domain.entity.EntryInfo;
import ffxiv.roh.discord.bot.dodo.domain.party.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DatastoreConfig {
    @Bean
    public DatastoreCustomConversions datastoreCustomConversions() {
        return new DatastoreCustomConversions(
                Arrays.asList(
                        Roles.ROLES_STRING_CONVERTER, Roles.STRING_ROLES_CONVERTER,
                        EntryInfo.ENTRY_INFO_STRING_CONVERTER, EntryInfo.STRING_ENTRY_INFO_CONVERTER
                )
        );
    }
}
