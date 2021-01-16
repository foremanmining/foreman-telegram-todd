package mn.foreman.telegrambot.bot;

import mn.foreman.telegrambot.db.session.ChatSession;
import mn.foreman.telegrambot.db.session.SessionRepository;
import mn.foreman.telegrambot.db.session.SessionState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;

import java.util.Optional;
import java.util.function.Consumer;

/** A {@link ReplyProcessor} implementation that handles a client ID response. */
public class ReplyProcessorClientId
        implements ReplyProcessor {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(ReplyProcessorClientId.class);

    /** The session repository. */
    private final SessionRepository sessionRepository;

    /**
     * Constructor.
     *
     * @param sessionRepository The session repository.
     */
    public ReplyProcessorClientId(final SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void processReply(
            final Message message,
            final Consumer<SendMessage> sender) {
        final String chatId =
                message.getChatId().toString();
        final Optional<ChatSession> chatSessionOpt =
                this.sessionRepository.findById(chatId);
        if (chatSessionOpt.isPresent()) {
            try {
                final int clientId = Integer.parseInt(message.getText());
                final ChatSession chatSession = chatSessionOpt.get();
                chatSession.setClientId(clientId);
                chatSession.setSessionState(SessionState.WAITING_API_KEY);
                this.sessionRepository.save(chatSession);

                sender.accept(
                        MessageUtils.withReplyMarkup(
                                new ForceReplyKeyboard(true),
                                chatId,
                                "And now your api key"));
            } catch (final NumberFormatException nfe) {
                LOG.warn("Number not provided", nfe);
                sender.accept(
                        MessageUtils.startOver(
                                chatId,
                                "That doesn't look like a number"));
            }
        } else {
            sender.accept(MessageUtils.startOver(chatId));
        }
    }
}
