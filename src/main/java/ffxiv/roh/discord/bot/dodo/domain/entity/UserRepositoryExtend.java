package ffxiv.roh.discord.bot.dodo.domain.entity;

public interface UserRepositoryExtend {
    User findOrCreate(String userId, String userName);
}
