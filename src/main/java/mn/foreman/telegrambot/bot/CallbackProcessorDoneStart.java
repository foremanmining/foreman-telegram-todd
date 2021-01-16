package mn.foreman.telegrambot.bot;

import mn.foreman.telegrambot.db.session.ChatSession;
import mn.foreman.telegrambot.db.session.SessionRepository;
import mn.foreman.telegrambot.db.session.SessionState;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;

import java.util.Optional;
import java.util.function.Consumer;

/** Processor for {@link CallbackData#DONE_START} notifications. */
public class CallbackProcessorDoneStart
        implements CallbackProcessor {

    /** The session repository. */
    private final SessionRepository sessionRepository;

    /**
     * Constructor.
     *
     * @param sessionRepository The session repository.
     */
    public CallbackProcessorDoneStart(final SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void process(
            final CallbackData callbackData,
            final CallbackQuery callbackQuery,
            final Consumer<SendMessage> callback) {
        final String chatId =
                callbackQuery
                        .getMessage()
                        .getChatId().toString();
        final Optional<ChatSession> chatSessionOpt =
                this.sessionRepository.findById(chatId);
        if (chatSessionOpt.isPresent()) {
            final ChatSession chatSession = chatSessionOpt.get();
            chatSession.setSessionState(SessionState.WAITING_CLIENT_ID);
            this.sessionRepository.save(chatSession);

            callback.accept(
                    MessageUtils.withReplyMarkup(
                            new ForceReplyKeyboard(true),
                            chatId,
                            "Great! Now enter your client ID"));
        } else {
            callback.accept(MessageUtils.startOver(chatId));
        }
    }
}
