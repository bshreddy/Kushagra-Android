package com.project.crop_prediction.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CropDetailsDeserializer implements JsonDeserializer<CropDetails> {

    @Override
    public CropDetails deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonArray jsonProds = jsonObject.get(CodingKeys.majorProducers.rawValue).getAsJsonArray();
        String[] prods = new String[jsonProds.size()];

        for (int i = 0; i < jsonProds.size(); i++)
            prods[i] = jsonProds.get(i).getAsString();

        return new CropDetails((jsonObject.get(CodingKeys.typ.rawValue) != null) ? jsonObject.get(CodingKeys.typ.rawValue).getAsString() : null,
                (jsonObject.get(CodingKeys.techniquesUsed.rawValue) != null) ? jsonObject.get(CodingKeys.techniquesUsed.rawValue).getAsString() : null,
                (jsonObject.get(CodingKeys.varieties.rawValue) != null) ? jsonObject.get(CodingKeys.varieties.rawValue).getAsString() : null,
                (jsonObject.get(CodingKeys.temp.rawValue) != null) ? jsonObject.get(CodingKeys.temp.rawValue).getAsString() : null,
                (jsonObject.get(CodingKeys.rainfall.rawValue) != null) ? jsonObject.get(CodingKeys.rainfall.rawValue).getAsString() : null,
                (jsonObject.get(CodingKeys.soil.rawValue) != null) ? jsonObject.get(CodingKeys.soil.rawValue).getAsString() : null,
                prods);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    enum CodingKeys {
        typ("type"), techniquesUsed("tech"), varieties("vrts"),
        temp("temp"), rainfall("rain"), soil("soil"), majorProducers("prdcrs");

        public String rawValue;

        CodingKeys(String rawValue) {
            this.rawValue = rawValue;
        }
    }

}
