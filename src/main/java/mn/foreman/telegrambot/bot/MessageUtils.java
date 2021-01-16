package mn.foreman.telegrambot.bot;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

/** Utilities for generating {@link SendMessage messages}. */
public class MessageUtils {

    /**
     * Utility function for sending a standard {@link SendMessage} with markdown
     * formatting.
     *
     * @param chatId        The receiving chat id.
     * @param message       The message.
     * @param replyKeyboard The reply keyboard.
     *
     * @return The message to be sent.
     */
    public static SendMessage simpleMessage(
            final String chatId,
            final String message,
            final ReplyKeyboard replyKeyboard) {
        final SendMessage.SendMessageBuilder builder =
                SendMessage
                        .builder()
                        .parseMode(ParseMode.MARKDOWN)
                        .chatId(chatId)
                        .text(EmojiParser.parseToUnicode(message));
        if (replyKeyboard != null) {
            builder.replyMarkup(replyKeyboard);
        }
        return builder.build();
    }

    /**
     * Utility function for sending a standard {@link SendMessage} with markdown
     * formatting.
     *
     * @param chatId  The receiving chat id.
     * @param message The message.
     *
     * @return The message to be sent.
     */
    public static SendMessage simpleMessage(
            final String chatId,
            final String message) {
        return SendMessage
                .builder()
                .parseMode(ParseMode.MARKDOWN)
                .chatId(chatId)
                .text(EmojiParser.parseToUnicode(message))
                .disableWebPagePreview(true)
                .build();
    }

    /**
     * Creates a specific reason for a start-over message.
     *
     * @param chatId The chat ID.
     * @param cause  The reason.
     *
     * @return The message.
     */
    public static SendMessage startOver(
            final String chatId,
            final String cause) {
        final SendMessage sendMessage;
        if (cause != null) {
            sendMessage =
                    MessageUtils.simpleMessage(
                            chatId,
                            String.format(
                                    "%s... Let's start over: %s",
                                    cause,
                                    Command.START.getKey()));
        } else {
            sendMessage = startOver(chatId);
        }
        return sendMessage;
    }

    /**
     * Creates a generic start over message.
     *
     * @param chatId The chat ID.
     *
     * @return The message.
     */
    public static SendMessage startOver(
            final String chatId) {
        return startOver(
                chatId,
                "How'd we get here");
    }

    public static SendMessage withReplyMarkup(
            final ReplyKeyboard replyMarkup,
            final String chatId,
            final String message) {
        return simpleMessage(
                chatId,
                message,
                replyMarkup);
    }
}
