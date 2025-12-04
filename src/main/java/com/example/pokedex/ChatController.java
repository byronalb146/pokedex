package com.example.pokedex;

import com.example.pokedex.dto.ChatRequest;
import com.example.pokedex.dto.ChatResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final GeminiClient geminiClient;

    public ChatController() {
        this.geminiClient = new GeminiClient();
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = geminiClient.chat(request.getMessage());
        return new ChatResponse(reply);
    }
}
