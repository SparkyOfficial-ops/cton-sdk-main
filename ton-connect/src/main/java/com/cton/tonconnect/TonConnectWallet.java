package com.cton.tonconnect;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cton.contract.BaseWallet;
import com.cton.contract.OutgoingTransfer;
import com.cton.contract.StateInit;
import com.cton.sdk.Address;
import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.Crypto;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Гаманець TonConnect 2.0: підключається до HTTP-мосту по SSE, підписує
 * sendTransaction через BaseWallet і відправляє BOC у мережу.
 */
public final class TonConnectWallet implements Closeable {
    public static final String DEFAULT_BRIDGE = "https://bridge.tonapi.io/bridge";

    private final BaseWallet wallet;
    private final Crypto.PrivateKey privateKey;
    private final OkHttpClient http;
    private final String bridgeUrl;
    private final String network;
    private final SessionCrypto session = new SessionCrypto();
    private final AtomicBoolean open = new AtomicBoolean(false);
    private volatile Thread listener;
    private volatile String dappClientId;

    public TonConnectWallet(BaseWallet wallet, Crypto.PrivateKey privateKey) {
        this(wallet, privateKey, DEFAULT_BRIDGE, "-239");
    }

    public TonConnectWallet(BaseWallet wallet, Crypto.PrivateKey privateKey, String bridgeUrl, String network) {
        if (wallet == null || privateKey == null) {
            throw new IllegalArgumentException("Wallet and private key are required");
        }
        this.wallet = wallet;
        this.privateKey = privateKey;
        this.bridgeUrl = bridgeUrl == null || bridgeUrl.isEmpty() ? DEFAULT_BRIDGE : bridgeUrl;
        this.network = network == null ? "-239" : network;
        this.http = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    public String walletClientId() {
        return session.clientId();
    }

    /**
     * Відповісти на посилання dApp і слухати міст, поки сесію не закриють.
     */
    public void connect(String connectLink) throws IOException {
        ConnectLink link = ConnectLink.parse(connectLink);
        String bridge = link.bridgeUrl == null || link.bridgeUrl.isEmpty() ? bridgeUrl : link.bridgeUrl;
        BridgeClient bridgeClient = new BridgeClient(http, bridge);
        JsonObject request = resolveRequest(link.requestJson);
        dappClientId = link.dappClientId;
        open.set(true);
        bridgeClient.send(session.clientId(), dappClientId, session.encrypt(connectEvent(request), SessionCrypto.hexToBytes(dappClientId)), 300);
        listener = new Thread(() -> listen(bridgeClient), "ton-connect");
        listener.setDaemon(true);
        listener.start();
    }

    @Override
    public void close() {
        open.set(false);
        Thread current = listener;
        if (current != null) {
            current.interrupt();
        }
        http.dispatcher().executorService().shutdown();
        http.connectionPool().evictAll();
    }

    private void listen(BridgeClient bridgeClient) {
        String lastEventId = null;
        while (open.get()) {
            try {
                final String[] cursor = new String[] { lastEventId };
                bridgeClient.listen(session.clientId(), lastEventId, (id, data) -> {
                    if (id != null) {
                        cursor[0] = id;
                    }
                    handleEvent(bridgeClient, data);
                }, open::get);
                lastEventId = cursor[0];
            } catch (IOException e) {
                if (!open.get()) {
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void handleEvent(BridgeClient bridgeClient, String data) {
        JsonObject event = JsonParser.parseString(data).getAsJsonObject();
        if (!event.has("from") || !event.has("message")) {
            return;
        }
        String from = event.get("from").getAsString();
        String plain;
        try {
            plain = session.decrypt(event.get("message").getAsString(), SessionCrypto.hexToBytes(from));
        } catch (RuntimeException e) {
            return;
        }
        JsonObject rpc = JsonParser.parseString(plain).getAsJsonObject();
        if (!rpc.has("method")) {
            return;
        }
        String id = rpc.has("id") ? rpc.get("id").getAsString() : "0";
        try {
            String result = dispatch(rpc.get("method").getAsString(), rpc.get("params"));
            reply(bridgeClient, from, resultReply(id, result));
        } catch (Exception e) {
            reply(bridgeClient, from, errorReply(id, 1, e.getMessage()));
        }
    }

    private String dispatch(String method, JsonElement params) throws IOException {
        if ("disconnect".equals(method)) {
            open.set(false);
            return "";
        }
        if (!"sendTransaction".equals(method)) {
            throw new IOException("Method is not supported: " + method);
        }
        JsonObject request = transactionRequest(params);
        if (request.has("valid_until")) {
            long validUntil = request.get("valid_until").getAsLong();
            if (validUntil < System.currentTimeMillis() / 1000) {
                throw new IOException("sendTransaction valid_until is in the past");
            }
        }
        if (request.has("from") && !sameAddress(request.get("from").getAsString())) {
            throw new IOException("sendTransaction from does not match this wallet");
        }
        List<OutgoingTransfer> transfers = new ArrayList<>();
        for (JsonElement element : request.getAsJsonArray("messages")) {
            transfers.add(toTransfer(element.getAsJsonObject()));
        }
        Long validUntil = request.has("valid_until") ? request.get("valid_until").getAsLong() : null;
        return wallet.sendTransfers(transfers, privateKey, validUntil);
    }

    private OutgoingTransfer toTransfer(JsonObject message) {
        Address destination = new Address(message.get("address").getAsString());
        java.math.BigInteger amount = new java.math.BigInteger(message.get("amount").getAsString());
        Cell body = message.has("payload") && !message.get("payload").isJsonNull()
            ? cellFromBase64(message.get("payload").getAsString()) : null;
        Cell stateInit = message.has("stateInit") && !message.get("stateInit").isJsonNull()
            ? cellFromBase64(message.get("stateInit").getAsString()) : null;
        return new OutgoingTransfer(destination, amount, body, stateInit, BaseWallet.SEND_MODE_PAY_GAS_SEPARATELY, true);
    }

    private static Cell cellFromBase64(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        try (Boc boc = Boc.deserialize(bytes)) {
            return boc.getRoot();
        }
    }

    private boolean sameAddress(String rawOrFriendly) {
        return new Address(rawOrFriendly).toRaw().equals(wallet.getAddress().toRaw());
    }

    private String connectEvent(JsonObject request) throws IOException {
        JsonObject event = new JsonObject();
        event.addProperty("event", "connect");
        event.addProperty("id", 0);
        JsonObject payload = new JsonObject();
        JsonArray items = new JsonArray();
        String manifestUrl = request.get("manifestUrl").getAsString();
        String domain = domainOf(manifestUrl);
        Crypto.PublicKey key = privateKey.getPublicKey();
        byte[] publicKey;
        try {
            publicKey = key.getData();
        } finally {
            key.close();
        }
        for (JsonElement element : request.getAsJsonArray("items")) {
            String name = element.getAsJsonObject().get("name").getAsString();
            if ("ton_addr".equals(name)) {
                items.add(addressItem(publicKey));
            } else if ("ton_proof".equals(name)) {
                items.add(proofItem(element.getAsJsonObject(), domain));
            }
        }
        payload.add("items", items);
        payload.add("device", device());
        event.add("payload", payload);
        return event.toString();
    }

    private JsonObject addressItem(byte[] publicKey) throws IOException {
        JsonObject item = new JsonObject();
        item.addProperty("name", "ton_addr");
        item.addProperty("address", wallet.getAddress().toRaw());
        item.addProperty("network", network);
        item.addProperty("publicKey", hex(publicKey));
        StateInit init = StateInit.forWallet(wallet.getVersion(), wallet.getSubwalletId(), publicKey);
        try (Boc boc = new Boc(init.toCell())) {
            item.addProperty("walletStateInit", Base64.getEncoder().encodeToString(boc.serialize(true, true)));
        }
        return item;
    }

    private JsonObject proofItem(JsonObject request, String domain) {
        String payload = request.get("payload").getAsString();
        long timestamp = System.currentTimeMillis() / 1000;
        byte[] signature = TonProof.sign(privateKey, wallet.getAddress(), domain, timestamp, payload);
        JsonObject proof = new JsonObject();
        proof.addProperty("timestamp", timestamp);
        JsonObject domainJson = new JsonObject();
        domainJson.addProperty("lengthBytes", domain.getBytes(StandardCharsets.UTF_8).length);
        domainJson.addProperty("value", domain);
        proof.add("domain", domainJson);
        proof.addProperty("payload", payload);
        proof.addProperty("signature", Base64.getEncoder().encodeToString(signature));
        JsonObject item = new JsonObject();
        item.addProperty("name", "ton_proof");
        item.add("proof", proof);
        return item;
    }

    private JsonObject device() {
        JsonObject device = new JsonObject();
        device.addProperty("platform", "windows");
        device.addProperty("appName", "cton-sdk");
        device.addProperty("appVersion", "0.1.0");
        device.addProperty("maxProtocolVersion", 2);
        JsonArray features = new JsonArray();
        JsonObject sendTransaction = new JsonObject();
        sendTransaction.addProperty("name", "SendTransaction");
        sendTransaction.addProperty("maxMessages", 4);
        features.add(sendTransaction);
        device.add("features", features);
        return device;
    }

    private void reply(BridgeClient bridgeClient, String to, String json) {
        try {
            bridgeClient.send(session.clientId(), to, session.encrypt(json, SessionCrypto.hexToBytes(to)), 300);
        } catch (IOException ignored) {
            // Наступна подія мосту прийде окремим SSE-повідомленням.
        }
    }

    private static String resultReply(String id, String boc) {
        JsonObject reply = new JsonObject();
        reply.addProperty("id", id);
        reply.addProperty("result", boc);
        return reply.toString();
    }

    private static String errorReply(String id, int code, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message == null ? "" : message);
        JsonObject reply = new JsonObject();
        reply.addProperty("id", id);
        reply.add("error", error);
        return reply.toString();
    }

    private JsonObject resolveRequest(String request) throws IOException {
        String json = request;
        if (request.startsWith("http://") || request.startsWith("https://")) {
            Request httpRequest = new Request.Builder().url(request).build();
            try (Response response = http.newCall(httpRequest).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("Failed to load TonConnect request: " + response.code());
                }
                json = response.body().string();
            }
        }
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static JsonObject transactionRequest(JsonElement params) {
        if (params == null || !params.isJsonArray() || params.getAsJsonArray().size() == 0) {
            throw new IllegalArgumentException("sendTransaction params are missing");
        }
        JsonElement first = params.getAsJsonArray().get(0);
        if (first.isJsonObject()) {
            return first.getAsJsonObject();
        }
        return JsonParser.parseString(first.getAsString()).getAsJsonObject();
    }

    private static String domainOf(String manifestUrl) {
        String host = URI.create(manifestUrl).getHost();
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("TonConnect manifestUrl has no domain");
        }
        return host;
    }

    private static String hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >> 4) & 0xf, 16));
            builder.append(Character.forDigit(value & 0xf, 16));
        }
        return builder.toString();
    }
}
