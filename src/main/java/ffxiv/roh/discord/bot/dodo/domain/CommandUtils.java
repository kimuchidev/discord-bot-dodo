package ffxiv.roh.discord.bot.dodo.domain;

import ffxiv.roh.discord.bot.dodo.domain.exception.StopProcessException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandUtils {
    public static String getOptionString(SlashCommandInteractionEvent event, String key) {
        return getOptionString(event, key, null);
    }

    public static String getOptionString(SlashCommandInteractionEvent event, String key, String defaultValue) {
        OptionMapping optionMapping = event.getOption(key);
        return optionMapping == null ? defaultValue : optionMapping.getAsString();
    }

    public static String getInputString(ModalInteractionEvent event, String key) {
        return getInputString(event, key, null);
    }

    public static LocalDateTime getInputDateTime(ModalInteractionEvent event, String key) {
        String inputString = getInputString(event, key);
        if (inputString == null) {
            return null;
        }

        if (inputString.length() != 12) {
            // 所定形式じゃないので、可能な限り解析してみる
            inputString = inputString.replace("年", "");
            inputString = inputString.replace("月", "");
            inputString = inputString.replace("日", "");
            inputString = inputString.replace("時", "");
            inputString = inputString.replace("分", "");
            inputString = inputString.replace(" ", "");
            inputString = inputString.replace("　", "");
            inputString = inputString.replace("/", "");
            inputString = inputString.replace(":", "");
        }

        try {
            return LocalDateTime.parse(inputString, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(inputString, DateTimeFormatter.ofPattern("yyyyMMddHmm"));
        }
    }

    public static String getInputString(ModalInteractionEvent event, String key, String defaultValue) {
        ModalMapping modalMapping = event.getValue(key);
        return modalMapping == null ? defaultValue : modalMapping.getAsString();
    }

    /**
     * @return オプション未指定の場合 0 を返す
     */
    public static int getOptionInt(SlashCommandInteractionEvent event, String key) {
        return getOptionInt(event, key, 0);
    }

    public static int getOptionInt(SlashCommandInteractionEvent event, String key, int defaultValue) {
        OptionMapping optionMapping = event.getOption(key);
        return optionMapping == null ? defaultValue : optionMapping.getAsInt();
    }

    /**
     * Returns the current AudioChannelUnion that the Member is in.
     */
    public static Optional<AudioChannel> getAudioChannel(SlashCommandInteractionEvent event) throws StopProcessException {
        var member = event.getMember();
        if (member == null) {
            event.reply("メンバー情報を取得できませんでした。管理者に連絡してください。").setEphemeral(true).queue();
            throw new StopProcessException();
        }
        var voiceState = member.getVoiceState();
        if (voiceState == null) {
            event.reply("voiceState を取得できませんでした。管理者に連絡してください。(voiceState requires CacheFlag.VOICE_STATE to be enabled!)").setEphemeral(true).queue();
            throw new StopProcessException();
        }
        return Optional.ofNullable(voiceState.getChannel());
    }

    public static String getUserId(SlashCommandInteractionEvent event) throws StopProcessException {
        var member = event.getMember();
        if (member == null) {
            event.reply("メンバー情報を取得できませんでした。管理者に連絡してください。").setEphemeral(true).queue();
            throw new StopProcessException();
        }
        return member.getId();
    }

    public static String getNickname(SlashCommandInteractionEvent event) throws StopProcessException {
        var member = event.getMember();
        if (member == null) {
            event.reply("メンバー情報を取得できませんでした。管理者に連絡してください。").setEphemeral(true).queue();
            throw new StopProcessException();
        }
        return member.getNickname();
    }

    public static String getNickname(ModalInteractionEvent event) throws StopProcessException {
        var member = event.getMember();
        if (member == null) {
            event.reply("メンバー情報を取得できませんでした。管理者に連絡してください。").setEphemeral(true).queue();
            throw new StopProcessException();
        }
        return member.getNickname();
    }


}
