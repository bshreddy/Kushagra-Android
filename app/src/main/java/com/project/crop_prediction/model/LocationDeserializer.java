package com.project.crop_prediction.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class LocationDeserializer implements JsonDeserializer<Coordinate> {

    private enum CodingKeys {
        lat("lat"), lon("long"), alt("altitude");

        private String rawValue;

        CodingKeys(String rawValue) {
            this.rawValue = rawValue;
        }
    }

    @Override
    public Coordinate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        return new Coordinate(jsonObject.get(CodingKeys.lat.rawValue).getAsDouble(),
                jsonObject.get(CodingKeys.lon.rawValue).getAsDouble(),
                jsonObject.get(CodingKeys.alt.rawValue).getAsDouble());
    }
}
