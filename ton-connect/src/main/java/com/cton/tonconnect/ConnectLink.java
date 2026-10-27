package com.cton.tonconnect;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Посилання dApp: v=2, id — публічний ключ dApp, r — JSON запиту на підключення.
 */
final class ConnectLink {
    final String dappClientId;
    final String requestJson;
    final String bridgeUrl;

    private ConnectLink(String dappClientId, String requestJson, String bridgeUrl) {
        this.dappClientId = dappClientId;
        this.requestJson = requestJson;
        this.bridgeUrl = bridgeUrl;
    }

    static ConnectLink parse(String link) {
        int queryStart = link.indexOf('?');
        if (queryStart < 0) {
            throw new IllegalArgumentException("TonConnect link has no query");
        }
        Map<String, String> query = new LinkedHashMap<>();
        for (String part : link.substring(queryStart + 1).split("&")) {
            int eq = part.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = urlDecode(part.substring(0, eq));
            String value = urlDecode(part.substring(eq + 1));
            query.put(key, value);
        }
        String version = query.get("v");
        if (version != null && !"2".equals(version)) {
            throw new IllegalArgumentException("Unsupported TonConnect version: " + version);
        }
        String id = query.get("id");
        String request = query.get("r");
        if (id == null || request == null) {
            throw new IllegalArgumentException("TonConnect link requires id and r");
        }
        return new ConnectLink(id, request, query.get("bridge"));
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
