package mn.foreman.telegrambot.config;

import mn.foreman.telegrambot.bot.*;
import mn.foreman.telegrambot.db.session.SessionRepository;
import mn.foreman.telegrambot.db.session.SessionState;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Instant;
import java.util.Map;

/** Configuration for creating the bean necessary to run the Telegram bot. */
@Configuration
public class BotConfig {

    /**
     * Creates the callback processors.
     *
     * @param sessionRepository The session repository.
     *
     * @return The processors.
     */
    @Bean
    public Map<CallbackData, CallbackProcessor> callbackProcessors(
            final SessionRepository sessionRepository) {
        return ImmutableMap.<CallbackData, CallbackProcessor>builder()
                .put(
                        CallbackData.DONE_START,
                        new CallbackProcessorDoneStart(
                                sessionRepository))
                .build();
    }

    /**
     * Creates the command processors.
     *
     * @param sessionRepository The session repository.
     * @param foremanBaseUrl    The base URL.
     *
     * @return The command processors.
     */
    @Bean
    public Map<Command, CommandProcessor> commandProcessors(
            final SessionRepository sessionRepository,
            @Value("${foreman.baseUrl}") final String foremanBaseUrl) {
        return ImmutableMap.<Command, CommandProcessor>builder()
                .put(
                        Command.START,
                        new CommandProcessorStart(
                                sessionRepository,
                                foremanBaseUrl))
                .put(
                        Command.TEST,
                        new CommandProcessorTest(
                                sessionRepository,
                                foremanBaseUrl))
                .put(
                        Command.REGISTER,
                        new CommandProcessorRegister(
                                sessionRepository,
                                foremanBaseUrl))
                .put(
                        Command.FORGET,
                        new CommandProcessorForget(
                                sessionRepository))
                .build();
    }

    /**
     * Returns a new JSON {@link ObjectMapper}.
     *
     * @return The mapper.
     */
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    /**
     * Creates all of the reply processors.
     *
     * @param sessionRepository The session repository.
     * @param foremanBaseUrl    The Foreman base URL.
     *
     * @return The reply processors.
     */
    @Bean
    public Map<SessionState, ReplyProcessor> replyProcessors(
            final SessionRepository sessionRepository,
            @Value("${foreman.baseUrl}") final String foremanBaseUrl) {
        return ImmutableMap.<SessionState, ReplyProcessor>builder()
                .put(
                        SessionState.WAITING_CLIENT_ID,
                        new ReplyProcessorClientId(
                                sessionRepository))
                .put(
                        SessionState.WAITING_API_KEY,
                        new ReplyProcessorApiKey(
                                sessionRepository,
                                foremanBaseUrl))
                .build();
    }

    /**
     * Returns the application start time.
     *
     * @return The application start time.
     */
    @Bean
    public Instant startTime() {
        return Instant.now();
    }

    /**
     * Creates the bot API.
     *
     * @param foremanBot The bot to register.
     *
     * @return The bot API.
     *
     * @throws TelegramApiException on failure.
     */
    @Bean
    public TelegramBotsApi telegramBotsApi(final ForemanBot foremanBot)
            throws TelegramApiException {
        final TelegramBotsApi telegramBotsApi =
                new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(foremanBot);
        return telegramBotsApi;
    }
}
