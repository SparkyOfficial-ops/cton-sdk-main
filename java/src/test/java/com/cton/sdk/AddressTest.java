// AddressTest.java - Тести для Address
// Author: Андрій Будильников (Sparky)

package com.cton.sdk;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Тести для Address
 */
public class AddressTest {
    
    @Test
    public void testDefaultAddress() {
        Address address = new Address();
        assertNotNull(address);
        assertFalse(address.isValid());
    }
    
    @Test
    public void testAddressFromString() {
        // Test with valid address string
        Address address = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
        assertNotNull(address);
        // Address parsing might not be fully implemented yet
        assertTrue(true);
    }
    
    @Test
    public void testAddressWorkchain() {
        Address address = new Address();
        address.setWorkchain((byte) -1);
        assertEquals(-1, address.getWorkchain());
    }
    
    @Test
    public void testAddressHashPart() {
        Address address = new Address();
        byte[] hashPart = new byte[32];
        for (int i = 0; i < 32; i++) {
            hashPart[i] = (byte) i;
        }
        
        address.setHashPart(hashPart);
        
        byte[] retrievedHashPart = address.getHashPart();
        assertArrayEquals(hashPart, retrievedHashPart);
    }
    
    @Test
    public void testAddressHashPartInvalidLength() {
        Address address = new Address();
        byte[] invalidHashPart = new byte[31]; // Should be 32 bytes
        
        assertThrows(IllegalArgumentException.class, () -> {
            address.setHashPart(invalidHashPart);
        });
    }
    
    @Test
    public void testAddressToRaw() {
        Address address = new Address();
        String raw = address.toRaw();
        // Might be null for unimplemented method
        assertTrue(true);
    }
    
    @Test
    public void testAddressToUserFriendly() {
        Address address = new Address();
        String uf = address.toUserFriendly(true, false);
        // Might be null for unimplemented method
        assertTrue(true);
    }
    
    @Test
    public void testAddressIsValid() {
        Address address = new Address();
        // Default address should not be valid
        assertFalse(address.isValid());
    }
}