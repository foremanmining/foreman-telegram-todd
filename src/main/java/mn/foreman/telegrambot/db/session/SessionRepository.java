package mn.foreman.telegrambot.db.session;

import org.springframework.data.mongodb.repository.MongoRepository;

/** A repository for storing {@link ChatSession sessions}. */
public interface SessionRepository
        extends MongoRepository<ChatSession, String> {

}
