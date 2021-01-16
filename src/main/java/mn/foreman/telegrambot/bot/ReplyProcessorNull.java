package mn.foreman.telegrambot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.Consumer;

/** Do nothing reply processor (out of sync). */
public class ReplyProcessorNull
        implements ReplyProcessor {

    @Override
    public void processReply(
            final Message message,
            final Consumer<SendMessage> sender) {
        sender.accept(MessageUtils.startOver(message.getChatId().toString()));
    }
}
