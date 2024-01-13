package ffxiv.roh.discord.bot.dodo.domain.read;


import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.config.TextReadProperties;
import ffxiv.roh.discord.bot.dodo.domain.CommandListener;
import ffxiv.roh.discord.bot.dodo.domain.CommandUtils;
import ffxiv.roh.discord.bot.dodo.domain.entity.User;
import ffxiv.roh.discord.bot.dodo.domain.entity.UserRepository;
import ffxiv.roh.discord.bot.dodo.domain.entity.Voice;
import ffxiv.roh.discord.bot.dodo.domain.exception.StopProcessException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReadCommandListener extends CommandListener {
    private final TextReadProperties textReadProperties;
    private final GuildMusicManager guildMusicManager;
    private final UserRepository userRepository;
    private AudioManager audioManager;

    public ReadCommandListener(DiscordProperties discordProperties, TextReadProperties textReadProperties, GuildMusicManager guildMusicManager, UserRepository userRepository) {
        super(discordProperties);
        this.textReadProperties = textReadProperties;
        this.guildMusicManager = guildMusicManager;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) throws StopProcessException {
        String subcommand = event.getSubcommandName();
        switch (subcommand) {
            case "s" -> start(event);
            case "b" -> bye(event);
            case "v" -> changeVoice(event);
            case "n" -> changeName(event);
            case null, default -> log.error("Unknown subcommand: {}", subcommand);
        }
    }

    void start(SlashCommandInteractionEvent event) throws StopProcessException {
        var audioChannel = CommandUtils.getAudioChannel(event);
        if (audioChannel.isPresent()) {
            connectTo(audioChannel.get());
        } else {
            event.reply("Voice チャンネルに参加した状態で実行してください。").setEphemeral(true).queue();
            return;
        }
        textReadProperties.setReadTargetChanelId(event.getChannel().getId());
        event.reply("<#%s> のメッセージ読み上げを開始します。".formatted(event.getChannel().getId())).queue();
    }

    void bye(SlashCommandInteractionEvent event) {
        event.reply("<#%s> のメッセージ読み上げを終了します。".formatted(textReadProperties.getReadTargetChanelId())).queue();
        textReadProperties.setReadTargetChanelId("");
        audioManager.closeAudioConnection();
        audioManager = null;
    }

    void changeVoice(SlashCommandInteractionEvent event) throws StopProcessException {
        Voice voiceChangeTobe = Voice.valueOf(CommandUtils.getOptionString(event, "voice"));

        String userId = CommandUtils.getUserId(event);
        String userName = CommandUtils.getNickname(event);
        User user = userRepository.findOrCreate(userId, userName);
        user.setVoice(voiceChangeTobe);
        userRepository.save(user);

        event.reply("以降 %s の声で読み上げます。".formatted(voiceChangeTobe.getVoiceNameJp())).setEphemeral(true).queue();
    }

    void changeName(SlashCommandInteractionEvent event) throws StopProcessException {
        String spellChangeTobe = CommandUtils.getOptionString(event, "name");

        String userId = CommandUtils.getUserId(event);
        String userName = CommandUtils.getNickname(event);
        User user = userRepository.findOrCreate(userId, userName);
        user.setSpell(spellChangeTobe);
        userRepository.save(user);

        event.reply("以降 %s とお呼びします。".formatted(spellChangeTobe)).setEphemeral(true).queue();
    }

    private void connectTo(AudioChannel channel) {
        if (audioManager != null) {
            audioManager.closeAudioConnection();
        }
        Guild guild = channel.getGuild();
        audioManager = guild.getAudioManager();
        audioManager.setSendingHandler(guildMusicManager.getSendHandler());
        audioManager.openAudioConnection(channel);
    }

    @Override
    public String getCommandCode() {
        return "read";
    }

    @Override
    public String getDescription() {
        return "実行したチャンネルのメッセージをドードーが読み上げます。";
    }

    @Override
    public SubcommandGroupData getCommandData() {
        String voiceCodes = Arrays.stream(Voice.values()).map(Voice::name).collect(Collectors.joining("|"));

        OptionData voiceOptionData = new OptionData(OptionType.STRING, "voice", "読み上げ時の声", true);
        Arrays.stream(Voice.values()).forEach(voice -> voiceOptionData.addChoice(voice.name(), voice.name()));

        return new SubcommandGroupData(getCommandCode(), getDescription())
                .addSubcommands(
                        new SubcommandData("s", "`s[tart]`: 読み上げ開始。"),
                        new SubcommandData("b", "`b[ye]`: 読み上げ終了。"),
                        new SubcommandData("v", "`v[oice] <voice>`: 声を変更。（ユーザー別設定）")
                                .addOptions(voiceOptionData),
                        new SubcommandData("n", "`n[ame] <name>`: 名前を変更。（ユーザー別設定）")
                                .addOption(OptionType.STRING, "name", "読み上げる名前", true, true)
                );
    }
}
