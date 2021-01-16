package mn.foreman.telegrambot.bot;

import mn.foreman.api.ForemanApi;
import mn.foreman.api.ping.Ping;
import mn.foreman.telegrambot.db.session.ChatSession;
import mn.foreman.telegrambot.db.session.SessionRepository;
import mn.foreman.telegrambot.utils.ForemanUtils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A {@link CommandProcessorTest} provides a command implementation that will
 * test API connectivity.
 */
public class CommandProcessorTest
        implements CommandProcessor {

    /** The Foreman base URL. */
    private final String foremanBaseUrl;

    /** The session repository. */
    private final SessionRepository sessionRepository;

    /**
     * Constructor.
     *
     * @param sessionRepository The session repository.
     * @param foremanBaseUrl    The Foreman base URL.
     */
    public CommandProcessorTest(
            final SessionRepository sessionRepository,
            final String foremanBaseUrl) {
        this.sessionRepository = sessionRepository;
        this.foremanBaseUrl = foremanBaseUrl;
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
            final ChatSession chatSession = chatSessionOpt.get();
            final ForemanApi foremanApi =
                    ForemanUtils.toApi(
                            chatSession,
                            this.foremanBaseUrl);
            final Ping ping = foremanApi.ping();

            callback.accept(
                    MessageUtils.simpleMessage(
                            chatId,
                            "Checking connectivity to Foreman..."));
            if (ping.ping()) {
                callback.accept(
                        MessageUtils.simpleMessage(
                                chatId,
                                "*Result*: :white_check_mark:"));
            } else {
                callback.accept(
                        MessageUtils.simpleMessage(
                                chatId,
                                "*Result*: :x:"));
            }

            callback.accept(
                    MessageUtils.simpleMessage(
                            chatId,
                            "Checking authentication with your API credentials..."));
            if (ping.pingClient()) {
                callback.accept(
                        MessageUtils.simpleMessage(
                                chatId,
                                "*Result*: :white_check_mark:"));
            } else {
                callback.accept(
                        MessageUtils.simpleMessage(
                                chatId,
                                "*Result*: :x:"));
            }
        } else {
            callback.accept(
                    MessageUtils.simpleMessage(
                            chatId,
                            "We haven't met"));
        }
    }
}
