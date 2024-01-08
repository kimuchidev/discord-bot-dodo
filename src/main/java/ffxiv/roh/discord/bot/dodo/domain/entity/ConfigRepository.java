package ffxiv.roh.discord.bot.dodo.domain.entity;

import com.google.cloud.spring.data.datastore.repository.DatastoreRepository;

public interface ConfigRepository extends DatastoreRepository<Config, String> {
}
