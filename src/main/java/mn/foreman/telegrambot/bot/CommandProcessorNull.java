package mn.foreman.telegrambot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.Consumer;

/** Do nothing handler. */
public class CommandProcessorNull
        implements CommandProcessor {

    @Override
    public void processMessage(
            final Message message,
            final Consumer<SendMessage> callback) {
        // Do nothing
    }
}
