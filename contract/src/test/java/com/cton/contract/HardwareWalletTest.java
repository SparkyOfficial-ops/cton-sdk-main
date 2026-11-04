// HardwareWalletTest.java - тести для HardwareWallet
// Author: Андрій Будильников (Sparky)
// Tests for HardwareWallet
// Тесты для HardwareWallet

package com.cton.contract;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;

/**
 * Тести для HardwareWallet
 */
public class HardwareWalletTest {
    private Address walletAddress;
    private TonApiClient apiClient;
    
    @BeforeEach
    public void setUp() {
        walletAddress = mock(Address.class);
        apiClient = mock(TonApiClient.class);
    }
    
    @Test
    public void testHardwareWalletConstructor() {
        // Тест конструктора HardwareWallet
        // Test HardwareWallet constructor
        // Тест конструктора HardwareWallet
        
        HardwareWallet hardwareWallet = new HardwareWallet(
            walletAddress, 
            apiClient, 
            "Ledger", 
            "device_12345"
        );
        
        assertNotNull(hardwareWallet);
        assertEquals("Ledger", hardwareWallet.getDeviceType());
        assertEquals("device_12345", hardwareWallet.getDeviceId());
        assertEquals(0, hardwareWallet.getAccountIndex());
    }
    
    @Test
    public void testHardwareWalletConstructorWithAccountIndex() {
        // Тест конструктора HardwareWallet з індексом облікового запису
        // Test HardwareWallet constructor with account index
        // Тест конструктора HardwareWallet с индексом учетной записи
        
        HardwareWallet hardwareWallet = new HardwareWallet(
            walletAddress, 
            apiClient, 
            "Trezor", 
            "device_67890",
            5
        );
        
        assertNotNull(hardwareWallet);
        assertEquals("Trezor", hardwareWallet.getDeviceType());
        assertEquals("device_67890", hardwareWallet.getDeviceId());
        assertEquals(5, hardwareWallet.getAccountIndex());
    }
    
    @Test
    public void testGetVersion() throws Exception {
        // Тест отримання версії
        // Test getting version
        // Тест получения версии
        
        HardwareWallet hardwareWallet = new HardwareWallet(
            walletAddress, 
            apiClient, 
            "Ledger", 
            "device_12345"
        );
        
        assertThrows(IOException.class, hardwareWallet::getVersion);
    }
    
    @Test
    public void testIsDeviceConnected() {
        // Тест перевірки підключення пристрою
        // Test device connection check
        // Тест проверки подключения устройства
        
        HardwareWallet hardwareWallet = new HardwareWallet(
            walletAddress, 
            apiClient, 
            "Ledger", 
            "device_12345"
        );
        
        assertFalse(hardwareWallet.isDeviceConnected());
    }
    
    @Test
    public void testGetSupportedDevices() {
        // Тест отримання списку підтримуваних пристроїв
        // Test getting supported devices list
        // Тест получения списка поддерживаемых устройств
        
        List<String> supportedDevices = HardwareWallet.getSupportedDevices();
        
        assertNotNull(supportedDevices);
        assertFalse(supportedDevices.isEmpty());
        assertEquals(List.of("Ledger"), supportedDevices);
    }
    
    @Test
    public void testGetDeviceType() {
        // Тест отримання типу пристрою
        // Test getting device type
        // Тест получения типа устройства
        
        HardwareWallet hardwareWallet = new HardwareWallet(
            walletAddress, 
            apiClient, 
            "Trezor", 
            "device_67890"
        );
        
        assertEquals("Trezor", hardwareWallet.getDeviceType());
    }
    
    @Test
    public void testGetDeviceId() {
        // Тест отримання ідентифікатора пристрою
        // Test getting device ID
        // Тест получения идентификатора устройства
        
        HardwareWallet hardwareWallet = new HardwareWallet(
            walletAddress, 
            apiClient, 
            "Keystone", 
            "keystone_device_abc"
        );
        
        assertEquals("keystone_device_abc", hardwareWallet.getDeviceId());
    }
    
    @Test
    public void testGetAccountIndex() {
        // Тест отримання індексу облікового запису
        // Test getting account index
        // Тест получения индекса учетной записи
        
        HardwareWallet hardwareWallet = new HardwareWallet(
            walletAddress, 
            apiClient, 
            "Ledger", 
            "device_12345",
            10
        );
        
        assertEquals(10, hardwareWallet.getAccountIndex());
    }
    
    @Test
    public void testGetAddressFromDevice() throws Exception {
        // Тест отримання адреси з пристрою
        // Test getting address from device
        // Тест получения адреса с устройства
        
        HardwareWallet hardwareWallet = new HardwareWallet(
            walletAddress, 
            apiClient, 
            "Ledger", 
            "device_12345"
        );
        
        assertThrows(IOException.class, hardwareWallet::getAddressFromDevice);
    }
    
    @Test
    public void testCreateTransfer() throws Exception {
        // Тест створення транзакції
        // Test creating transaction
        // Тест создания транзакции
        
        HardwareWallet hardwareWallet = new HardwareWallet(
            walletAddress, 
            apiClient, 
            "Ledger", 
            "device_12345"
        );
        
        Address destination = mock(Address.class);
        java.math.BigInteger amount = java.math.BigInteger.valueOf(1000000000L);
        
        com.cton.sdk.Cell transaction = hardwareWallet.createTransfer(destination, amount, "Test transfer");
        
        assertNotNull(transaction);
    }

    @Test
    public void testLedgerApduFraming() {
        byte[] apdu = LedgerApdu.command(LedgerApdu.INS_GET_ADDRESS, 0, 0, new byte[] {0x01, 0x02});
        assertArrayEquals(new byte[] {(byte) 0xE0, 0x04, 0x00, 0x00, 0x02, 0x01, 0x02}, apdu);
        byte[] response = new byte[] {0x0A, (byte) 0x90, 0x00};
        assertEquals(LedgerApdu.SW_OK, LedgerApdu.statusWord(response));
        assertArrayEquals(new byte[] {0x0A}, LedgerApdu.payload(response));
    }
}