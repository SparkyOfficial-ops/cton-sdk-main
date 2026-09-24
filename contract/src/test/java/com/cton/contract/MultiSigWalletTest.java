// MultiSigWalletTest.java - тести для MultiSigWallet
// Author: Андрій Будильников (Sparky)
// Tests for MultiSigWallet
// Тесты для MultiSigWallet

package com.cton.contract;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;

/**
 * Тести для MultiSigWallet
 */
public class MultiSigWalletTest {
    private Address walletAddress;
    private TonApiClient apiClient;
    private List<Address> signers;
    
    @BeforeEach
    public void setUp() {
        walletAddress = mock(Address.class);
        apiClient = mock(TonApiClient.class);
        signers = new ArrayList<>();
        signers.add(mock(Address.class));
        signers.add(mock(Address.class));
        signers.add(mock(Address.class));
    }
    
    @Test
    public void testMultiSigWalletConstructor() {
        // Тест конструктора MultiSigWallet
        // Test MultiSigWallet constructor
        // Тест конструктора MultiSigWallet
        
        MultiSigWallet multiSigWallet = new MultiSigWallet(walletAddress, apiClient, signers, 2);
        
        assertNotNull(multiSigWallet);
        assertEquals(2, multiSigWallet.getRequiredSignatures());
        assertEquals(3, multiSigWallet.getSigners().size());
    }
    
    @Test
    public void testMultiSigWalletConstructorWithSubwalletId() throws Exception {
        // Тест конструктора MultiSigWallet з subwalletId
        // Test MultiSigWallet constructor with subwalletId
        // Тест конструктора MultiSigWallet с subwalletId
        
        MultiSigWallet multiSigWallet = new MultiSigWallet(walletAddress, apiClient, signers, 2, 12345);
        
        assertNotNull(multiSigWallet);
        assertEquals(2, multiSigWallet.getRequiredSignatures());
        assertEquals(12345, multiSigWallet.getSubwalletId());
    }
    
    @Test
    public void testInvalidRequiredSignatures() {
        // Тест з неправильними параметрами підписів
        // Test with invalid signature parameters
        // Тест с неправильными параметрами подписей
        
        // Потрібно більше підписів, ніж є підписувачів
        // Need more signatures than there are signers
        // Нужно больше подписей, чем есть подписчиков
        
        assertThrows(IllegalArgumentException.class, () -> {
            new MultiSigWallet(walletAddress, apiClient, signers, 5);
        });
    }
    
    @Test
    public void testZeroRequiredSignatures() {
        // Тест з нульовою кількістю необхідних підписів
        // Test with zero required signatures
        // Тест с нулевым количеством необходимых подписей
        
        assertThrows(IllegalArgumentException.class, () -> {
            new MultiSigWallet(walletAddress, apiClient, signers, 0);
        });
    }
    
    @Test
    public void testGetVersion() throws Exception {
        // Тест отримання версії
        // Test getting version
        // Тест получения версии
        
        MultiSigWallet multiSigWallet = new MultiSigWallet(walletAddress, apiClient, signers, 2);
        assertEquals(100, multiSigWallet.getVersion());
    }
    
    @Test
    public void testGetSigners() {
        // Тест отримання списку підписувачів
        // Test getting signers list
        // Тест получения списка подписчиков
        
        MultiSigWallet multiSigWallet = new MultiSigWallet(walletAddress, apiClient, signers, 2);
        List<Address> walletSigners = multiSigWallet.getSigners();
        
        assertNotNull(walletSigners);
        assertEquals(3, walletSigners.size());
        assertNotSame(signers, walletSigners); // Should return a copy
    }
    
    @Test
    public void testGetRequiredSignatures() {
        // Тест отримання кількості необхідних підписів
        // Test getting required signatures count
        // Тест получения количества необходимых подписей
        
        MultiSigWallet multiSigWallet = new MultiSigWallet(walletAddress, apiClient, signers, 2);
        assertEquals(2, multiSigWallet.getRequiredSignatures());
    }
}