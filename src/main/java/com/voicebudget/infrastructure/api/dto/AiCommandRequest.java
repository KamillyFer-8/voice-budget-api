
package com.voicebudget.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AiCommandRequest(

        @NotBlank(message = "O comando não pode estar vazio")
        String message

) {}
