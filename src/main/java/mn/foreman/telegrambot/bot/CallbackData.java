package mn.foreman.telegrambot.bot;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link CallbackData} provides an enumeration of all of the known Telegram
 * bot callback data.
 */
public enum CallbackData {

    /** The data representing a completed start. */
    DONE_START("done_start");

    /** Mapping of all of the known datas to their callbacks. */
    private static final Map<String, CallbackData> VALUES =
            new ConcurrentHashMap<>();

    static {
        for (final CallbackData callbackData : values()) {
            VALUES.put(callbackData.data, callbackData);
        }
    }

    /** The data. */
    private final String data;

    /**
     * Constructor.
     *
     * @param data The data.
     */
    CallbackData(final String data) {
        this.data = data;
    }

    /**
     * Attempts to determine the data type from the data in the message.
     *
     * @param data The message data.
     *
     * @return The data type.
     */
    public static Optional<CallbackData> forData(final String data) {
        return Optional.ofNullable(VALUES.get(data));
    }

    /**
     * Returns the data.
     *
     * @return The data.
     */
    public String getData() {
        return this.data;
    }
}
