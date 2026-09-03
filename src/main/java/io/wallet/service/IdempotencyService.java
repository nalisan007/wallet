package io.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wallet.entity.IdempotencyRecord;
import io.wallet.entity.TransferRequest;
import io.wallet.exception.IdempotencyKeyReuseException;
import io.wallet.repository.IdempotencyRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(
        IdempotencyRecordRepository repository,
        ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public IdempotencyRecord findExistingOrCreate(
        UUID idempotencyKey,
        TransferRequest request
    ) {
        if (idempotencyKey == null) {
            throw new IllegalArgumentException(
                "Idempotency key is required"
            );
        }

        String requestHash = calculateRequestHash(request);

        IdempotencyRecord existing =
            repository.findById(idempotencyKey).orElse(null);

        if (existing != null) {
            validateHash(existing, requestHash);
            return existing;
        }

        IdempotencyRecord record =
            new IdempotencyRecord(
                idempotencyKey,
                requestHash
            );

        try {
            return repository.saveAndFlush(record);
        } catch (DataIntegrityViolationException exception) {
            /*
             * Another transaction may have inserted the same key
             * concurrently. Read the committed record and validate
             * the request hash.
             */
            IdempotencyRecord concurrent =
                repository.findById(idempotencyKey)
                    .orElseThrow(() ->
                        exception
                    );

            validateHash(concurrent, requestHash);

            return concurrent;
        }
    }

    private void validateHash(
        IdempotencyRecord record,
        String requestHash
    ) {
        if (!MessageDigest.isEqual(
            record.getRequestHash()
                .getBytes(StandardCharsets.UTF_8),
            requestHash.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new IdempotencyKeyReuseException(
                record.getIdempotencyKey()
            );
        }
    }

    private String calculateRequestHash(
        TransferRequest request
    ) {
        try {
            String canonicalRequest =
                objectMapper.writeValueAsString(
                    new CanonicalTransferRequest(
                        request.fromWalletId(),
                        request.toWalletId(),
                        request.amountPaise()
                    )
                );

            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                digest.digest(
                    canonicalRequest.getBytes(
                        StandardCharsets.UTF_8
                    )
                )
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                "Unable to serialize transfer request",
                exception
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is not available",
                exception
            );
        }
    }

    private record CanonicalTransferRequest(
        UUID fromWalletId,
        UUID toWalletId,
        Long amountPaise
    ) {
    }
}
