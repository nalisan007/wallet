package io.wallet.exception;

public final class ValidationMessages {

    private ValidationMessages() {
    }

    public static final String REQUIRED =
        "This field is required";

    public static final String POSITIVE_AMOUNT =
        "Amount must be greater than zero";

    public static final String NON_NEGATIVE_AMOUNT =
        "Amount cannot be negative";

    public static final String SELF_TRANSFER =
        "From wallet and to wallet must be different";

    public static final String INVALID_DATE_RANGE =
        "To date must be greater than or equal to from date";
}
