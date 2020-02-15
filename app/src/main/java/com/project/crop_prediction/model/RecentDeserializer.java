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

    @Override
    public Recent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Prediction.class, new PredictionDeserializer())
                .registerTypeAdapter(Coordinate.class, new CoordinateDeserializer())
                .create();

        return new Recent(gson.fromJson(jsonObject.get(Recent.CodingKeys.prediction.rawValue).getAsJsonObject(), Prediction.class),
                jsonObject.get(Recent.CodingKeys.bookmarked.rawValue).getAsBoolean(),
                new Date(jsonObject.get(Recent.CodingKeys.createdAt.rawValue).getAsLong()),
                gson.fromJson(jsonObject.get(Recent.CodingKeys.location.rawValue).getAsJsonObject(), Coordinate.class));
    }
}
