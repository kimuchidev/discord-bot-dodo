package ffxiv.roh.discord.bot.dodo.domain.party;


import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.domain.CommandListener;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PartyCommandListener extends CommandListener {
    public PartyCommandListener(DiscordProperties discordProperties) {
        super(discordProperties);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        //TODO
    }

    @Override
    public String getCommandCode() {
        return "party";
    }

    @Override
    public String getDescription() {
        return "募集掲示用メッセージを作成します。";
    }

    @Override
    public SubcommandGroupData getCommandData() {
        return new SubcommandGroupData("party", "パーティー募集機能")
                .addSubcommands(
                        new SubcommandData("new", "new")
                                .addOption(OptionType.STRING, "content", "コンテンツ", true, true)
                                .addOption(OptionType.STRING, "date", "yyyyMMdd", true, true)
                                .addOption(OptionType.STRING, "time", "hh:mm", true, true)
                                .addOption(OptionType.STRING, "role", "TTHHDDDD", true, true)
                                .addOption(OptionType.STRING, "comment", "任意のコメント", true, true)
                        ,
                        new SubcommandData("edit", "edit"),
                        new SubcommandData("delete", "delete")
                );
    }
}
