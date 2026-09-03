package io.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.Cursor;
import io.wallet.exception.InvalidCursorException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
public class CursorService {

    private final ObjectMapper objectMapper;

    public CursorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(Cursor cursor) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(cursor);

            return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                "Unable to encode cursor",
                exception
            );
        }
    }

    public Cursor decode(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            byte[] decoded =
                Base64.getUrlDecoder().decode(encodedCursor);

            Cursor cursor =
                objectMapper.readValue(
                    decoded,
                    Cursor.class
                );

            validate(cursor);

            return cursor;

        } catch (
            IllegalArgumentException |
            JsonProcessingException exception
        ) {
            throw new InvalidCursorException(
                "Cursor is malformed or invalid"
            );
        }
    }

    private void validate(Cursor cursor) {
        if (cursor == null
            || cursor.createdAt() == null
            || cursor.id() == null
            || cursor.id().version() != 7) {

            throw new InvalidCursorException(
                "Cursor contains invalid pagination data"
            );
        }
    }
}
