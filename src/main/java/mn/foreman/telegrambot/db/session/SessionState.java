package mn.foreman.telegrambot.db.session;

/** A {@link SessionState} provides the states of the user's session. */
public enum SessionState {

    /** Just getting started. */
    INIT,

    /** Waiting for the client's ID. */
    WAITING_CLIENT_ID,

    /** Waiting for the client's API key. */
    WAITING_API_KEY,

    /** The session is fully configured and validated. */
    CONFIGURED;
}
