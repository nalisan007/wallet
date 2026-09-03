package io.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.Cursor;
import io.wallet.exception.InvalidCursorException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class CursorService {

    private final ObjectMapper objectMapper;

    public CursorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(Cursor cursor) {
        try {
            String json = objectMapper.writeValueAsString(cursor);

            return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                    json.getBytes(StandardCharsets.UTF_8)
                );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                "Unable to encode pagination cursor",
                exception
            );
        }
    }

    public Cursor decode(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder()
                .decode(encodedCursor);

            String json = new String(
                decoded,
                StandardCharsets.UTF_8
            );

            Cursor cursor = objectMapper.readValue(
                json,
                Cursor.class
            );

            if (cursor.createdAt() == null || cursor.id() == null) {
                throw new InvalidCursorException(encodedCursor);
            }

            return cursor;

        } catch (IllegalArgumentException | JsonProcessingException exception) {
            throw new InvalidCursorException(encodedCursor);
        }
    }
}
