package ffxiv.roh.discord.bot.dodo.domain.entity;

import ffxiv.roh.discord.bot.dodo.domain.party.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * パーティー募集の応募者
 */
@Getter
@Setter
@AllArgsConstructor
public class EntryInfo {
    private String userId;
    private String name;
    private List<Role> entryRoles;

    public static final Converter<EntryInfo, String> ENTRY_INFO_STRING_CONVERTER =
            new Converter<EntryInfo, String>() {
                @Override
                public String convert(EntryInfo entryInfo) {
                    return entryInfo.getUserId() + ";" +
                            entryInfo.getName() + ";" +
                            entryInfo.entryRoles.stream().map(Role::name).collect(joining(","));
                }
            };

    public static final Converter<String, EntryInfo> STRING_ENTRY_INFO_CONVERTER =
            new Converter<String, EntryInfo>() {
                @Override
                public EntryInfo convert(String s) {
                    String[] splits = s.split(";");
                    return new EntryInfo(
                            splits[0],
                            splits[1],
                            splits[2] == null ? new ArrayList<>() : Arrays.stream(splits[2].split(",")).map(Role::valueOf).toList()
                    );
                }
            };
}
