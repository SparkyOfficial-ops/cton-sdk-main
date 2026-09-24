// TonLiteClient.java - реалізація клієнта для LiteServer
// Author: Андрій Будильников (Sparky)
// Implementation of client for LiteServer
// Реализация клиента для LiteServer

package com.cton.api.liteclient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Реалізація клієнта для прямого з'єднання з TON LiteServer
 * 
 * Implementation of client for direct connection to TON LiteServer
 * Реализация клиента для прямого соединения с TON LiteServer
 */
public class TonLiteClient implements LiteClient {
    private final OkHttpClient httpClient;
    private WebSocket webSocket;
    private String baseUrl;
    private boolean connected;
    private final Object lock = new Object();
    
    /**
     * Конструктор
     */
    public TonLiteClient() {
        this.httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build();
        this.connected = false;
    }
    
    @Override
    public void connect(String host, int port, String publicKeyBase64) throws IOException {
        // Формуємо URL для підключення
        // Формируем URL для подключения
        // Build URL for connection
        this.baseUrl = "ws://" + host + ":" + port + "/ws";
        this.connected = true;
        
        // Встановлюємо WebSocket з'єднання
        // Устанавливаем WebSocket соединение
        // Establish WebSocket connection
        Request request = new Request.Builder()
            .url(baseUrl)
            .build();
            
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                synchronized (lock) {
                    connected = true;
                }
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                synchronized (lock) {
                    connected = false;
                }
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                synchronized (lock) {
                    connected = false;
                }
            }
        };
        
        this.webSocket = httpClient.newWebSocket(request, listener);
    }
    
    @Override
    public void disconnect() {
        if (this.webSocket != null) {
            this.webSocket.close(1000, "Client disconnect");
        }
        this.connected = false;
        // Закриваємо WebSocket з'єднання
        // Закрываем WebSocket соединение
        // Close WebSocket connection
    }
    
    @Override
    public boolean isConnected() {
        return this.connected;
    }
    
    @Override
    public JsonObject runGetMethod(String address, String method, JsonObject stack) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to LiteServer");
        }
        
        // Формуємо URL запиту
        // Формируем URL запроса
        // Build request URL
        String url = baseUrl.replace("/ws", "") + "/runGetMethod";
        
        // Створюємо JSON тіло запиту
        // Создаем JSON тело запроса
        // Create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("address", address);
        requestBody.addProperty("method", method);
        requestBody.add("stack", stack);
        
        // Створюємо запит
        // Создаем запрос
        // Create request
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
            .build();
        
        // Виконуємо запит
        // Выполняем запрос
        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            
            // Парсимо JSON відповідь
            // Парсим JSON ответ
            // Parse JSON response
            String responseBody = body.string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
    
    @Override
    public CompletableFuture<JsonObject> runGetMethodAsync(String address, String method, JsonObject stack) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return runGetMethod(address, method, stack);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    @Override
    public JsonObject getBlockHeader(int workchain, long shard, long seqno) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to LiteServer");
        }
        
        // Формуємо URL запиту
        // Формируем URL запроса
        // Build request URL
        String url = baseUrl.replace("/ws", "") + "/getBlockHeader";
        
        // Створюємо JSON тіло запиту
        // Создаем JSON тело запроса
        // Create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("workchain", workchain);
        requestBody.addProperty("shard", shard);
        requestBody.addProperty("seqno", seqno);
        
        // Створюємо запит
        // Создаем запрос
        // Create request
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
            .build();
        
        // Виконуємо запит
        // Выполняем запрос
        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            
            // Парсимо JSON відповідь
            // Парсим JSON ответ
            // Parse JSON response
            String responseBody = body.string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
    
    @Override
    public JsonObject getBlockTransactions(int workchain, long shard, long seqno) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to LiteServer");
        }
        
        // Формуємо URL запиту
        // Формируем URL запроса
        // Build request URL
        String url = baseUrl.replace("/ws", "") + "/getBlockTransactions";
        
        // Створюємо JSON тіло запиту
        // Создаем JSON тело запроса
        // Create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("workchain", workchain);
        requestBody.addProperty("shard", shard);
        requestBody.addProperty("seqno", seqno);
        
        // Створюємо запит
        // Создаем запрос
        // Create request
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
            .build();
        
        // Виконуємо запит
        // Выполняем запрос
        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            
            // Парсимо JSON відповідь
            // Парсим JSON ответ
            // Parse JSON response
            String responseBody = body.string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
    
    @Override
    public JsonObject sendExternalMessage(String messageBoc) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to LiteServer");
        }
        
        // Формуємо URL запиту
        // Формируем URL запроса
        // Build request URL
        String url = baseUrl.replace("/ws", "") + "/sendExternalMessage";
        
        // Створюємо JSON тіло запиту
        // Создаем JSON тело запроса
        // Create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("boc", messageBoc);
        
        // Створюємо запит
        // Создаем запрос
        // Create request
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
            .build();
        
        // Виконуємо запит
        // Выполняем запрос
        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            
            // Парсимо JSON відповідь
            // Парсим JSON ответ
            // Parse JSON response
            String responseBody = body.string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
}