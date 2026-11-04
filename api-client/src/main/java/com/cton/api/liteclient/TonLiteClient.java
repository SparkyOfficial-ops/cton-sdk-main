package com.cton.api.liteclient;

/**
 * Old name for {@link TonProxyHttpClient}. This class talks HTTP to a proxy.
 * It is not a LiteServer client.
 */
@Deprecated
public class TonLiteClient extends TonProxyHttpClient {
    public TonLiteClient() {
        super();
    }
}
