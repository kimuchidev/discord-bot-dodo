package ffxiv.roh.discord.bot.dodo.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.lang3.time.DateUtils;

import java.io.File;
import java.time.Clock;
import java.util.Date;
import java.util.Iterator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DirUtils {
    public static String getCacheAudioFileName() {
        return "./cache/Audio_%s.mp3".formatted(Clock.systemUTC().millis());
    }

    public static void deleteOldCache() {
        File cacheDir = new File("cache");
        Date oldestAllowedFileDate = DateUtils.addMinutes(new Date(), -1); //minus days from current date
        Iterator<File> targetFiles = FileUtils.iterateFiles(cacheDir, new AgeFileFilter(oldestAllowedFileDate), null);
        while (targetFiles.hasNext()) {
            var targetFile = targetFiles.next();
            log.debug("Deleting file: {}", targetFile.getName());
            FileUtils.deleteQuietly(targetFile);
        }
    }
}
