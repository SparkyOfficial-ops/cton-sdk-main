// ComprehensiveExample.java - Комплексний приклад використання CTON-SDK
// Author: Андрій Будильников (Sparky)

/*
 * Комплексний приклад використання CTON-SDK
 * Цей приклад демонструє всі основні функції SDK
 */

import com.cton.api.TonApiClient;
import com.cton.contract.Wallet;
import com.cton.contract.WalletV3;
import com.cton.contract.MessageFactory;
import com.cton.contract.Jetton;
import com.cton.contract.Nft;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.google.gson.JsonObject;
import java.math.BigInteger;
import java.io.IOException;

/**
 * Комплексний приклад використання CTON-SDK
 * 
 * This example demonstrates all the main features of the CTON-SDK:
 * 1. API client for interacting with TON Center
 * 2. Wallet functionality for creating and sending transactions
 * 3. Message factory for creating different types of messages
 * 4. Contract wrappers for Jetton and NFT tokens
 */
public class ComprehensiveExample {
    
    public static void main(String[] args) {
        System.out.println("CTON-SDK Comprehensive Example");
        System.out.println("=============================");
        System.out.println("");
        System.out.println("This example shows how to use all components of the CTON-SDK.");
        System.out.println("Note: Actual execution requires building the native library first.");
        System.out.println("");
        
        // 1. API Client usage
        System.out.println("1. API Client Usage:");
        System.out.println("-------------------");
        System.out.println("TonApiClient client = new TonApiClient(\"https://toncenter.com/api/v2/\");");
        System.out.println("// JsonObject addressInfo = client.getAddressInformation(\"EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N\");");
        System.out.println("");
        
        // 2. Wallet usage
        System.out.println("2. Wallet Usage:");
        System.out.println("----------------");
        System.out.println("// Address walletAddress = new Address(\"EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N\");");
        System.out.println("// Wallet wallet = new WalletV3(walletAddress, client);");
        System.out.println("// BigInteger balance = wallet.getBalance();");
        System.out.println("// Cell transfer = wallet.createTransfer(recipient, amount, \"Test transfer\");");
        System.out.println("");
        
        // 3. Message Factory usage
        System.out.println("3. Message Factory Usage:");
        System.out.println("------------------------");
        System.out.println("// Address recipient = new Address(\"EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V\");");
        System.out.println("// BigInteger amount = BigInteger.valueOf(1_000_000_000L); // 1 TON");
        System.out.println("// Cell textMessage = MessageFactory.createTextMessage(\"Hello from CTON-SDK!\");");
        System.out.println("// Cell internalMessage = MessageFactory.createInternalMessage(recipient, amount, textMessage, 0);");
        System.out.println("// Cell externalMessage = MessageFactory.createExternalMessage(recipient, amount, textMessage);");
        System.out.println("");
        
        // 4. Jetton usage
        System.out.println("4. Jetton Token Usage:");
        System.out.println("---------------------");
        System.out.println("// Address jettonAddress = new Address(\"EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N\");");
        System.out.println("// Jetton jetton = new Jetton(jettonAddress, client);");
        System.out.println("// BigInteger totalSupply = jetton.getTotalSupply();");
        System.out.println("// Address owner = new Address(\"EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V\");");
        System.out.println("// BigInteger balance = jetton.getBalance(owner);");
        System.out.println("");
        
        // 5. NFT usage
        System.out.println("5. NFT Usage:");
        System.out.println("-------------");
        System.out.println("// Address nftAddress = new Address(\"EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N\");");
        System.out.println("// Nft nft = new Nft(nftAddress, client);");
        System.out.println("// JsonObject nftData = nft.getNftData();");
        System.out.println("// long index = nft.getIndex();");
        System.out.println("// Address owner = nft.getOwner();");
        System.out.println("");
        
        // 6. Contract usage
        System.out.println("6. Contract Usage:");
        System.out.println("------------------");
        System.out.println("// Address contractAddress = new Address(\"EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N\");");
        System.out.println("// Contract contract = new Contract(contractAddress, client) {};");
        System.out.println("// JsonObject stack = new JsonObject();");
        System.out.println("// JsonObject result = contract.runGetMethod(\"get_wallet_data\", stack);");
        System.out.println("");
        
        System.out.println("All components of the CTON-SDK are now implemented!");
        System.out.println("The SDK provides a complete solution for working with the TON blockchain.");
    }
}