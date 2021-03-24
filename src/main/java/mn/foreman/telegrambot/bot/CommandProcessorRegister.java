package mn.foreman.telegrambot.bot;

import mn.foreman.api.ForemanApi;
import mn.foreman.api.endpoints.ping.Ping;
import mn.foreman.telegrambot.db.session.ChatSession;
import mn.foreman.telegrambot.db.session.SessionRepository;
import mn.foreman.telegrambot.db.session.SessionState;
import mn.foreman.telegrambot.utils.ForemanUtils;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A {@link ReplyProcessor} implementation that handles a client api key
 * response.
 */
public class CommandProcessorRegister
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
    public CommandProcessorRegister(
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

            final String content = message.getText();
            final String[] parts = content.split(" ");
            if (parts.length == 3) {
                try {
                    final int clientId = Integer.parseInt(parts[1]);
                    final String apiKey = parts[2];

                    final ForemanApi foremanApi =
                            ForemanUtils.toApi(
                                    clientId,
                                    apiKey,
                                    this.foremanBaseUrl);
                    final Ping ping = foremanApi.ping();
                    if (ping.pingClient()) {
                        handleSuccess(
                                clientId,
                                apiKey,
                                chatSession,
                                callback);
                    } else {
                        handleFailure(
                                chatId,
                                "I tried those, but they didn't work",
                                callback);
                    }
                } catch (final Exception e) {
                    handleFailure(
                            chatId,
                            callback);
                }
            } else {
                handleFailure(
                        chatId,
                        callback);
            }
        } else {
            handleFailure(
                    chatId,
                    callback);
        }
    }

    /**
     * Sends a fail response.
     *
     * @param chatId The chat ID.
     * @param sender The message sender.
     */
    private void handleFailure(
            final String chatId,
            final Consumer<SendMessage> sender) {
        handleFailure(
                chatId,
                null,
                sender);
    }

    /**
     * Sends a fail response.
     *
     * @param chatId The chat ID.
     * @param reason The reason.
     * @param sender The message sender.
     */
    private void handleFailure(
            final String chatId,
            final String reason,
            final Consumer<SendMessage> sender) {
        sender.accept(
                MessageUtils.startOver(
                        chatId,
                        reason));
    }

    /**
     * Successful API authentication.
     *
     * @param clientId    The client ID.
     * @param apiKey      The API key.
     * @param chatSession The chat session.
     * @param sender      The message sender.
     */
    private void handleSuccess(
            final int clientId,
            final String apiKey,
            final ChatSession chatSession,
            final Consumer<SendMessage> sender) {
        chatSession.setApiKey(apiKey);
        chatSession.setDateRegistered(Instant.now());
        chatSession.setClientId(clientId);
        chatSession.setSessionState(SessionState.CONFIGURED);
        this.sessionRepository.save(chatSession);

        final String chatId = chatSession.getChatId();

        sender.accept(
                MessageUtils.simpleMessage(
                        chatId,
                        "Those look correct! Setup complete! " +
                                ":white_check_mark:"));

        final InlineKeyboardMarkup markupInline =
                new InlineKeyboardMarkup(
                        Collections.singletonList(
                                Collections.singletonList(
                                        InlineKeyboardButton
                                                .builder()
                                                .url(
                                                        String.format(
                                                                "%s/dashboard/triggers/",
                                                                this.foremanBaseUrl))
                                                .text(EmojiParser.parseToUnicode(
                                                        ":arrow_right: Create/Edit Triggers"))
                                                .callbackData(CallbackData.DONE_START.getData())
                                                .build())));
        sender.accept(
                MessageUtils.withReplyMarkup(
                        markupInline,
                        chatId,
                        "You'll get notified based on your *alert* " +
                                "triggers, so make sure you created some and " +
                                "set their _destination_ to Telegram.\n\nIf " +
                                "you've already done this, you should be good " +
                                "to go! :thumbsup:"));
    }
}
