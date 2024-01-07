package ffxiv.roh.discord.bot.dodo.config;


import java.util.Set;


public record TextReadProperties(
        Set<String> readTargetChanelIds
) {
}