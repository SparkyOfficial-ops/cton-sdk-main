package com.cton.tonconnect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * HTTP-міст TonConnect: POST /message і SSE GET /events.
 */
final class BridgeClient {
    private final OkHttpClient http;
    private final String bridgeUrl;

    BridgeClient(OkHttpClient http, String bridgeUrl) {
        this.http = http;
        String trimmed = bridgeUrl.endsWith("/") ? bridgeUrl.substring(0, bridgeUrl.length() - 1) : bridgeUrl;
        this.bridgeUrl = trimmed;
    }

    void send(String fromClientId, String toClientId, String encryptedBase64, int ttlSeconds) throws IOException {
        String url = bridgeUrl + "/message?client_id=" + fromClientId + "&to=" + toClientId + "&ttl=" + ttlSeconds;
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(encryptedBase64, MediaType.get("text/plain")))
            .build();
        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("TonConnect bridge rejected the message: " + response.code());
            }
        }
    }

    void listen(String clientId, String lastEventId, BiConsumer<String, String> onEvent, java.util.function.BooleanSupplier open)
            throws IOException {
        StringBuilder url = new StringBuilder(bridgeUrl).append("/events?client_id=").append(clientId);
        if (lastEventId != null && !lastEventId.isEmpty()) {
            url.append("&last_event_id=").append(lastEventId);
        }
        Request request = new Request.Builder()
            .url(url.toString())
            .header("Accept", "text/event-stream")
            .build();
        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("TonConnect bridge events failed: " + response.code());
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {
                String event = "message";
                String id = lastEventId;
                StringBuilder data = new StringBuilder();
                String line;
                while (open.getAsBoolean() && (line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        if (data.length() > 0) {
                            onEvent.accept(id, data.toString());
                        }
                        data.setLength(0);
                        event = "message";
                    } else if (line.startsWith("data:")) {
                        data.append(line.substring(5).trim());
                    } else if (line.startsWith("event:")) {
                        event = line.substring(6).trim();
                    } else if (line.startsWith("id:")) {
                        id = line.substring(3).trim();
                    }
                    if (!"message".equals(event) && data.length() > 0 && line.isEmpty()) {
                        data.setLength(0);
                    }
                }
            }
        }
    }
}
