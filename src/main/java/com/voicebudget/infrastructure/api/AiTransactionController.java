
package com.voicebudget.infrastructure.api;

import com.voicebudget.infrastructure.api.dto.AiCommandRequest;
import com.voicebudget.infrastructure.api.dto.AiCommandResponse;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions/ai")
public class AiTransactionController {

    private final ChatClient chatClient;

    public AiTransactionController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping
    public AiCommandResponse processCommand(
            @Valid @RequestBody AiCommandRequest request
    ) {
        String response = chatClient.prompt()
                .user(request.message())
                .call()
                .content();

        return new AiCommandResponse(response);
    }
}
