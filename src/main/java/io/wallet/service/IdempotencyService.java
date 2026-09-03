package io.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.IdempotencyRecord;
import io.wallet.entity.TransferRequest;
import io.wallet.exception.IdempotencyKeyReuseException;
import io.wallet.repository.IdempotencyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(
        IdempotencyRecordRepository idempotencyRecordRepository,
        ObjectMapper objectMapper
    ) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public IdempotencyRecord findExistingOrCreate(
        UUID idempotencyKey,
        TransferRequest request
    ) {
        String requestHash = calculateRequestHash(request);

        return idempotencyRecordRepository
            .findByIdempotencyKeyForUpdate(idempotencyKey)
            .map(existing -> {
                if (!existing.getRequestHash().equals(requestHash)) {
                    throw new IdempotencyKeyReuseException(idempotencyKey);
                }

                return existing;
            })
            .orElseGet(() -> createRecord(idempotencyKey, requestHash));
    }

    private IdempotencyRecord createRecord(
        UUID idempotencyKey,
        String requestHash
    ) {
        IdempotencyRecord record = new IdempotencyRecord(
            idempotencyKey,
            requestHash
        );

        return idempotencyRecordRepository.save(record);
    }

    public String calculateRequestHash(TransferRequest request) {
        try {
            String canonicalRequest = objectMapper
                .writeValueAsString(request);

            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                canonicalRequest.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (JsonProcessingException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "Unable to calculate request hash",
                exception
            );
        }
    }
}
