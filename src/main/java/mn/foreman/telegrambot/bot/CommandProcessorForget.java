package mn.foreman.telegrambot.bot;

import mn.foreman.telegrambot.db.session.ChatSession;
import mn.foreman.telegrambot.db.session.SessionRepository;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A {@link CommandProcessorForget} provides a command implementation that will
 * stop the bot from messaging.
 */
public class CommandProcessorForget
        implements CommandProcessor {

    /** The session repository. */
    private final SessionRepository sessionRepository;

    /**
     * Constructor.
     *
     * @param sessionRepository The session repository.
     */
    public CommandProcessorForget(final SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void processMessage(
            final Message message,
            final Consumer<SendMessage> callback) {
        final String chatId =
                message.getChatId().toString();
        final Optional<ChatSession> chatSessionOpt =
                this.sessionRepository.findById(chatId);
        if (chatSessionOpt.isPresent()) {
            this.sessionRepository.delete(chatSessionOpt.get());
            callback.accept(
                    MessageUtils.simpleMessage(
                            chatId,
                            "I won't message you here anymore"));
        } else {
            callback.accept(
                    MessageUtils.simpleMessage(
                            chatId,
                            "We haven't met"));
        }
    }
}
