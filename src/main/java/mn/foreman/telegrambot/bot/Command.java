package mn.foreman.telegrambot.bot;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** All of the known commands. */
public enum Command {

    /** Start bot setup. */
    START("/start"),

    /** Channel registration. */
    REGISTER("/register"),

    /** Stop bot interactions. */
    FORGET("/forget"),

    /** Test bot connectivity. */
    TEST("/test");

    /** All of the known commands. */
    private static final ConcurrentMap<String, Command> VALUES =
            new ConcurrentHashMap<>();

    static {
        for (final Command command : values()) {
            VALUES.put(command.key, command);
        }
    }

    /** The key. */
    private final String key;

    /**
     * Constructor.
     *
     * @param key The key.
     */
    Command(final String key) {
        this.key = key;
    }

    /**
     * Returns the command related to the text.
     *
     * @param text The text.
     *
     * @return The command.
     */
    public static Optional<Command> forText(final String text) {
        final String[] regions = text.split(" ");
        if (regions.length > 0) {
            return Optional.ofNullable(VALUES.get(regions[0]));
        }
        return Optional.empty();
    }

    /**
     * Returns the key.
     *
     * @return The key.
     */
    public String getKey() {
        return this.key;
    }
}
