package ffxiv.roh.discord.bot.dodo.domain.party;

import ffxiv.roh.discord.bot.dodo.domain.entity.EntryInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RolePair {
    private Role role;
    private EntryInfo entryInfo;

    public boolean isEntryAbleBy(EntryInfo entryInfo) {
        return role.isEntryAbleBy(entryInfo);
    }

    public String getEntryName() {
        if (entryInfo == null) {
            return null;
        }
        return entryInfo.getName();
    }
}
