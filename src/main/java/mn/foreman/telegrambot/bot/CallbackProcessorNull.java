package mn.foreman.telegrambot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.function.Consumer;

/** A null processor. */
public class CallbackProcessorNull
        implements CallbackProcessor {

    @Override
    public void process(
            final CallbackData callbackData,
            final CallbackQuery callbackQuery,
            final Consumer<SendMessage> callback) {
        // Do nothing
    }
}
