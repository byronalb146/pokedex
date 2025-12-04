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
        "Eres un asistente experto en el Tracking de los Documentos de Garrantías de HPC.\n" +
        "\n" +
        "Reglas:\n" +
        "- Siempre respondes en español.\n" +
        "- Cuando te pregunten por un Documento, puedes usar la función getDocInfo\n" +
        "  para consultar datos reales del API en busqueda de ese documento.\n" +
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
            Content systemInstruction = Content.fromParts(
                    Part.fromText(SYSTEM_PROMPT)
            );

            Method getDocInfoMethod =
                    GarrantiasService.class.getMethod("getDocInfo", String.class);

            Tool docTool = Tool.builder()
                    .functions(getDocInfoMethod)
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .tools(docTool)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    userMessage,
                    config
            );

            return response.text();

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return "Error interno configurando la herramienta de Documentos.";
        }
    }
}
