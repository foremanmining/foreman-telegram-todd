package mn.foreman.telegrambot.bot;

import mn.foreman.api.ForemanApi;
import mn.foreman.api.ForemanApiImpl;
import mn.foreman.api.JdkWebUtil;
import mn.foreman.api.endpoints.notifications.Notifications;
import mn.foreman.telegrambot.db.session.ChatSession;
import mn.foreman.telegrambot.db.session.SessionRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

/**
 * A simple {@link NotificationsProcessor} implementation that sends
 * markdown-formatted messages to the provided chat based on the session that's
 * to be notified.
 */
@Component
public class NotificationsProcessorImpl
        implements NotificationsProcessor {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(NotificationsProcessorImpl.class);

    /** The base URL for Foreman. */
    private final String foremanBaseUrl;

    /** The max notifications to send at once. */
    private final int maxNotifications;

    /** The mapper. */
    private final ObjectMapper objectMapper;

    /** The session repository. */
    private final SessionRepository sessionRepository;

    /** The bot start time. */
    private final Instant startTime;

    /**
     * Constructor.
     *
     * @param sessionRepository The session repository.
     * @param objectMapper      The mapper.
     * @param startTime         The start time.
     * @param maxNotifications  The max notifications to send at once.
     * @param foremanBaseUrl    The Foreman base URL.
     */
    public NotificationsProcessorImpl(
            final SessionRepository sessionRepository,
            final ObjectMapper objectMapper,
            final Instant startTime,
            @Value("${notifications.max}") final int maxNotifications,
            @Value("${foreman.baseUrl}") final String foremanBaseUrl) {
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.startTime = startTime;
        this.maxNotifications = maxNotifications;
        this.foremanBaseUrl = foremanBaseUrl;
    }

    @Override
    public void process(
            final ChatSession chatSession,
            final Consumer<SendMessage> sender) {
        final ForemanApi foremanApi =
                new ForemanApiImpl(
                        Integer.toString(chatSession.getClientId()),
                        "",
                        this.objectMapper,
                        new JdkWebUtil(
                                this.foremanBaseUrl,
                                chatSession.getApiKey()));

        final Notifications notificationsApi =
                foremanApi.notifications();

        final Instant registered = chatSession.getDateRegistered();

        final List<Notifications.Notification> notifications =
                notificationsApi.telegram(
                        chatSession.getLastNotificationId(),
                        registered.isAfter(this.startTime)
                                ? registered
                                : this.startTime);

        LOG.info("Session {} has {} pending notifications",
                chatSession,
                notifications);
        if (!notifications.isEmpty()) {
            notifications
                    .stream()
                    .map(this::toNotificationMessage)
                    .map(message ->
                            MessageUtils.simpleMessage(
                                    chatSession.getChatId(),
                                    message))
                    .forEach(sender);

            final Notifications.Notification lastNotification =
                    Iterables.getLast(notifications);
            chatSession.setLastNotificationId(lastNotification.id);
            this.sessionRepository.save(chatSession);
        }
    }

    /**
     * Appends the provided {@link Notifications.Notification.FailingMiner} as a
     * markdown list item.
     *
     * @param failingMiner  The miner.
     * @param stringBuilder The builder for creating the aggregated message.
     */
    private void appendMiner(
            final Notifications.Notification.FailingMiner failingMiner,
            final StringBuilder stringBuilder) {
        stringBuilder
                .append(
                        String.format(
                                "[%s](%s/dashboard/miners/%d/details/)",
                                failingMiner.miner,
                                this.foremanBaseUrl,
                                failingMiner.minerId))
                .append("\n");
        failingMiner
                .diagnosis
                .forEach(
                        diag ->
                                stringBuilder
                                        .append(diag)
                                        .append("\n"));
        stringBuilder
                .append("\n");
    }

    /**
     * Converts the provided notification to a Telegram message to be sent.
     *
     * @param notification The notification to process.
     *
     * @return The Telegram, markdown-formatted message.
     */
    private String toNotificationMessage(
            final Notifications.Notification notification) {
        final StringBuilder messageBuilder =
                new StringBuilder();

        // Write the subject
        messageBuilder.append(
                String.format(
                        "%s *%s*",
                        !notification.failingMiners.isEmpty()
                                ? ":x:"
                                : ":white_check_mark:",
                        notification.subject));

        final List<Notifications.Notification.FailingMiner> failingMiners =
                notification.failingMiners;
        if (!failingMiners.isEmpty()) {
            // Write the miners out as lists
            messageBuilder.append("\n\n");
            failingMiners
                    .stream()
                    .limit(this.maxNotifications)
                    .forEach(
                            miner ->
                                    appendMiner(
                                            miner,
                                            messageBuilder));
            if (failingMiners.size() > this.maxNotifications) {
                // Too many were failing
                messageBuilder
                        .append("\n\n")
                        .append(
                                String.format(
                                        "*...and %d more*",
                                        failingMiners.size() - this.maxNotifications))
                        .append("\n\n")
                        .append(
                                String.format(
                                        "Head to [your dashboard](%s/dashboard/) to see the rest",
                                        this.foremanBaseUrl));
            }
        }

        return messageBuilder.toString();
    }
}
