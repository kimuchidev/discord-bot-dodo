package ffxiv.roh.discord.bot.dodo.domain.read;

import ffxiv.roh.discord.bot.dodo.domain.entity.Config;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigKey;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigRepository;
import ffxiv.roh.discord.bot.dodo.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static ffxiv.roh.discord.bot.dodo.domain.DirUtils.deleteOldCache;

@RequiredArgsConstructor
@Service
@Slf4j
public class TextReadService {
    private final ConfigRepository configRepository;
    private final AzureTextReadService azureTextReadService;

    public void read(User user, String text) {
        deleteOldCache();
        switch (user.getVoice().getType()) {
            case AZURE -> azureTextReadService.read(user.getVoice(), transformText(user, text));
        }
    }

    private String transformText(User user, String text) {
        Config config = configRepository.findById(ConfigKey.readName.name()).orElseGet(() -> new Config(ConfigKey.readName.name(), "true"));
        if (config.getValue().equals("true")) {
            return "%s: %s".formatted(user.getSpell(), text);
        }
        return text;
    }
}
