package mn.foreman.telegrambot.db.session;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.Instant;

/**
 * A {@link ChatSession} represents the bot's state for each registered chat
 * id.
 */
@Data
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    /** The API key. */
    private String apiKey;

    /** The chat id. */
    @Id
    private String chatId;

    /** The client ID. */
    private int clientId;

    /** When the session was added. */
    private Instant dateRegistered;

    /** The last notification id. */
    private int lastNotificationId;

    /** The session state. */
    private SessionState sessionState;
}
