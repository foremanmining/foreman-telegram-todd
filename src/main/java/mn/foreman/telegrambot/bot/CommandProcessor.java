package mn.foreman.telegrambot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.Consumer;

/**
 * A {@link CommandProcessor} provides a mechanism for processing commands as
 * they're received.
 */
public interface CommandProcessor {

    /**
     * Processes the provided message.
     *
     * @param message  The message.
     * @param callback The callback for sending messages.
     */
    void processMessage(
            Message message,
            Consumer<SendMessage> callback);
}
