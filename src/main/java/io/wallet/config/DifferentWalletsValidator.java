package io.wallet.config;

import io.wallet.entity.TransferRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DifferentWalletsValidator
        implements ConstraintValidator<DifferentWallets, TransferRequest> {

    @Override
    public boolean isValid(
        TransferRequest request,
        ConstraintValidatorContext context
    ) {
        if (request == null) {
            return true;
        }

        if (request.fromWalletId() == null
            || request.toWalletId() == null) {
            return true;
        }

        return !request.fromWalletId().equals(request.toWalletId());
    }
}
