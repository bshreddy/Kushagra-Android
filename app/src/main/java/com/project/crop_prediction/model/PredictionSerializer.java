package com.project.crop_prediction.model;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class PredictionSerializer implements JsonSerializer<Prediction> {

    private static final String TAG = "PredictionSerializer";

    @Override
    public JsonElement serialize(Prediction src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        Log.d(TAG, "serialize: ");

        JsonArray jsonConf = new JsonArray();
        for(int i = 0; i < src.confidences.length; i++)
            jsonConf.add(src.confidences[i]);
        jsonObject.add(Prediction.CodingKeys.confidences.rawValue, jsonConf);

        jsonObject.addProperty(Prediction.CodingKeys.predicted_idx.rawValue, src.predicted_idx);
        jsonObject.addProperty(Prediction.CodingKeys.kind.rawValue, src.kind.rawValue);

        return jsonObject;
    }

}
