package mn.foreman.telegrambot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.function.Consumer;

/**
 * A {@link CallbackProcessor} provides a mechanism for processing callbacks as
 * they're received.
 */
public interface CallbackProcessor {

    /**
     * Processes the provided callback.
     *
     * @param callbackData  The callback data.
     * @param callbackQuery The query.
     * @param callback      The handler for sending messages.
     */
    void process(
            final CallbackData callbackData,
            final CallbackQuery callbackQuery,
            final Consumer<SendMessage> callback);
}
