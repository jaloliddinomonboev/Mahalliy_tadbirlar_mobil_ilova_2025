package com.example.myeventmanagement;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class FCMNotificationWorker extends Worker {
    public FCMNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String token = getInputData().getString("token");
        String title = getInputData().getString("title");
        String message = getInputData().getString("message");

        try {
            String jsonPayload = new JSONObject()
                    .put("to", token)
                    .put("notification", new JSONObject()
                            .put("title", title)
                            .put("body", message)
                    ).toString();

            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=AIzaSyBoAzzyPrWwy--nrWu8Ms1-iGlBfYwZ3mc"); // Server Key
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(jsonPayload.getBytes("UTF-8"));
            os.close();

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode == 200 ? Result.success() : Result.retry();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}