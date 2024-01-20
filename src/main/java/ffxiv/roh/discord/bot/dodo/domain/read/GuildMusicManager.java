package ffxiv.roh.discord.bot.dodo.domain.read;

import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

@Slf4j
public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final TrackScheduler scheduler;

    /**
     * Creates a player and a track scheduler.
     *
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    public void queueLocalMp3(String fileName) {
        localAudioTrack(fileName).ifPresent(scheduler::queue);
    }

    private static Optional<Mp3AudioTrack> localAudioTrack(String fileName) {
        try {
            return Optional.of(new Mp3AudioTrack(
                    dummyTrackInfo(),
                    new NonSeekableInputStream(new FileInputStream(fileName))
            ));
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException:{}", e.getMessage());
            return Optional.empty();
        }
    }

    private static AudioTrackInfo dummyTrackInfo() {
        return new AudioTrackInfo("", "", 0, "", true, "");
    }

}