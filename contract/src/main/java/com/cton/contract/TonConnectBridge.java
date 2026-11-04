package com.cton.contract;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * TON Connect HTTP bridge. Messages are posted to {@code /message} and
 * wallet events are read from the {@code /events} SSE stream.
 */
public final class TonConnectBridge {
    public static final String DEFAULT_URL = "https://bridge.tonapi.io/bridge";

    private final String baseUrl;
    private final HttpClient http;

    public TonConnectBridge() {
        this(DEFAULT_URL);
    }

    public TonConnectBridge(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("bridge url is required");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    }

    public String url() {
        return baseUrl;
    }

    public void send(String fromClientId, String toClientId, String bodyBase64, int ttlSeconds) throws IOException {
        String url = baseUrl + "/message?client_id=" + encode(fromClientId)
                + "&to=" + encode(toClientId)
                + "&ttl=" + ttlSeconds;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(bodyBase64))
                .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IOException("bridge rejected the message: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("bridge request was interrupted", e);
        }
    }

    public InputStream events(String clientId) throws IOException {
        String url = baseUrl + "/events?client_id=" + encode(clientId);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Accept", "text/event-stream")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() / 100 != 2) {
                throw new IOException("bridge event stream status " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("bridge event stream was interrupted", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
