package ffxiv.roh.discord.bot.dodo.domain.party;

import ffxiv.roh.discord.bot.dodo.domain.exception.CantParseRoleException;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.core.convert.converter.Converter;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

public record Roles(List<Role> roles) {
    public static Roles empty() {
        return new Roles(List.of());
    }

    /**
     * フォーム入力された値を Roles に変換する。
     */
    public static Roles parse(String roleString) throws CantParseRoleException {
        try {
            return new Roles(Arrays.stream(roleString.split(","))
                    .flatMap(singleRoleString -> {
                        String str = singleRoleString.trim();
                        Role role = Role.valueOf(str.replaceAll("\\d+", ""));
                        String countStr = str.replace(role.name(), "");
                        if (countStr.isEmpty()) {
                            countStr = "1";
                        }
                        int count = Integer.parseInt(countStr);
                        Stream.Builder<Role> roleStream = Stream.builder();
                        for (int i = 0; i < count; i++) {
                            roleStream.add(role);
                        }
                        return roleStream.build();
                    })
                    .toList());
        } catch (Exception e) {
            throw new CantParseRoleException(roleString);
        }
    }

    /**
     * @return 優先度でソートした　Role Set
     */
    public Set<Role> getSortedRoleSet() {
        return roles.stream()
                .sorted(Comparator.comparing(Role::getPriority))
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * @return 優先度でソートした　Role List
     */
    public List<Role> getSortedRoles() {
        return roles.stream()
                .sorted(Comparator.comparing(Role::getPriority))
                .toList();
    }

    public String getEmojis(Guild guild) {
        var Emojis = guild.getEmojis();
        return roles.stream().map(role -> role.getEmojiAsMention(guild)).collect(joining());
    }

    public String toInputValue() {
        return getSortedRoleSet().stream()
                .map(role -> role.name() + roles.stream().filter(r -> r == role).count())
                .collect(joining(","));
    }


    public static final Converter<Roles, String> ROLES_STRING_CONVERTER =
            new Converter<Roles, String>() {
                @Override
                public String convert(Roles roles) {
                    return roles.roles.stream().map(Role::name).collect(joining(","));
                }
            };

    public static final Converter<String, Roles> STRING_ROLES_CONVERTER =
            new Converter<String, Roles>() {
                @Override
                public Roles convert(String s) {
                    return new Roles(Arrays.stream(s.split(",")).map(Role::valueOf).toList());
                }
            };
}
