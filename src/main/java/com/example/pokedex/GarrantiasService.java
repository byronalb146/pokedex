package com.example.pokedex;

import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class GarrantiasService {

    private static final String API_URL =
            "https://warranty-tracker-yzqu.onrender.com/api/tracking/";

    private static final String API_KEY =
            System.getenv("WARRANTY_TRACKER_API_KEY");

    public static String getDocInfo(String docname) {
        try {
            if (API_KEY == null || API_KEY.isBlank()) {
                System.out.println("⚠️ WARNING: WARRANTY_TRACKER_API_KEY no está configurada.");
            }

            String body = Unirest
                    .get(API_URL + docname.toUpperCase())
                    .header("x-api-key", API_KEY)
                    .asString()
                    .getBody();

            // 🔍 DEBUG
            System.out.println("🔎 Request a: " + API_URL + docname.toUpperCase());
            System.out.println("🔎 API key presente: " + (API_KEY != null && !API_KEY.isBlank()));
            System.out.println("🔎 Respuesta API:");
            System.out.println(body);

            return body;
        } catch (UnirestException e) {
            e.printStackTrace();
            return "Error al consultar la API de garantías: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "Ocurrió un error inesperado al consultar la API de garantías.";
        }
    }
}
