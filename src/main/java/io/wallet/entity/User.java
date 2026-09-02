package io.wallet.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "wallet_user",
    indexes = {
        @Index(
            name = "idx_wallet_user_status",
            columnList = "status"
        )
    }
)
public class User {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
        name = "id",
        nullable = false,
        updatable = false,
        columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @NotBlank(message = "User name is required")
    @Size(
        min = 1,
        max = 100,
        message = "User name must be between 1 and 100 characters"
    )
    @Column(
        name = "name",
        nullable = false,
        length = 100
    )
    private String name;

    @NotNull(message = "User status is required")
    @Column(
        name = "status",
        nullable = false,
        length = 20
    )
    private UserStatus status;

    @NotNull(message = "User creation time is required")
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    @NotNull(message = "User update time is required")
    @Column(
        name = "updated_at",
        nullable = false
    )
    private Instant updatedAt;

    protected User() {
        // Required by JPA.
    }

    public User(
        String name,
        UserStatus status
    ) {
        this.name = name;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }

        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
