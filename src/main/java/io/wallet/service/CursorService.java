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
        if (cursor == null
            || cursor.createdAt() == null
            || cursor.id() == null) {
            throw new InvalidCursorException(
                "Cursor cannot be null"
            );
        }

        try {
            byte[] json =
                objectMapper.writeValueAsBytes(cursor);

            return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json);

        } catch (JsonProcessingException exception) {
            throw new InvalidCursorException(
                "Unable to encode cursor"
            );
        }
    }

    public Cursor decode(String encodedCursor) {
        if (encodedCursor == null
            || encodedCursor.isBlank()) {
            throw new InvalidCursorException(
                "Cursor cannot be empty"
            );
        }

        try {
            byte[] decoded =
                Base64.getUrlDecoder()
                    .decode(encodedCursor);

            Cursor cursor =
                objectMapper.readValue(
                    decoded,
                    Cursor.class
                );

            if (cursor.createdAt() == null
                || cursor.id() == null) {
                throw new InvalidCursorException(
                    "Cursor is missing required fields"
                );
            }

            return cursor;

        } catch (
            IllegalArgumentException
                | JsonProcessingException exception
        ) {
            throw new InvalidCursorException(
                "Invalid cursor"
            );
        }
    }
}
