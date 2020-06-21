package com.project.crop_prediction.model;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class CoordinateSerializer implements JsonSerializer<Coordinate> {

    private static final String TAG = "CoordinateSerializer";

    @Override
    public JsonElement serialize(Coordinate src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        Log.d(TAG, "serialize: ");

        jsonObject.addProperty(Coordinate.CodingKeys.lat.rawValue, src.lat);
        jsonObject.addProperty(Coordinate.CodingKeys.lon.rawValue, src.lon);
        jsonObject.addProperty(Coordinate.CodingKeys.alt.rawValue, src.alt);

        return jsonObject;
    }

}
