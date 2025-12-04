package com.example.pokedex;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;

import java.lang.reflect.Method;

public class GeminiClient {

    private static final String SYSTEM_PROMPT =
        "Eres un asistente experto en Pokémon.\n" +
        "\n" +
        "Reglas:\n" +
        "- Siempre respondes en español.\n" +
        "- Cuando te pregunten por un Pokémon, puedes usar la función getPokemonInfo\n" +
        "  para consultar datos reales de la PokeAPI.\n" +
        "- Si no sabes algo, dilo claramente y no inventes datos.\n" +
        "- Sé amable y no des respuestas extremadamente largas.\n";

    private final Client client;

    public GeminiClient() {
        this.client = Client.builder()
            .apiKey(System.getenv("GOOGLE_API_KEY"))
            .build();
    }

    public String chat(String userMessage) {
        try {
            // Instrucciones del sistema
            Content systemInstruction = Content.fromParts(
                    Part.fromText(SYSTEM_PROMPT)
            );

            // Método que Gemini puede invocar como herramienta
            Method getPokemonInfoMethod =
                    PokemonService.class.getMethod("getPokemonInfo", String.class);

            Tool pokemonTool = Tool.builder()
                    .functions(getPokemonInfoMethod)
                    .build();

            // Config con system + tool
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .tools(pokemonTool)
                    .build();

            // Llamada al modelo (AFC decide si llama o no a getPokemonInfo)
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    userMessage,
                    config
            );

            return response.text();

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return "Error interno configurando la herramienta de Pokémon.";
        }
    }
}
