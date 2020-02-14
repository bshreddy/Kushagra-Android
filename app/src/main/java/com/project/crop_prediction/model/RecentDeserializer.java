package com.project.crop_prediction.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Date;

public class RecentDeserializer implements JsonDeserializer<Recent> {

    private enum CodingKeys {
        prediction("pred"), createdAt("crtdAt"), bookmarked("bkmrkd"), location("loc");

        private String rawValue;

        CodingKeys(String rawValue) {
            this.rawValue = rawValue;
        }
    }

    @Override
    public Recent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Prediction.class, new PredictionDeserializer())
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .create();

        return new Recent(gson.fromJson(jsonObject.get(CodingKeys.prediction.rawValue).getAsJsonObject(), Prediction.class),
                jsonObject.get(CodingKeys.bookmarked.rawValue).getAsBoolean(),
                new Date(jsonObject.get(CodingKeys.createdAt.rawValue).getAsLong()),
                gson.fromJson(jsonObject.get(CodingKeys.location.rawValue).getAsJsonObject(), Location.class));
    }
}
