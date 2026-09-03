package io.wallet.entity;

public enum WalletStatus {

    ACTIVE,

    INACTIVE;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
