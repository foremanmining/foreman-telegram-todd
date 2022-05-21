package mn.foreman.telegrambot.bot;

import mn.foreman.telegrambot.db.session.ChatSession;
import mn.foreman.telegrambot.db.session.SessionRepository;
import mn.foreman.telegrambot.db.session.SessionState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The Foreman Telegram bot. */
@Component
public class ForemanBot
        extends TelegramLongPollingBot {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(ForemanBot.class);

    /** The callback processors. */
    private final Map<CallbackData, CallbackProcessor> callbackProcessors;

    /** The processor. */
    private final Map<Command, CommandProcessor> commandProcessors;

    /** The full username. */
    private final String fullUsername;

    /** The processor for notifications. */
    private final NotificationsProcessor notificationsProcessor;

    /** The reply processors. */
    private final Map<SessionState, ReplyProcessor> replyProcessors;

    /** The session repository. */
    private final SessionRepository sessionRepository;

    /** The token. */
    private final String token;

    /** The username. */
    private final String username;

    /**
     * Constructor.
     *
     * @param username               The username.
     * @param fullUsername           The full username.
     * @param token                  The token.
     * @param sessionRepository      The session repository.
     * @param commandProcessors      The command processors.
     * @param callbackProcessors     The callback processors.
     * @param notificationsProcessor The notification processor.
     * @param replyProcessors        The reply processors.
     */
    @Autowired
    public ForemanBot(
            @Value("${bot.username}") final String username,
            @Value("${bot.fullUsername}") final String fullUsername,
            @Value("${bot.token}") final String token,
            final SessionRepository sessionRepository,
            final Map<Command, CommandProcessor> commandProcessors,
            final Map<CallbackData, CallbackProcessor> callbackProcessors,
            final NotificationsProcessor notificationsProcessor,
            final Map<SessionState, ReplyProcessor> replyProcessors) {
        this.username = username;
        this.fullUsername = fullUsername;
        this.token = token;
        this.sessionRepository = sessionRepository;
        this.commandProcessors = new HashMap<>(commandProcessors);
        this.callbackProcessors = new HashMap<>(callbackProcessors);
        this.notificationsProcessor = notificationsProcessor;
        this.replyProcessors = new HashMap<>(replyProcessors);
    }

    /**
     * Periodically fetches notifications for each session that this bot
     * monitors and sends messages to users, as necessary.
     */
    @Scheduled(
            initialDelayString = "${bot.check.initialDelay}",
            fixedDelayString = "${bot.check.fixedDelay}")
    public void fetchAndNotify() {
        final List<ChatSession> sessions =
                this.sessionRepository.findAll();
        LOG.info("Looking for notifications for {} sessions", sessions.size());
        sessions
                .parallelStream()
                .filter(session -> session.getDateRegistered() != null)
                .forEach(
                        session ->
                                this.notificationsProcessor.process(
                                        session,
                                        this::sendMessage));
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

    @Override
    public String getBotUsername() {
        return this.username;
    }

    @Override
    public void onUpdateReceived(final Update update) {
        if (update.hasMessage() || update.hasChannelPost()) {
            final Message message;
            if (update.hasMessage()) {
                message = update.getMessage();
            } else {
                message = update.getChannelPost();
            }

            if (message.hasText()) {
                handleMessage(message);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    /**
     * Processes the provided callback, interpreted as the provided {@link
     * CallbackData}.
     *
     * @param data          The data.
     * @param callbackQuery The query.
     */
    private void handleCallback(
            final CallbackData data,
            final CallbackQuery callbackQuery) {
        this.callbackProcessors
                .getOrDefault(
                        data,
                        new CallbackProcessorNull())
                .process(
                        data,
                        callbackQuery,
                        this::sendMessage);
    }

    /**
     * Processes the provided callback.
     *
     * @param callbackQuery The callback.
     */
    private void handleCallback(
            final CallbackQuery callbackQuery) {
        final String data = callbackQuery.getData();
        CallbackData.forData(data)
                .ifPresent(
                        callbackData ->
                                handleCallback(
                                        callbackData,
                                        callbackQuery));
    }

    /**
     * Processes the provided message as interpreted as the provided {@link
     * Command}.
     *
     * @param message The message.
     * @param command The command.
     */
    private void handleCommand(
            final Message message,
            final Command command) {
        this.commandProcessors
                .getOrDefault(
                        command,
                        new CommandProcessorNull())
                .processMessage(
                        message,
                        this::sendMessage);
    }

    /**
     * Processes the provided message.
     *
     * @param message The message to process.
     */
    private void handleMessage(final Message message) {
        final String text = message.getText();
        LOG.debug("Received: {}", text);
        final String chatId = message.getChatId().toString();
        final Optional<Command> command = Command.forText(text);
        if (command.isPresent()) {
            handleCommand(
                    message,
                    command.get());
        } else if (message.isReply()) {
            final Message replyToMessage = message.getReplyToMessage();
            final User from = replyToMessage.getFrom();
            if (this.fullUsername.equals(from.getUserName())) {
                final Optional<ChatSession> chatSessionOpt =
                        this.sessionRepository.findById(chatId);
                if (chatSessionOpt.isPresent()) {
                    final ChatSession chatSession =
                            chatSessionOpt.get();
                    final SessionState sessionState =
                            chatSession.getSessionState();
                    final ReplyProcessor replyProcessor =
                            this.replyProcessors.getOrDefault(
                                    sessionState,
                                    new ReplyProcessorNull());
                    replyProcessor.processReply(
                            message,
                            this::sendMessage);
                } else {
                    // Out of sync
                    sendMessage(MessageUtils.startOver(chatId));
                }
            }
        }
    }

    /**
     * Utility for sending messages.
     *
     * @param sendMessage The message to send.
     */
    private void sendMessage(final SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (final Exception e) {
            LOG.warn(
                    "Exception occurred while sending {}",
                    sendMessage,
                    e);
        }
    }


}
