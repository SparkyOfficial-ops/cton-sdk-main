// StateInit.java - початковий стан контракту гаманця
// Author: Андрій Будильников (Sparky)

package com.cton.contract;

import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * StateInit за TL-B:
 * split_depth:(Maybe) special:(Maybe) code:(Maybe ^Cell) data:(Maybe ^Cell) library:(HashmapE 256).
 * Код V3R2 і V4R2 — стандартні комірки з tonweb / ton-blockchain wallet-contract.
 */
public final class StateInit {
    /** Зкомпільований wallet-v3-r2, hex BoC. */
    private static final String V3R2_CODE_HEX =
        "B5EE9C724101010100710000DEFF0020DD2082014C97BA218201339CBAB19F71B0ED44D0D31FD31F31D70BFFE304E0A4F2608308D71820D31FD31FD31FF82313BBF263ED44D0D31FD31FD3FFD15132BAF2A15144BAF2A204F901541055F910F2A3F8009320D74A96D307D402FB00E8D101A4C8CB1FCB1FCBFFC9ED5410BD6DAD";

    /** Зкомпільований wallet-v4-r2, hex BoC. */
    private static final String V4R2_CODE_HEX =
        "B5EE9C72410214010002D4000114FF00F4A413F4BCF2C80B010201200203020148040504F8F28308D71820D31FD31FD31F02F823BBF264ED44D0D31FD31FD3FFF404D15143BAF2A15151BAF2A205F901541064F910F2A3F80024A4C8CB1F5240CB1F5230CBFF5210F400C9ED54F80F01D30721C0009F6C519320D74A96D307D402FB00E830E021C001E30021C002E30001C0039130E30D03A4C8CB1F12CB1FCBFF1011121302E6D001D0D3032171B0925F04E022D749C120925F04E002D31F218210706C7567BD22821064737472BDB0925F05E003FA403020FA4401C8CA07CBFFC9D0ED44D0810140D721F404305C810108F40A6FA131B3925F07E005D33FC8258210706C7567BA923830E30D03821064737472BA925F06E30D06070201200809007801FA00F40430F8276F2230500AA121BEF2E0508210706C7567831EB17080185004CB0526CF1658FA0219F400CB6917CB1F5260CB3F20C98040FB0006008A5004810108F45930ED44D0810140D720C801CF16F400C9ED540172B08E23821064737472831EB17080185005CB055003CF1623FA0213CB6ACB1FCB3FC98040FB00925F03E20201200A0B0059BD242B6F6A2684080A06B90FA0218470D4080847A4937D29910CE6903E9FF9837812801B7810148987159F31840201580C0D0011B8C97ED44D0D70B1F8003DB29DFB513420405035C87D010C00B23281F2FFF274006040423D029BE84C600201200E0F0019ADCE76A26840206B90EB85FFC00019AF1DF6A26840106B90EB858FC0006ED207FA00D4D422F90005C8CA0715CBFFC9D077748018C8CB05CB0222CF165005FA0214CB6B12CCCCC973FB00C84014810108F451F2A7020070810108D718FA00D33FC8542047810108F451F2A782106E6F746570748018C8CB05CB025006CF165004FA0214CB6A12CB1FCB3FC973FB0002006C810108D718FA00D33F305224810108F459F2A782106473747270748018C8CB05CB025005CF165003FA0213CB6ACB1F12CB3FC973FB00000AF400C9ED54696225E5";

    private static volatile Cell v3R2Code;
    private static volatile Cell v4R2Code;

    private final Cell code;
    private final Cell data;

    public StateInit(Cell code, Cell data) {
        if (code == null || data == null) {
            throw new IllegalArgumentException("StateInit code and data are required");
        }
        this.code = code;
        this.data = data;
    }

    public Cell getCode() {
        return code;
    }

    public Cell getData() {
        return data;
    }

    /**
     * Початкові дані гаманця: seqno = 0, subwallet_id, public_key.
     * V4R2 після ключа має порожній словник плагінів.
     */
    public static StateInit forWallet(int version, long subwalletId, byte[] publicKey) {
        if (publicKey == null || publicKey.length != 32) {
            throw new IllegalArgumentException("Ed25519 public key must be 32 bytes");
        }
        CellBuilder dataBuilder = new CellBuilder();
        dataBuilder.storeUInt(32, 0);
        dataBuilder.storeUInt(32, subwalletId);
        dataBuilder.storeBytes(publicKey);
        Cell codeCell;
        if (version == 3) {
            codeCell = walletV3R2Code();
        } else if (version == 4) {
            dataBuilder.storeBit(false);
            codeCell = walletV4R2Code();
        } else {
            throw new IllegalArgumentException("StateInit is defined for wallet V3R2 and V4R2");
        }
        return new StateInit(codeCell, dataBuilder.build());
    }

    /**
     * Записати StateInit у вже відкриту комірку повідомлення (Either Left, inline).
     */
    public void store(CellBuilder builder) {
        builder.storeBit(false);
        builder.storeBit(false);
        builder.storeBit(true);
        builder.storeRef(code);
        builder.storeBit(true);
        builder.storeRef(data);
        builder.storeBit(false);
    }

    public Cell toCell() {
        CellBuilder builder = new CellBuilder();
        store(builder);
        return builder.build();
    }

    private static Cell walletV3R2Code() {
        Cell cached = v3R2Code;
        if (cached == null) {
            synchronized (StateInit.class) {
                cached = v3R2Code;
                if (cached == null) {
                    v3R2Code = cached = cellFromHexBoc(V3R2_CODE_HEX);
                }
            }
        }
        return cached;
    }

    private static Cell walletV4R2Code() {
        Cell cached = v4R2Code;
        if (cached == null) {
            synchronized (StateInit.class) {
                cached = v4R2Code;
                if (cached == null) {
                    v4R2Code = cached = cellFromHexBoc(V4R2_CODE_HEX);
                }
            }
        }
        return cached;
    }

    private static Cell cellFromHexBoc(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        try (Boc boc = Boc.deserialize(bytes)) {
            return boc.getRoot();
        }
    }
}
