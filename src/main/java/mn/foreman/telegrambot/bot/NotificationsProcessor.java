package mn.foreman.telegrambot.bot;

import mn.foreman.telegrambot.db.session.ChatSession;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.function.Consumer;

/**
 * A {@link NotificationsProcessor} provides a mechanism for obtaining pending
 * Telegram notifications for a chat via the Foreman API and sends notifications
 * to a chat accordingly.
 */
public interface NotificationsProcessor {

    /**
     * Obtains notifications for the provided session and notifies the chat, as
     * necessary.
     *
     * @param chatSession The session.
     * @param sender      The callback for sending messages.
     */
    void process(
            ChatSession chatSession,
            Consumer<SendMessage> sender);
}
