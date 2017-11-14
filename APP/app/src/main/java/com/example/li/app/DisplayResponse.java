package com.example.li.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DisplayResponse extends AppCompatActivity {
    public static final String url = "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/analyze" +
            "?visualFeatures=Faces, Categories, Tags, Description&language=en&details=Landmarks";

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_response);
        final TextView textView = findViewById(R.id.textView);

//        mTextView.setText("Test");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String pictureURL = "https://www.spaceneedle.com/wp-content/uploads/2013/11/main_03-social.jpg";
            final String subscriptionKey = "5eb78facc92b4299bd4ebc3d55d5d234";


            JSONObject body = null;
            try {
                body = new JSONObject("{\"url\":\"https://www.spaceneedle.com/wp-content/uploads/2013/11/main_03-social.jpg\"}");
            } catch (JSONException e) {
                e.printStackTrace();
            }


            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, url, body,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                textView.setText("Success: " + response.toString(2));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            textView.setText("Something went wrong: " + error.networkResponse + "\n" + error.getStackTrace());
                        }
                    })
            {
                @Override
                protected Map<String,String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("visualFeatures", "Faces, Categories, Tags, Description");
                    params.put("language", "en");
                    params.put("details", "Landmarks");
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Ocp-Apim-Subscription-Key", subscriptionKey);
                    return headers;
                }
            };
// Add the request to the RequestQueue.

        queue.add(request);
    }






//    public static final String url = "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/analyze" +
//            "?visualFeatures=Faces, Categories, Tags, Description&language=en&details=Landmarks";
//
//    private static class RequestSender extends AsyncTask<String, Void, String> {
//
//
//        final String subscriptionKey = "5eb78facc92b4299bd4ebc3d55d5d234";
//
//        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//
//        OkHttpClient client = new OkHttpClient();
//
//        String post(String url, String json) throws IOException {
//            RequestBody body = RequestBody.create(JSON, json);
//            Request request = new Request.Builder()
//                    .url(url)
//                    .post(body)
//                    .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
//                    .addHeader("Content-Type", "application/json")
//                    .build();
//            try (Response response = client.newCall(request).execute()) {
//                return response.body().string();
//            }
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            String res = null;
//            try {
//                res = post(url, strings[0]);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return res;
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_display_response);
//        TextView textView = findViewById(R.id.textView);
//
//        Intent intent = getIntent();
//        String pictureURL = "https://www.spaceneedle.com/wp-content/uploads/2013/11/main_03-social.jpg";
//
//        try {
////            JSONObject jsonObject = new JSONObject();
////            jsonObject.put("url", pictureURL);
//            String req = "{\"url\":\"https://www.spaceneedle.com/wp-content/uploads/2013/11/main_03-social.jpg\"}";
//            String res = new RequestSender().execute(req);
//            Log.i("tag", res);
//            textView.setText(res);
//        } catch (Exception e) {
//            Log.i("error", e.toString());
//            textView.setText(e.getMessage());
//        }
//    }
}
