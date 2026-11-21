
package com.voicebudget.infrastructure.api.dto;

import com.voicebudget.domain.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotBlank(message = "A descrição é obrigatória")
        String description,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Digits(integer = 12, fraction = 2,
                message = "O valor deve ter no máximo duas casas decimais")
        BigDecimal amount,

        @NotNull(message = "A categoria é obrigatória")
        Category category

) {}
