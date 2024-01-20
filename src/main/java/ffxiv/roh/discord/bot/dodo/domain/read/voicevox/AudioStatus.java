package ffxiv.roh.discord.bot.dodo.domain.read.voicevox;

public record AudioStatus(
        boolean success,
        boolean isAudioReady,
        boolean isAudioError,
        String status,
        String speaker,
        int audioCount,
        long updatedTime
) {
}
