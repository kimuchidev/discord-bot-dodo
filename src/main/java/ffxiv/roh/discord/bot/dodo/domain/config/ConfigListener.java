package ffxiv.roh.discord.bot.dodo.domain.config;

import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.domain.CommandUtils;
import ffxiv.roh.discord.bot.dodo.domain.SlashCommandListener;
import ffxiv.roh.discord.bot.dodo.domain.entity.Config;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigKey;
import ffxiv.roh.discord.bot.dodo.domain.entity.ConfigRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class ConfigListener extends SlashCommandListener {
    private final ConfigRepository configRepository;

    public ConfigListener(DiscordProperties discordProperties, ConfigRepository configRepository) {
        super(discordProperties);
        this.configRepository = configRepository;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        String requestId = CommandUtils.getOptionString(event, "value");
        configRepository.save(new Config(ConfigKey.getKey(subcommand), requestId));
        event.reply("設定完了。").setEphemeral(true).queue();
    }

    @Override
    public String getCommandCode() {
        return "config";
    }

    @Override
    public String getDescription() {
        return "Set config。";
    }

    @Override
    public SubcommandGroupData getCommandData() {
        return new SubcommandGroupData(getCommandCode(), getDescription())
                .addSubcommands(
                        Arrays.stream(ConfigKey.values()).map(key ->
                                new SubcommandData(key.getCode(), key.getDescription())
                                        .addOption(OptionType.STRING, "value", "値", true, false)
                        ).toList()
                );
    }
}
