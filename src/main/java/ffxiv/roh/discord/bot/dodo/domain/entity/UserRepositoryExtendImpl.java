package ffxiv.roh.discord.bot.dodo.domain.entity;

import com.google.cloud.spring.data.datastore.core.DatastoreOperations;
import com.google.cloud.spring.data.datastore.repository.support.SimpleDatastoreRepository;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryExtendImpl extends SimpleDatastoreRepository<User, String> implements UserRepository {
    public UserRepositoryExtendImpl(DatastoreOperations datastoreTemplate) {
        super(datastoreTemplate, User.class);
    }

    @Override
    public User findOrCreate(String userId, String userName) {
        if (!existsById(userId)) {
            User newUser = User.getDefaultInstance(userId, userName);
            save(newUser);
        }
        return findById(userId).orElseThrow();
    }
}
