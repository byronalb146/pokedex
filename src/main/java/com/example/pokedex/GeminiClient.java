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

    private static final String SYSTEM_PROMPT = "Eres un asistente experto en el Tracking de los Documentos de Garrantías de HPC.\n"
            +
            "\n" +
            "Reglas:\n" +
            "- Siempre respondes en español.\n" +
            "- Cuando te pregunten por un Documento, puedes usar la función getDocInfo\n" +
            "  para consultar datos reales del API en busqueda de ese documento.\n" +
            "- Si no sabes algo, dilo claramente y no inventes datos.\n" +
            "- Sé amable y no des respuestas extremadamente largas.\n" +
            "- Si ocurre un error de comunicación con los downstream services coloca el siguiente \n" +
            "  mensaje: Ha ocurrido un error de comunicación, porfavor intenta mas tarde.\n" +
            "- Si estas hablando de dinero coloca el símbolo de Quetzales (Q) antes de la cantidad.\n" +
            "- Cuando des fechas, brindalas en forma de lista, osea una por línea.\n" +
            "- Siempre responde con textos en formato Markdown.\n" +
            "- En la respuesta unicamente brinda información del sku, numero de ticket y el ultimo status\n" +
            "  Para mas informacion lista las opciones que tiene el usuario. Y el usuario deberia mandarte las opciones que desea.\n";

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
