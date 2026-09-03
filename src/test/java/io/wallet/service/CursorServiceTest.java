package io.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.Cursor;
import io.wallet.exception.InvalidCursorException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CursorServiceTest {

    private final CursorService cursorService =
        new CursorService(new ObjectMapper());

    @Test
    void shouldEncodeAndDecodeCursor() {

        Cursor cursor = new Cursor(
            Instant.parse("2026-01-01T10:00:00Z"),
            UUID.fromString(
                "019c0000-0000-7000-8000-000000000001"
            )
        );

        String encoded = cursorService.encode(cursor);

        Cursor decoded =
            cursorService.decode(encoded);

        assertEquals(
            cursor.createdAt(),
            decoded.createdAt()
        );

        assertEquals(
            cursor.id(),
            decoded.id()
        );
    }

    @Test
    void shouldReturnNullForMissingCursor() {

        assertEquals(
            null,
            cursorService.decode(null)
        );

        assertEquals(
            null,
            cursorService.decode("")
        );
    }

    @Test
    void shouldRejectMalformedCursor() {

        assertThrows(
            InvalidCursorException.class,
            () -> cursorService.decode("invalid-cursor")
        );
    }

    @Test
    void shouldRejectCursorWithNonUuidV7Id() {

        Cursor cursor = new Cursor(
            Instant.parse("2026-01-01T10:00:00Z"),
            UUID.randomUUID()
        );

        String encoded = cursorService.encode(cursor);

        assertThrows(
            InvalidCursorException.class,
            () -> cursorService.decode(encoded)
        );
    }
}
