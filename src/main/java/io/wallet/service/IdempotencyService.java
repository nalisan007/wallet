package io.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
        this.objectMapper = objectMapper.copy()
            .configure(
                SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,
                true
            );
    }

    @Transactional
    public IdempotencyRecord findExistingOrCreate(
        UUID idempotencyKey,
        TransferRequest request
    ) {
        String requestHash = calculateRequestHash(request);

        /*
         * The unique database constraint on idempotency_key prevents
         * two concurrent requests from creating two records for the
         * same key.
         *
         * After the record exists, the pessimistic lock serializes
         * concurrent requests using that key.
         */
        IdempotencyRecord existing =
            idempotencyRecordRepository
                .findByIdempotencyKeyForUpdate(idempotencyKey)
                .orElse(null);

        if (existing != null) {
            validateRequestHash(existing, requestHash);
            return existing;
        }

        IdempotencyRecord record =
            new IdempotencyRecord(
                idempotencyKey,
                requestHash
            );

        try {
            return idempotencyRecordRepository.saveAndFlush(record);
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            /*
             * Another transaction may have inserted the same
             * idempotency key between our lookup and insert.
             *
             * The current transaction must not continue after a
             * constraint violation because MySQL/JPA may mark the
             * transaction rollback-only. The concurrent-request
             * serialization is therefore ultimately guaranteed by
             * the unique constraint plus the transaction boundary.
             */
            throw exception;
        }
    }

    private void validateRequestHash(
        IdempotencyRecord existing,
        String requestHash
    ) {
        if (!MessageDigest.isEqual(
            existing.getRequestHash()
                .getBytes(StandardCharsets.UTF_8),
            requestHash.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new IdempotencyKeyReuseException(
                existing.getIdempotencyKey()
            );
        }
    }

    private String calculateRequestHash(
        TransferRequest request
    ) {
        String canonicalRequest;

        try {
            canonicalRequest =
                objectMapper.writeValueAsString(
                    new CanonicalTransferRequest(
                        request.fromWalletId(),
                        request.toWalletId(),
                        request.amountPaise()
                    )
                );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                "Unable to calculate request hash",
                exception
            );
        }

        try {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] hash =
                digest.digest(
                    canonicalRequest.getBytes(
                        StandardCharsets.UTF_8
                    )
                );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 algorithm is not available",
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
