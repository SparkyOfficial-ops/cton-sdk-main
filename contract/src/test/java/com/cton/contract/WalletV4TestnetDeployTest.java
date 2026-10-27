// Повний цикл гаманця V4R2: TON-мнемоніка, адреса, деплой зі StateInit у testnet.
package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.Crypto;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Мнемоніка TON (сіль {@code TON default seed}) дає ключ Ed25519.
 * Адреса V4R2 — це хеш комірки StateInit (код V4R2, seqno 0, subwallet, публічний ключ, порожні плагіни).
 * Перший {@code transfer} на неактивний рахунок збирає external-in зі StateInit і відправляє його в testnet.
 */
public class WalletV4TestnetDeployTest {
    static final String TESTNET_API = "https://testnet.toncenter.com/api/v2/";
    static final int SUBWALLET_ID = 698983191;
    /** 0.01 TON лишається на щойно розгорнутому рахунку. */
    static final BigInteger DEPLOY_TRANSFER = new BigInteger("10000000");
    /** Нижче цієї суми мережа не прийме зовнішнє повідомлення: рахунок ще порожній. */
    static final BigInteger MIN_BALANCE = new BigInteger("50000000");

    @Test
    public void derivesV4AddressFromTonMnemonic() {
        List<String> mnemonic = Crypto.generateTonMnemonic(24);
        assertEquals(24, mnemonic.size());
        assertTrue(Crypto.isValidTonMnemonic(mnemonic));

        try (Crypto.PrivateKey privateKey = privateKey(mnemonic);
             Crypto.PublicKey publicKey = privateKey.getPublicKey();
             Address address = v4Address(publicKey.getData())) {
            assertEquals(32, publicKey.getData().length);
            assertTrue(address.toRaw().startsWith("0:"));
            assertEquals(66, address.toRaw().length());
            String friendly = address.toUserFriendly(true, true);
            assertFalse(friendly.isEmpty());
            try (Address parsed = new Address(friendly)) {
                assertEquals(address.toRaw(), parsed.toRaw());
            }
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "CTON_TESTNET", matches = "1")
    public void deploysWalletV4WithStateInitOnTestnet() throws Exception {
        String configured = System.getenv("CTON_TESTNET_MNEMONIC");
        List<String> mnemonic = configured == null || configured.trim().isEmpty()
            ? Crypto.generateTonMnemonic(24)
            : Arrays.asList(configured.trim().split("\\s+"));
        assertEquals(24, mnemonic.size(), "TON-мнемоніка гаманця V4 має 24 слова");
        assertTrue(Crypto.isValidTonMnemonic(mnemonic));

        TonApiClient apiClient = new TonApiClient(TESTNET_API, System.getenv("CTON_TESTNET_API_KEY"));
        try (Crypto.PrivateKey privateKey = privateKey(mnemonic);
             Crypto.PublicKey publicKey = privateKey.getPublicKey();
             Address address = v4Address(publicKey.getData())) {
            WalletV4 wallet = new WalletV4(address, apiClient, SUBWALLET_ID);
            assertEquals(4, wallet.getVersion());
            assertEquals(SUBWALLET_ID, wallet.getSubwalletId());

            JsonObject info = apiClient.getAddressInformation(address.toRaw());
            String state = accountState(info);
            BigInteger balance = wallet.getBalance();
            if ("active".equals(state)) {
                assertTrue(readSeqno(apiClient, address) >= 1);
                return;
            }

            if (balance.compareTo(MIN_BALANCE) < 0) {
                fail("Рахунок " + address.toUserFriendly(true, true)
                    + " (" + address.toRaw() + ") у testnet має баланс " + balance
                    + " наноTON. Надішліть монети через @testgiver_ton_bot і повторіть тест із "
                    + "CTON_TESTNET=1 та CTON_TESTNET_MNEMONIC=\"" + String.join(" ", mnemonic) + "\"");
            }

            wallet.transfer(address, DEPLOY_TRANSFER, "cton-sdk deploy", privateKey);

            long deadline = System.currentTimeMillis() + 90_000L;
            while (System.currentTimeMillis() < deadline && !"active".equals(state)) {
                Thread.sleep(3000L);
                state = accountState(apiClient.getAddressInformation(address.toRaw()));
            }
            assertEquals("active", state, "Після external-in зі StateInit рахунок V4R2 має стати active");
            assertTrue(readSeqno(apiClient, address) >= 1);
        }
    }

    static Crypto.PrivateKey privateKey(List<String> mnemonic) {
        byte[] seed = Crypto.mnemonicToTonSeed(mnemonic);
        assertEquals(64, seed.length);
        return new Crypto.PrivateKey(Arrays.copyOf(seed, 32));
    }

    /**
     * Адреса workchain 0 дорівнює representation hash комірки StateInit.
     */
    static Address v4Address(byte[] publicKey) {
        StateInit init = StateInit.forWallet(4, SUBWALLET_ID, publicKey);
        Cell state = init.toCell();
        byte[] hash = state.representationHash();
        assertEquals(32, hash.length);
        return new Address("0:" + hex(hash));
    }

    static String accountState(JsonObject info) throws IOException {
        if (!info.has("ok") || !info.get("ok").getAsBoolean() || !info.has("result")) {
            throw new IOException("getAddressInformation: " + info);
        }
        return info.getAsJsonObject("result").get("state").getAsString();
    }

    static long readSeqno(TonApiClient apiClient, Address address) throws IOException {
        JsonObject response = apiClient.runGetMethod(address.toRaw(), "seqno", new JsonArray());
        if (!response.has("result") || !response.get("result").isJsonObject()) {
            throw new IOException("runGetMethod seqno: " + response);
        }
        JsonObject result = response.getAsJsonObject("result");
        if (result.has("exit_code") && result.get("exit_code").getAsInt() != 0) {
            throw new IOException("seqno exit_code " + result.get("exit_code") + ": " + response);
        }
        JsonArray stack = result.getAsJsonArray("stack");
        String value = stack.get(0).getAsJsonArray().get(1).getAsString();
        return Long.decode(value);
    }

    static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            out.append(Character.forDigit((b >> 4) & 0xF, 16));
            out.append(Character.forDigit(b & 0xF, 16));
        }
        return out.toString();
    }
}
