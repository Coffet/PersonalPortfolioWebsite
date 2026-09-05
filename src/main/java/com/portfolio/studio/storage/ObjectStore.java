package com.portfolio.studio.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface ObjectStore {

    void put(String key, InputStream content, long contentLength, String contentType) throws IOException;

    Optional<StoredObject> get(String key) throws IOException;

    boolean exists(String key) throws IOException;

    void deleteIfPresent(String key);

    record StoredObject(InputStream content, long contentLength, String contentType) {
    }
}
