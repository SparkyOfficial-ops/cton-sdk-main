package com.cton.tonconnect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SessionCryptoTest {

    @Test
    public void boxRoundTripMatchesThePeer() {
        SessionCrypto wallet = new SessionCrypto();
        SessionCrypto dapp = new SessionCrypto();
        String encrypted = wallet.encrypt("{\"method\":\"sendTransaction\"}", SessionCrypto.hexToBytes(dapp.clientId()));
        String opened = dapp.decrypt(encrypted, SessionCrypto.hexToBytes(wallet.clientId()));
        assertEquals("{\"method\":\"sendTransaction\"}", opened);
        assertEquals(64, wallet.clientId().length());
        assertTrue(encrypted.length() > 0);
    }

    @Test
    public void parsesConnectLink() {
        String request = "{\"manifestUrl\":\"https://example.com/tonconnect-manifest.json\",\"items\":[{\"name\":\"ton_addr\"}]}";
        String link = "tc://?v=2&id=aa&r=" + java.net.URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8)
            + "&bridge=https://bridge.tonapi.io/bridge";
        ConnectLink parsed = ConnectLink.parse(link);
        assertEquals("aa", parsed.dappClientId);
        assertEquals("https://bridge.tonapi.io/bridge", parsed.bridgeUrl);
        assertTrue(parsed.requestJson.contains("ton_addr"));
    }
}
