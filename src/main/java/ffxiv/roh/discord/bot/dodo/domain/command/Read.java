package ffxiv.roh.discord.bot.dodo.domain.command;


import ffxiv.roh.discord.bot.dodo.config.DiscordProperties;
import ffxiv.roh.discord.bot.dodo.config.TextReadProperties;
import ffxiv.roh.discord.bot.dodo.domain.entity.User;
import ffxiv.roh.discord.bot.dodo.domain.entity.UserRepository;
import ffxiv.roh.discord.bot.dodo.domain.entity.Voice;
import ffxiv.roh.discord.bot.dodo.domain.exception.NoSuchCommandOptionException;
import ffxiv.roh.discord.bot.dodo.domain.read.GuildMusicManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class Read extends Command {
    private final DiscordProperties discordProperties;
    private final TextReadProperties textReadProperties;
    private final GuildMusicManager guildMusicManager;
    private final UserRepository userRepository;

    @Override
    public void execute(MessageReceivedEvent event, String... options) {
        if (options.length == 0 || options[0].startsWith("s")) {
            try {
                AudioChannel audioChannel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
                if (audioChannel != null) {
                    connectTo(audioChannel);
                } else {
                    event.getMessage().reply("Voice チャンネルに参加した状態で実行してください。").queue();
                    return;
                }
            } catch (NullPointerException e) {
                event.getMessage().reply("""
                        読み上げ起動に失敗しました。
                        ```
                        %s
                        ```
                        """.formatted(e)).queue();
                return;
            }

            textReadProperties.setReadTargetChanelId(event.getChannel().getId());
            event.getMessage().reply("<#%s> のメッセージ読み上げを開始します。".formatted(event.getChannel().getId())).queue();
        } else if (options[0].startsWith("b")) {
            event.getMessage().reply("<#%s> のメッセージ読み上げを終了します。".formatted(textReadProperties.getReadTargetChanelId())).queue();
            textReadProperties.setReadTargetChanelId("");
        } else if (options[0].startsWith("v")) {
            Voice voiceChangeTobe;
            try {
                voiceChangeTobe = Voice.valueOf(options[1]);
            } catch (IllegalArgumentException e) {
                event.getMessage().reply("指定の声 %s は存在しません。".formatted(options[1])).queue();
                return;
            }

            String authorId = event.getAuthor().getId();
            String authorName = event.getMember().getNickname();
            User user = userRepository.findOrCreate(authorId, authorName);
            user.setVoice(voiceChangeTobe);
            userRepository.save(user);

            event.getMessage().reply("以降 %s の声で読み上げます。".formatted(voiceChangeTobe.getVoiceNameJp())).queue();
        } else if (options[0].startsWith("n")) {
            String spellChangeTobe = options[1];
            if (!StringUtils.hasLength(spellChangeTobe)) {
                event.getMessage().reply("呼び名を指定する必要があります。").queue();
                return;
            }

            String authorId = event.getAuthor().getId();
            String authorName = event.getMember().getNickname();
            User user = userRepository.findOrCreate(authorId, authorName);
            user.setSpell(spellChangeTobe);
            userRepository.save(user);

            event.getMessage().reply("以降 %s とお呼びします。".formatted(spellChangeTobe)).queue();
        } else {
            throw new NoSuchCommandOptionException();
        }
    }

    private void connectTo(AudioChannel channel) {
        Guild guild = channel.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        audioManager.setSendingHandler(guildMusicManager.getSendHandler());
        audioManager.openAudioConnection(channel);
    }

    @Override
    public String getCommand() {
        return "read";
    }

    @Override
    public String getDescription() {
        return "コマンドを実行したチャンネルのメッセージを読み上げます。";
    }

    @Override
    public String getUsage() {
        return """
                - `${command}`: 読み上げ開始。
                - `${command} s[tart]`: 読み上げ開始。
                - `${command} b[ye]`: 読み上げ終了。
                - `${command} v[oice] <${voice}>`: 読み上げ声変更。（ユーザー別設定）
                - `${command} n[ame] <name>`: 名前変更。（ユーザー別設定）"""
                .replace("${command}", getCommand())
                .replace("${voice}", Arrays.stream(Voice.values()).map(Voice::name).collect(Collectors.joining("|")));
    }

    @Override
    public String getExample() {
        return """
                - `${prefix} ${command} s`: 読み上げ開始。
                - `${prefix} ${command} bye`: 読み上げ終了。
                - `${prefix} ${command} change boy1`: 指定した声で読み上げる。
                - `${prefix} ${command} name ココ`: 指定した名前で呼ぶ。"""
                .replace("${prefix}", discordProperties.getCommandPrefix())
                .replace("${command}", getCommand());
    }

}
