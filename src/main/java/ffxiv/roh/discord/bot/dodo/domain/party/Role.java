package ffxiv.roh.discord.bot.dodo.domain.party;

import ffxiv.roh.discord.bot.dodo.domain.entity.EntryInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Role {
    T(19) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case T, MT, ST -> true;
                default -> false;
            };
        }
    },
    MT(11) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case MT -> true;
                default -> false;
            };
        }

        @Override
        public String getEmojiName() {
            return "\uD83C\uDDF2";
        }

        @Override
        public String getEmojiMentionName() {
            return "regional_indicator_m";
        }

        @Override
        public String getEmojiCodePoint() {
            //https://emojipedia.org/ja/regional-indicator-symbol-letter-m#technical
            return "U+1F1F2";
        }
    },
    ST(12) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case ST -> true;
                default -> false;
            };
        }

        @Override
        public String getEmojiName() {
            return "\uD83C\uDDF8";
        }

        @Override
        public String getEmojiMentionName() {
            return "regional_indicator_s";
        }

        @Override
        public String getEmojiCodePoint() {
            //https://emojipedia.org/ja/regional-indicator-symbol-letter-s#technical
            return "U+1F1F8";
        }
    },
    H(29) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case H, PH, BH -> true;
                default -> false;
            };
        }
    },
    BH(21) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case BH -> true;
                default -> false;
            };
        }
    },
    PH(22) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case PH -> true;
                default -> false;
            };
        }
    },
    D(39) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case D, MD, RD, CD -> true;
                default -> false;
            };
        }
    },
    MD(31) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case MD -> true;
                default -> false;
            };
        }
    },
    RD(32) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case RD -> true;
                default -> false;
            };
        }
    },
    CD(33) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return switch (role) {
                case CD -> true;
                default -> false;
            };
        }
    },
    F(99) {
        @Override
        boolean isEntryAbleBy(Role role) {
            return true;
        }
    };

    private final String defaultEmojiName = "%sRole".formatted(name());

    public String getEmojiName() {
        return defaultEmojiName;
    }

    public String getEmojiMentionName() {
        return defaultEmojiName;
    }

    public String getEmojiCodePoint() {
        throw new RuntimeException("getEmojiCodePoint は実装されてないため、呼び出されるべきではありません。");
    }

    // 優先度
    private final int priority;

    abstract boolean isEntryAbleBy(Role role);

    // this.role が entryInfo.entryRoles の対象になれるか
    public boolean isEntryAbleBy(EntryInfo entryInfo) {
        return entryInfo.getEntryRoles()
                .stream()
                .anyMatch(this::isEntryAbleBy);
    }

    public Emoji getEmoji(Guild guild) {
        List<RichCustomEmoji> emoji = guild.getEmojisByName(this.getEmojiName(), true);
        if (emoji.isEmpty()) {
            return Emoji.fromUnicode(getEmojiCodePoint());
        }
        return emoji.getFirst();
    }

    public String getEmojiAsMention(Guild guild) {
        List<RichCustomEmoji> emoji = guild.getEmojisByName(this.getEmojiName(), true);
        if (emoji.isEmpty()) {
            return ":%s:".formatted(getEmojiMentionName());
        }
        return emoji.getFirst().getAsMention();
    }

    public static Role emojiOf(String emojiName) {
        for (Role role : values()) {
            if (role.getEmojiName().equals(emojiName)) {
                return role;
            }
        }
        return null;
    }
}
