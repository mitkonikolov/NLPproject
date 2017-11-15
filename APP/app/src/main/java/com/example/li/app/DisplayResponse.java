package com.example.li.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DisplayResponse extends AppCompatActivity {
    final String subscriptionKey = "5eb78facc92b4299bd4ebc3d55d5d234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_response);
        final TextView textView = findViewById(R.id.textView);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String pictureURL = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("westcentralus.api.cognitive.microsoft.com")
                    .appendPath("vision")
                    .appendPath("v1.0")
                    .appendPath("analyze")
                    .appendQueryParameter("visualFeatures", "Faces, Categories, Tags, Description")
                    .appendQueryParameter("details", "Landmarks")
                    .appendQueryParameter("model", "landscape");
            //.appendQueryParameter("sort", "relevance")
            //.fragment("section-name");
            String myUrl = builder.build().toString();

            Map<String, String> body = new HashMap<>();
            body.put("url", pictureURL);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    myUrl,
                    new JSONObject(body),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                textView.setText("This picture shows: " +
                                        response.getJSONObject("description")
                                                .getJSONArray("captions")
                                                .getJSONObject(0)
                                                .getString("text"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            textView.setText("Something went wrong: " +
                                    error.networkResponse +
                                    "\n" + error.getStackTrace() +
                                    "\n" + error.toString());
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Ocp-Apim-Subscription-Key", subscriptionKey);
                    return headers;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(request);

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
