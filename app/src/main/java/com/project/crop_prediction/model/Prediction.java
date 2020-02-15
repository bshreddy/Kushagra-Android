package com.project.crop_prediction.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.project.crop_prediction.network.VolleyMultipartRequest;
import com.project.crop_prediction.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Prediction implements Parcelable {

    private static String cropUrl = "http://192.168.1.10:8000/crop";
    private static String diseaseUrl = "http://192.168.1.10:8000/disease";
    private static String cropClasses[] = {"coffee","cotton","jute","maize","millet","rice","sugarcane","tea","tomato","wheat"};
    private static String diseaseClasses[] = {"Apple - Apple scab", "Apple - Black rot", "Apple - Cedar apple rust", "Apple - Healthy",
            "Blueberry - Healthy", "Cherry (including sour) - Powdery mildew", "Cherry (including sour) - healthy",
            "Corn - Cercospora leaf spot Gray leaf spot", "Corn - Common rust ", "Corn - Northern Leaf Blight", "Corn - Healthy",
            "Grape - Black rot", "Grape - Esca (Black Measles)", "Grape - Leaf blight (Isariopsis Leaf Spot)", "Grape - Healthy",
            "Orange - Haunglongbing (Citrus greening)", "Peach - Bacterial spot", "Peach - Healthy", "Pepper, bell - Bacterial spot",
            "Pepper, bell - Healthy", "Potato - Early blight", "Potato - Late blight", "Potato - Healthy", "Raspberry - Healthy",
            "Soybean - Healthy", "Squash - Powdery mildew", "Strawberry - Leaf scorch", "Strawberry - healthy", "Tomato - Bacterial spot",
            "Tomato - Early blight", "Tomato - Late blight", "Tomato - Leaf Mold", "Tomato - Septoria leaf spot",
            "Tomato - Spider mites, Two-spotted spider mite", "Tomato - Target Spot", "Tomato - Tomato Yellow Leaf Curl Virus",
            "Tomato - Tomato mosaic virus", "Tomato - Healthy"};

    static enum CodingKeys {
        predicted_idx("pred"), confidences("cnf"), kind("kind");

        public String rawValue;

        CodingKeys(String rawValue) {
            this.rawValue = rawValue;
        }
    }

    public interface PredictionListener {
        void onCropPrediction(Prediction prediction);
    }

    public enum Kind {
        crop("crop"), disease("disease");

        public String rawValue;

        Kind(String rawValue) {
            this.rawValue = rawValue;
        }

        public String capitalized() {
            return rawValue.substring(0, 1).toUpperCase() + rawValue.substring(1);
        }
    }

    public Bitmap image;
    public double confidences[];
    public int predicted_idx;
    public Kind kind;
    public String classes[];

    protected Prediction(Parcel in) {
        image = in.readParcelable(Bitmap.class.getClassLoader());
        confidences = in.createDoubleArray();
        predicted_idx = in.readInt();
        classes = in.createStringArray();
        kind = (Kind) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(image, flags);
        dest.writeDoubleArray(confidences);
        dest.writeInt(predicted_idx);
        dest.writeStringArray(classes);
        dest.writeSerializable(kind);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Prediction> CREATOR = new Creator<Prediction>() {
        @Override
        public Prediction createFromParcel(Parcel in) {
            return new Prediction(in);
        }

        @Override
        public Prediction[] newArray(int size) {
            return new Prediction[size];
        }
    };

    public static void predict(Context context, Kind kind, final Bitmap bitmap, final PredictionListener callback) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        final byte[] imageBytes = outputStream.toByteArray();

        String url = (kind == Kind.crop) ? cropUrl : diseaseUrl;

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
//                            JSONObject obj = new JSONObject(new String(response.data));
//                            int pred = obj.getInt("pred");
//                            Kind kind = (obj.getString("kind").equalsIgnoreCase("crop")) ? Kind.crop : Kind.disease;
//
//                            JSONArray cnf = obj.getJSONArray("cnf");
//                            double confidences[] = new double[10];
//
//                            for(int i = 0; i < 10; i++)
//                                confidences[i] = cnf.getInt(i);
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(Prediction.class, new PredictionDeserializer())
                                .create();

                        Prediction prediction = gson.fromJson(new String(response.data), Prediction.class);
                        prediction.image = bitmap;
                        callback.onCropPrediction(prediction);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                        callback.onCropPrediction(null);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("img", new DataPart("crop.jpg", imageBytes));

                return params;
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public Prediction(int predicted_idx, double confidences[], Kind kind) {
        this.predicted_idx = predicted_idx;
        this.confidences = confidences;
        this.kind = kind;
        this.classes = (kind == Kind.crop) ? cropClasses : diseaseClasses;
    }

    public String getPredictedClass() {
        return classes[predicted_idx];
    }

    public String getPredictedName() {
        String pClass = getPredictedClass();
        return pClass.substring(0, 1).toUpperCase() + pClass.substring(1);
    }

    @NonNull
    @Override
    public String toString() {
        String confString = "{";
        for(int i = 0; i < confidences.length; i++)
            confString += confidences[i] + ", ";
        confString += "\b\b}";

        return "Prediction(predicted_idx: " + predicted_idx + ", confidences" + confString + ", kind: " + kind.rawValue + ")";
    }
}
