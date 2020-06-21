package com.project.crop_prediction.model;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class RecentSerializer implements JsonSerializer<Recent> {

    private static final String TAG = "RecentSerializer";

    @Override
    public JsonElement serialize(Recent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        Log.d(TAG, "serialize: ");

        jsonObject.add(Recent.CodingKeys.prediction.rawValue, context.serialize(src.prediction));
        jsonObject.addProperty(Recent.CodingKeys.bookmarked.rawValue, src.bookmarked);
        jsonObject.addProperty(Recent.CodingKeys.createdAt.rawValue, src.createdAt.getTime());
        jsonObject.add(Recent.CodingKeys.location.rawValue, context.serialize(src.coordinate));

        return jsonObject;
    }

}
