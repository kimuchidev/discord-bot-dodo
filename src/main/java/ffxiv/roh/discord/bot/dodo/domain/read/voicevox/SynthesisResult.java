package ffxiv.roh.discord.bot.dodo.domain.read.voicevox;

public record SynthesisResult(
        boolean success,
        boolean isApiKeyValid,
        String speakerName,
        String audioStatusUrl,
        String wavDownloadUrl,
        String mp3DownloadUrl,
        String mp3StreamingUrl) {
}
