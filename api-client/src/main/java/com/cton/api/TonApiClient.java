// TonApiClient.java - HTTP клієнт для TON Center API
// Author: Андрій Будильников (Sparky)
// HTTP client for TON Center API
// HTTP клиент для TON Center API

package com.cton.api;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cton.api.dto.AddressInformation;
import com.cton.api.dto.BlockHeader;
import com.cton.api.dto.BlockTransactions;
import com.cton.api.dto.SendBocResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Client for interacting with TON Center API
 */
public class TonApiClient {
    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;
    private final Gson gson;
    private final ExecutorService executorService;
    
    /**
     * Constructor
     * @param baseUrl base API URL
     */
    public TonApiClient(String baseUrl) {
        this(baseUrl, null);
    }
    
    /**
     * Constructor with API key
     * @param baseUrl base API URL
     * @param apiKey API key (if needed)
     */
    public TonApiClient(String baseUrl, String apiKey) {
        this.httpClient = new OkHttpClient();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.apiKey = apiKey;
        this.gson = new Gson();
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * Execute HTTP request
     * @param request HTTP request
     * @return JsonObject with response
     * @throws IOException if network error occurred
     */
    private JsonObject executeRequest(Request request) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            
            // парсимо JSON відповідь
            // парсим JSON ответ
            // parse JSON response
            String responseBody = body.string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
    
    /**
     * Execute HTTP request and deserialize to specified type
     * @param request HTTP request
     * @param clazz class for deserialization
     * @return object of specified type
     * @throws IOException if network error occurred
     */
    private <T> T executeRequest(Request request, Class<T> clazz) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            
            // парсимо JSON відповідь
            // парсим JSON ответ
            // parse JSON response
            String responseBody = body.string();
            return gson.fromJson(responseBody, clazz);
        }
    }
    
    /**
     * Create request with API key
     * @param url request URL
     * @param method HTTP method
     * @param requestBody request body (for POST requests)
     * @return Request object
     */
    private Request createRequest(String url, String method, RequestBody requestBody) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        
        if ("POST".equals(method)) {
            requestBuilder.post(requestBody);
        } else {
            requestBuilder.get();
        }
            
        // додаємо API ключ якщо потрібно
        // добавляем API ключ если нужно
        // add API key if needed
        if (apiKey != null && !apiKey.isEmpty()) {
            requestBuilder.addHeader("X-API-Key", apiKey);
        }
        
        return requestBuilder.build();
    }
    
    /**
     * Get address information
     * @param address address in raw format (e.g., 0:1234...)
     * @return address information in JSON format
     * @throws IOException if network error occurred
     */
    public JsonObject getAddressInformation(String address) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "getAddressInformation?address=" + address;
        
        Request request = createRequest(url, "GET", null);
        return executeRequest(request);
    }
    
    /**
     * Get address information as DTO
     * @param address address in raw format (e.g., 0:1234...)
     * @return address information as DTO
     * @throws IOException if network error occurred
     */
    public AddressInformation getAddressInformationDto(String address) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "getAddressInformation?address=" + address;
        
        Request request = createRequest(url, "GET", null);
        return executeRequest(request, AddressInformation.class);
    }
    
    /**
     * Asynchronously get address information
     * @param address address in raw format (e.g., 0:1234...)
     * @return CompletableFuture with address information
     */
    public CompletableFuture<JsonObject> getAddressInformationAsync(String address) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAddressInformation(address);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Asynchronously get address information as DTO
     * @param address address in raw format (e.g., 0:1234...)
     * @return CompletableFuture with address information as DTO
     */
    public CompletableFuture<AddressInformation> getAddressInformationDtoAsync(String address) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAddressInformationDto(address);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Виконати get-метод смарт-контракту
     * @param address адреса контракту у форматі raw
     * @param method назва методу
     * @param stack параметри методу
     * @return результат виконання методу
     * @throws IOException якщо сталася помилка мережі
     */
    public JsonObject runGetMethod(String address, String method, JsonObject stack) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "runGetMethod";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("address", address);
        requestBody.addProperty("method", method);
        requestBody.add("stack", stack);
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request);
    }
    
    /**
     * Виконати get-метод смарт-контракту
     * @param address адреса контракту у форматі raw
     * @param method назва методу
     * @param stack параметри методу
     * @return результат виконання методу
     * @throws IOException якщо сталася помилка мережі
     */
    public <T> T runGetMethod(String address, String method, JsonObject stack, Class<T> resultClass) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "runGetMethod";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("address", address);
        requestBody.addProperty("method", method);
        requestBody.add("stack", stack);
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request, resultClass);
    }
    
    /**
     * Асинхронно виконати get-метод смарт-контракту
     * @param address адреса контракту у форматі raw
     * @param method назва методу
     * @param stack параметри методу
     * @return CompletableFuture з результатом виконання методу
     */
    public CompletableFuture<JsonObject> runGetMethodAsync(String address, String method, JsonObject stack) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return runGetMethod(address, method, stack);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Асинхронно виконати get-метод смарт-контракту
     * @param address адреса контракту у форматі raw
     * @param method назва методу
     * @param stack параметри методу
     * @param resultClass клас для десеріалізації результату
     * @return CompletableFuture з результатом виконання методу
     */
    public <T> CompletableFuture<T> runGetMethodAsync(String address, String method, JsonObject stack, Class<T> resultClass) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return runGetMethod(address, method, stack, resultClass);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Отримати інформацію про блок
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return інформація про блок
     * @throws IOException якщо сталася помилка мережі
     */
    public JsonObject getBlockHeader(int workchain, long shard, long seqno) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "getBlockHeader";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("workchain", workchain);
        requestBody.addProperty("shard", shard);
        requestBody.addProperty("seqno", seqno);
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request);
    }
    
    /**
     * Отримати інформацію про блок у вигляді DTO
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return інформація про блок у вигляді DTO
     * @throws IOException якщо сталася помилка мережі
     */
    public BlockHeader getBlockHeaderDto(int workchain, long shard, long seqno) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "getBlockHeader";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("workchain", workchain);
        requestBody.addProperty("shard", shard);
        requestBody.addProperty("seqno", seqno);
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request, BlockHeader.class);
    }
    
    /**
     * Асинхронно отримати інформацію про блок
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return CompletableFuture з інформацією про блок
     */
    public CompletableFuture<JsonObject> getBlockHeaderAsync(int workchain, long shard, long seqno) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBlockHeader(workchain, shard, seqno);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Асинхронно отримати інформацію про блок у вигляді DTO
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return CompletableFuture з інформацією про блок у вигляді DTO
     */
    public CompletableFuture<BlockHeader> getBlockHeaderDtoAsync(int workchain, long shard, long seqno) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBlockHeaderDto(workchain, shard, seqno);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Отримати транзакції з блоку
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return транзакції з блоку
     * @throws IOException якщо сталася помилка мережі
     */
    public JsonObject getBlockTransactions(int workchain, long shard, long seqno) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "getBlockTransactions";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("workchain", workchain);
        requestBody.addProperty("shard", shard);
        requestBody.addProperty("seqno", seqno);
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request);
    }
    
    /**
     * Отримати транзакції з блоку у вигляді DTO
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return транзакції з блоку у вигляді DTO
     * @throws IOException якщо сталася помилка мережі
     */
    public BlockTransactions getBlockTransactionsDto(int workchain, long shard, long seqno) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "getBlockTransactions";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("workchain", workchain);
        requestBody.addProperty("shard", shard);
        requestBody.addProperty("seqno", seqno);
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request, BlockTransactions.class);
    }
    
    /**
     * Асинхронно отримати транзакції з блоку
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return CompletableFuture з транзакціями з блоку
     */
    public CompletableFuture<JsonObject> getBlockTransactionsAsync(int workchain, long shard, long seqno) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBlockTransactions(workchain, shard, seqno);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Асинхронно отримати транзакції з блоку у вигляді DTO
     * @param workchain робочий ланцюг
     * @param shard шард
     * @param seqno номер послідовності
     * @return CompletableFuture з транзакціями з блоку у вигляді DTO
     */
    public CompletableFuture<BlockTransactions> getBlockTransactionsDtoAsync(int workchain, long shard, long seqno) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBlockTransactionsDto(workchain, shard, seqno);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Надіслати BOC в мережу
     * @param bocBytes байти BOC
     * @return результат операції у форматі JSON
     * @throws IOException якщо сталася помилка мережі
     */
    public JsonObject sendBoc(byte[] bocBytes) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "sendBoc";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("boc", bytesToBase64(bocBytes));
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request);
    }
    
    /**
     * Надіслати BOC в мережу у вигляді DTO
     * @param bocBytes байти BOC
     * @return результат операції у вигляді DTO
     * @throws IOException якщо сталася помилка мережі
     */
    public SendBocResult sendBocDto(byte[] bocBytes) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "sendBoc";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("boc", bytesToBase64(bocBytes));
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request, SendBocResult.class);
    }
    
    /**
     * Асинхронно надіслати BOC в мережу
     * @param bocBytes байти BOC
     * @return CompletableFuture з результатом операції
     */
    public CompletableFuture<JsonObject> sendBocAsync(byte[] bocBytes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendBoc(bocBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Асинхронно надіслати BOC в мережу у вигляді DTO
     * @param bocBytes байти BOC
     * @return CompletableFuture з результатом операції у вигляді DTO
     */
    public CompletableFuture<SendBocResult> sendBocDtoAsync(byte[] bocBytes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendBocDto(bocBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Надіслати BOC в мережу у форматі Base64
     * @param bocBase64 BOC у форматі Base64
     * @return результат операції у форматі JSON
     * @throws IOException якщо сталася помилка мережі
     */
    public JsonObject sendBoc(String bocBase64) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "sendBoc";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("boc", bocBase64);
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request);
    }
    
    /**
     * Надіслати BOC в мережу у форматі Base64 у вигляді DTO
     * @param bocBase64 BOC у форматі Base64
     * @return результат операції у вигляді DTO
     * @throws IOException якщо сталася помилка мережі
     */
    public SendBocResult sendBocDto(String bocBase64) throws IOException {
        // формуємо URL запиту
        // формируем URL запроса
        // build request URL
        String url = baseUrl + "sendBoc";
        
        // створюємо JSON тіло запиту
        // создаем JSON тело запроса
        // create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("boc", bocBase64);
        
        Request request = createRequest(url, "POST", 
            RequestBody.create(requestBody.toString(), MediaType.get("application/json")));
        return executeRequest(request, SendBocResult.class);
    }
    
    /**
     * Асинхронно надіслати BOC в мережу у форматі Base64
     * @param bocBase64 BOC у форматі Base64
     * @return CompletableFuture з результатом операції
     */
    public CompletableFuture<JsonObject> sendBocAsync(String bocBase64) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        // Execute in a separate thread
        new Thread(() -> {
            try {
                future.complete(sendBoc(bocBase64));
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        }).start();
        return future;
    }
    
    /**
     * Асинхронно надіслати BOC в мережу у форматі Base64 у вигляді DTO
     * @param bocBase64 BOC у форматі Base64
     * @return CompletableFuture з результатом операції у вигляді DTO
     */
    public CompletableFuture<SendBocResult> sendBocDtoAsync(String bocBase64) {
        CompletableFuture<SendBocResult> future = new CompletableFuture<>();
        // Execute in a separate thread
        new Thread(() -> {
            try {
                future.complete(sendBocDto(bocBase64));
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        }).start();
        return future;
    }
    
    /**
     * Конвертує байти в Base64 строку
     * @param bytes байти для конвертації
     * @return Base64 строка
     */
    private String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    /**
     * Закрити HTTP клієнт
     */
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        executorService.shutdown();
    }
}