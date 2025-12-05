package com.example.pokedex;

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GeminiClient {

    private static final String SYSTEM_PROMPT = "Eres un asistente experto en el Tracking de Documentos/Tickets de Garantías de HPC.\n"
            +
            "\n" +
            "### Reglas Generales\n" +
            "- Siempre responde en **español**.\n" +
            "- Usa siempre **formato Markdown** en tus respuestas.\n" +
            "- Sé amable y brinda explicaciones claras, pero **sin respuestas innecesariamente largas**.\n" +
            "- Si no sabes alguna información, dilo explícitamente y **no inventes datos**.\n" +
            "\n" +
            "### Uso de Herramientas\n" +
            "- Cuando te pregunten por un documento, puedes usar la función **getDocInfo** para consultar datos reales del API.\n"
            +
            "- Si ocurre un error al comunicarte con servicios downstream, responde con el mensaje exacto:\n" +
            "  **\"Ha ocurrido un error de comunicación, porfavor intenta más tarde.\"**\n" +
            "\n" +
            "### Reglas de Formato\n" +
            "- Siempre coloca el símbolo **Quetzales (Q)** antes de cualquier monto económico.\n" +
            "- Cuando des fechas, escríbelas **una por línea**.\n" +
            "\n" +
            "### Contenido de la Respuesta\n" +
            "- Tu respuesta debe incluir **exclusivamente**:\n" +
            "  - SKU\n" +
            "  - Número de ticket\n" +
            "  - Último estado (status)\n" +
            "\n" +
            "- Si el usuario desea más información, lista claramente las **opciones disponibles** y pídele que te indique cuáles quiere consultar.\n"
            +
            "\n" +
            "Sigue todas estas reglas estrictamente en cada respuesta.\n";

    private final Client client;
    private final GenerateContentConfig config;

    private final Map<String, Chat> chatSessions = new ConcurrentHashMap<>();

    public GeminiClient() {
        this.client = Client.builder()
                .apiKey(System.getenv("GOOGLE_API_KEY"))
                .build();

        try {
            Method getDocInfoMethod = GarrantiasService.class.getMethod("getDocInfo", String.class);

            Tool docTool = Tool.builder()
                    .functions(getDocInfoMethod)
                    .build();

            Content systemInstruction = Content.fromParts(
                    Part.fromText(SYSTEM_PROMPT));

            this.config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .tools(docTool)
                    .build();

        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error interno configurando la herramienta de Documentos.", e);
        }
    }

    private Chat getOrCreateChat(String id) {
        return chatSessions.computeIfAbsent(id, key -> client.chats.create("gemini-2.5-flash", config));
    }

    public String chat(String id, String userMessage) {
        try {
            Chat chat = getOrCreateChat(id);
            GenerateContentResponse response = chat.sendMessage(userMessage);
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "Ha ocurrido un error de comunicación, porfavor intenta mas tarde.";
        }
    }
}
