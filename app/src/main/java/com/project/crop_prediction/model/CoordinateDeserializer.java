package com.project.crop_prediction.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CoordinateDeserializer implements JsonDeserializer<Coordinate> {

    @Override
    public Coordinate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        return new Coordinate(jsonObject.get(Coordinate.CodingKeys.lat.rawValue).getAsDouble(),
                jsonObject.get(Coordinate.CodingKeys.lon.rawValue).getAsDouble(),
                jsonObject.get(Coordinate.CodingKeys.alt.rawValue).getAsDouble());
    }
}
