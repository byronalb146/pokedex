package com.example.pokedex;

import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import kong.unirest.json.JSONArray;

public class PokemonService {

    /**
     * Función que Gemini puede llamar como herramienta.
     */
    public static String getPokemonInfo(String name) {
        try {
            JSONObject res = Unirest
                    .get("https://pokeapi.co/api/v2/pokemon/" + name.toLowerCase())
                    .asJson()
                    .getBody()
                    .getObject();

            String apiName = res.optString("name", name);

            System.out.println(apiName);

            int height = res.optInt("height", -1);
            int weight = res.optInt("weight", -1);

            StringBuilder tipos = new StringBuilder();
            JSONArray typesArray = res.optJSONArray("types");
            if (typesArray != null) {
                for (int i = 0; i < typesArray.length(); i++) {
                    JSONObject typeObj = typesArray.getJSONObject(i).getJSONObject("type");
                    if (i > 0) tipos.append(", ");
                    tipos.append(typeObj.optString("name", "desconocido"));
                }
            }

            return "Nombre: " + apiName + "\n" +
                   "Tipos: " + (tipos.length() > 0 ? tipos : "desconocido") + "\n" +
                   "Altura: " + (height >= 0 ? height : 0) + "\n" +
                   "Peso: " + (weight >= 0 ? weight : 0);
        } catch (Exception e) {
            return "No pude encontrar información para el Pokémon '" + name +
                   "'. Verifica el nombre (normalmente en inglés) e inténtalo de nuevo.";
        }
    }
}
