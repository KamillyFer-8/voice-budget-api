package com.voicebudget.infrastructure.api;

import com.voicebudget.infrastructure.api.dto.VoiceCommandResponse;
import com.voicebudget.infrastructure.audio.VoiceCommandOrchestrator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/transactions")
public class VoiceTransactionController {

    private final VoiceCommandOrchestrator voiceCommandOrchestrator;

    public VoiceTransactionController(
            VoiceCommandOrchestrator voiceCommandOrchestrator
    ) {
        this.voiceCommandOrchestrator = voiceCommandOrchestrator;
    }

    @PostMapping(
            value = "/voice",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public VoiceCommandResponse processVoiceCommand(
            @RequestParam("audio") MultipartFile audio
    ) {
        return voiceCommandOrchestrator.process(audio);
    }
}