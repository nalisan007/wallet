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

    private final CursorService service =
        new CursorService(
            new ObjectMapper()
        );

    @Test
    void shouldEncodeAndDecodeCursor() {
        Cursor original =
            new Cursor(
                Instant.parse(
                    "2026-09-03T10:15:30Z"
                ),
                UUID.randomUUID()
            );

        String encoded =
            service.encode(original);

        Cursor decoded =
            service.decode(encoded);

        assertEquals(
            original.createdAt(),
            decoded.createdAt()
        );

        assertEquals(
            original.id(),
            decoded.id()
        );
    }

    @Test
    void shouldRejectMalformedCursor() {
        assertThrows(
            InvalidCursorException.class,
            () -> service.decode("not-a-valid-cursor")
        );
    }

    @Test
    void shouldRejectEmptyCursor() {
        assertThrows(
            InvalidCursorException.class,
            () -> service.decode("")
        );
    }
}
