package io.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.IdempotencyRecord;
import io.wallet.entity.TransferRequest;
import io.wallet.exception.IdempotencyKeyProcessingException;
import io.wallet.exception.IdempotencyKeyReuseException;
import io.wallet.repository.IdempotencyRecordRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public IdempotencyService(
        IdempotencyRecordRepository repository,
        ObjectMapper objectMapper,
        Clock clock
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public Result findExistingOrCreate(
        UUID idempotencyKey,
        TransferRequest request
    ) {
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Transfer request is required");
        }

        String requestHash = calculateRequestHash(request);
        Instant now = clock.instant();

        int inserted = repository.insertIfAbsent(idempotencyKey, requestHash, now);

        IdempotencyRecord record = repository.findById(idempotencyKey)
            .orElseThrow(() -> new IllegalStateException(
                "Idempotency record could not be read after creation"
            ));

        validateHash(record, requestHash);

        return new Result(record, inserted == 1);
    }

    private void validateHash(IdempotencyRecord record, String requestHash) {
        if (!MessageDigest.isEqual(
            record.getRequestHash().getBytes(StandardCharsets.UTF_8),
            requestHash.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new IdempotencyKeyReuseException(record.getIdempotencyKey());
        }
    }

    public record Result(IdempotencyRecord record, boolean created) {}

    public IdempotencyKeyProcessingException processing(UUID key) {
        return new IdempotencyKeyProcessingException(key);
    }

    private String calculateRequestHash(TransferRequest request) {
        try {
            String canonical = objectMapper.writeValueAsString(
                new CanonicalTransferRequest(
                    request.fromWalletId(),
                    request.toWalletId(),
                    request.amountPaise()
                )
            );
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8))
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize transfer request", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private record CanonicalTransferRequest(
        UUID fromWalletId,
        UUID toWalletId,
        Long amountPaise
    ) {}
}
