package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.Crypto;
import com.cton.sdk.SessionCrypto;

/**
 * TON Connect 2 session. The session id is the hex X25519 public key.
 * {@code ton_sign} is signed with the caller's private key. There is no
 * placeholder signature.
 */
public class WalletConnect {
    private final Wallet wallet;
    private final SessionCrypto session;
    private final TonConnectBridge bridge;
    private final Map<String, WalletConnectRequest> pendingRequests;

    public WalletConnect(Wallet wallet) {
        this(wallet, new SessionCrypto(), new TonConnectBridge());
    }

    public WalletConnect(Wallet wallet, SessionCrypto session, TonConnectBridge bridge) {
        if (wallet == null || session == null || bridge == null) {
            throw new IllegalArgumentException("wallet, session and bridge are required");
        }
        this.wallet = wallet;
        this.session = session;
        this.bridge = bridge;
        this.pendingRequests = new HashMap<>();
    }

    public String createSession(Map<String, String> dAppMetadata) {
        String manifest = "";
        if (dAppMetadata != null) {
            manifest = dAppMetadata.getOrDefault("manifestUrl", dAppMetadata.getOrDefault("url", ""));
        }
        String request = "{\"manifestUrl\":\"" + escape(manifest) + "\",\"items\":[{\"name\":\"ton_addr\"}]}";
        String encoded = URLEncoder.encode(request, StandardCharsets.UTF_8);
        return "tc://?v=2&id=" + session.clientId() + "&r=" + encoded + "&bridge=" +
                URLEncoder.encode(bridge.url(), StandardCharsets.UTF_8);
    }

    public void sendEncrypted(String toClientId, byte[] receiverPublicKey, String json, int ttlSeconds) throws IOException {
        String body = session.encrypt(json.getBytes(StandardCharsets.UTF_8), receiverPublicKey);
        bridge.send(session.clientId(), toClientId, body, ttlSeconds);
    }

    public WalletConnectResponse handleRequest(String requestId, String requestType, Map<String, Object> params) {
        WalletConnectRequest request = new WalletConnectRequest(requestId, requestType, params);
        pendingRequests.put(requestId, request);
        switch (requestType) {
            case "ton_getBalance":
                return handleGetBalanceRequest(request);
            case "ton_sendTransaction":
                return handleSendTransactionRequest(request);
            case "ton_sign":
                return new WalletConnectResponse(requestId, false,
                        "signature requires approveRequest with a private key", null);
            default:
                return new WalletConnectResponse(requestId, false, "Unsupported request type: " + requestType, null);
        }
    }

    private WalletConnectResponse handleGetBalanceRequest(WalletConnectRequest request) {
        try {
            BigInteger balance = wallet.getBalance();
            Map<String, Object> result = new HashMap<>();
            result.put("balance", balance.toString());
            return new WalletConnectResponse(request.getId(), true, null, result);
        } catch (IOException e) {
            return new WalletConnectResponse(request.getId(), false, "Failed to get balance: " + e.getMessage(), null);
        }
    }

    private WalletConnectResponse handleSendTransactionRequest(WalletConnectRequest request) {
        try {
            Map<String, Object> params = request.getParams();
            String toAddress = (String) params.get("to");
            String amountStr = (String) params.get("amount");
            String comment = (String) params.get("comment");
            Address destination = new Address(toAddress);
            BigInteger amount = new BigInteger(amountStr);
            Cell transaction = wallet.createTransfer(destination, amount, comment);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "transaction cell is ready for the wallet signature");
            result.put("bits", transaction.getBitSize());
            return new WalletConnectResponse(request.getId(), true, null, result);
        } catch (Exception e) {
            return new WalletConnectResponse(request.getId(), false, "Failed to create transaction: " + e.getMessage(), null);
        }
    }

    public WalletConnectResponse approveRequest(String requestId, Crypto.PrivateKey privateKey) {
        WalletConnectRequest request = pendingRequests.get(requestId);
        if (request == null) {
            return new WalletConnectResponse(requestId, false, "Request not found", null);
        }
        pendingRequests.remove(requestId);
        if ("ton_sign".equals(request.getType())) {
            if (privateKey == null) {
                return new WalletConnectResponse(requestId, false, "private key is required to sign", null);
            }
            Object data = request.getParams() == null ? null : request.getParams().get("data");
            if (!(data instanceof String) || ((String) data).isEmpty()) {
                return new WalletConnectResponse(requestId, false, "sign request has no data", null);
            }
            byte[] signature = Crypto.sign(privateKey, ((String) data).getBytes(StandardCharsets.UTF_8));
            if (signature.length != 64) {
                return new WalletConnectResponse(requestId, false, "signature must be 64 bytes", null);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("signature", hex(signature));
            return new WalletConnectResponse(requestId, true, null, result);
        }
        return new WalletConnectResponse(requestId, true, null, Map.of("status", "approved"));
    }

    public WalletConnectResponse rejectRequest(String requestId) {
        WalletConnectRequest request = pendingRequests.get(requestId);
        if (request == null) {
            return new WalletConnectResponse(requestId, false, "Request not found", null);
        }
        pendingRequests.remove(requestId);
        return new WalletConnectResponse(requestId, true, null, Map.of("status", "rejected"));
    }

    public void disconnect() {
        pendingRequests.clear();
    }

    public String getSessionId() {
        return session.clientId();
    }

    public SessionCrypto session() {
        return session;
    }

    public TonConnectBridge bridge() {
        return bridge;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String hex(byte[] bytes) {
        char[] digits = new char[bytes.length * 2];
        char[] alphabet = "0123456789abcdef".toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            digits[i * 2] = alphabet[value >>> 4];
            digits[i * 2 + 1] = alphabet[value & 0x0F];
        }
        return new String(digits);
    }

    public static class WalletConnectRequest {
        private final String id;
        private final String type;
        private final Map<String, Object> params;

        public WalletConnectRequest(String id, String type, Map<String, Object> params) {
            this.id = id;
            this.type = type;
            this.params = params;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public Map<String, Object> getParams() {
            return params;
        }
    }

    public static class WalletConnectResponse {
        private final String id;
        private final boolean success;
        private final String error;
        private final Map<String, Object> result;

        public WalletConnectResponse(String id, boolean success, String error, Map<String, Object> result) {
            this.id = id;
            this.success = success;
            this.error = error;
            this.result = result;
        }

        public String getId() {
            return id;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }

        public Map<String, Object> getResult() {
            return result;
        }
    }
}
