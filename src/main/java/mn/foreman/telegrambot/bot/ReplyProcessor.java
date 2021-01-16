package mn.foreman.telegrambot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.Consumer;

/**
 * A {@link ReplyProcessor} provides a mechanism for processing a stateful
 * reply.
 */
public interface ReplyProcessor {

    /**
     * Processes the reply message.
     *
     * @param message The message.
     * @param sender  The message sender.
     */
    void processReply(
            Message message,
            Consumer<SendMessage> sender);
}
