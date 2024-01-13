package ffxiv.roh.discord.bot.dodo.domain.entity;

import com.google.cloud.spring.data.datastore.repository.DatastoreRepository;

import java.util.List;

public interface PartyRepository extends DatastoreRepository<Party, String> {
    List<Party> findByBordMessageId(String bordMessageId);

    boolean existsByBordMessageId(String bordMessageId);
}
