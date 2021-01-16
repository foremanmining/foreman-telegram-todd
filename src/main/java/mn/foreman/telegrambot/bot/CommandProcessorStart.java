package mn.foreman.telegrambot.bot;

import mn.foreman.telegrambot.db.session.ChatSession;
import mn.foreman.telegrambot.db.session.SessionRepository;
import mn.foreman.telegrambot.db.session.SessionState;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

/** First interaction with the bot to kick-off registration. */
public class CommandProcessorStart
        implements CommandProcessor {

    /** The base URL. */
    private final String foremanBaseUrl;

    /** The repository. */
    private final SessionRepository sessionRepository;

    /**
     * Constructor.
     *
     * @param sessionRepository The repository.
     * @param foremanBaseUrl    The base URL.
     */
    public CommandProcessorStart(
            final SessionRepository sessionRepository,
            final String foremanBaseUrl) {
        this.sessionRepository = sessionRepository;
        this.foremanBaseUrl = foremanBaseUrl;
    }

    @Override
    public void processMessage(
            final Message message,
            final Consumer<SendMessage> callback) {
        final String chatId = message.getChatId().toString();

        final Optional<ChatSession> chatSessionOpt =
                this.sessionRepository.findById(chatId);
        if (chatSessionOpt.isEmpty()) {
            this.sessionRepository.insert(
                    ChatSession
                            .builder()
                            .chatId(chatId)
                            .sessionState(SessionState.INIT)
                            .build());
        } else {
            final ChatSession chatSession = chatSessionOpt.get();
            chatSession.setSessionState(SessionState.INIT);
            this.sessionRepository.save(chatSession);
        }

        final InlineKeyboardMarkup markupInline =
                new InlineKeyboardMarkup(
                        Arrays.asList(
                                Collections.singletonList(
                                        InlineKeyboardButton
                                                .builder()
                                                .url(
                                                        String.format(
                                                                "%s/dashboard/profile/",
                                                                this.foremanBaseUrl))
                                                .text(EmojiParser.parseToUnicode(
                                                        ":arrow_right: Take me to them"))
                                                .build()),
                                Collections.singletonList(
                                        InlineKeyboardButton
                                                .builder()
                                                .text(EmojiParser.parseToUnicode(
                                                        ":ok_hand: I have them"))
                                                .callbackData(CallbackData.DONE_START.getData())
                                                .build())));

        callback.accept(
                MessageUtils.withReplyMarkup(
                        markupInline,
                        chatId,
                        "You'll need your client ID and API key first!"));
    }
}
