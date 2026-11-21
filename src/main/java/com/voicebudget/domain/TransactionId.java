
package com.voicebudget.domain;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(UUID uuid) {

    public TransactionId {
        Objects.requireNonNull(uuid, "O UUID não pode ser nulo");
    }

    public TransactionId() {
        this(UUID.randomUUID());
    }
}
