package com.portfolio.studio.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface ObjectStore {

    /**
 * Stores content under the specified key.
 *
 * @param key           the key under which to store the content
 * @param content       the content stream
 * @param contentLength the content length in bytes
 * @param contentType   the content type
 * @throws IOException if the content cannot be stored
 */
void put(String key, InputStream content, long contentLength, String contentType) throws IOException;

    /**
 * Retrieves the object associated with a key.
 *
 * @param key the key identifying the object
 * @return the stored object, or an empty optional if no object is associated with the key
 * @throws IOException if the object cannot be retrieved
 */
Optional<StoredObject> get(String key) throws IOException;

    /**
 * Determines whether an object exists for the specified key.
 *
 * @param key the object key
 * @return {@code true} if an object exists for the key, {@code false} otherwise
 * @throws IOException if the existence check fails
 */
boolean exists(String key) throws IOException;

    /**
 * Deletes the object stored under the specified key, if present.
 *
 * @param key the key identifying the object
 * @throws IOException if the deletion fails
 */
void deleteIfPresent(String key) throws IOException;

    record StoredObject(InputStream content, long contentLength, String contentType) {
    }
}
