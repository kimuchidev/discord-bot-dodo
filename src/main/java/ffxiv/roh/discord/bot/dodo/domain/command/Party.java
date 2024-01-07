package ffxiv.roh.discord.bot.dodo.domain.command;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class Party extends Command {
    @Override
    public void execute(MessageReceivedEvent event, String... options) {
        //TODO
    }

    @Override
    public String getCommand() {
        return "party";
    }

    @Override
    public String getDescription() {
        return "募集掲示用メッセージを作成します。";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public String getExample() {
        return null;
    }
}
