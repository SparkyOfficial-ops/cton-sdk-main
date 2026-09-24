// AddressExample.java - приклад роботи з адресами TON
// Author: Андрій Будильников (Sparky)
// Example of working with TON addresses
// Пример работы с адресами TON

import com.cton.sdk.Address;

public class AddressExample {
    public static void main(String[] args) {
        System.out.println("CTON-SDK Address Example");
        System.out.println("=======================");
        
        try {
            // Create an address from raw format
            System.out.println("1. Creating address from raw format...");
            String rawAddress = "0:1234567890123456789012345678901234567890123456789012345678901234";
            Address addr1 = new Address(rawAddress);
            System.out.println("   Raw address: " + rawAddress);
            System.out.println("   Address is valid: " + addr1.isValid());
            System.out.println("   Workchain: " + addr1.getWorkchain());
            
            // Convert to user-friendly format
            System.out.println("2. Converting to user-friendly format...");
            String userFriendly = addr1.toUserFriendly(true, false); // bounceable, not testnet
            System.out.println("   User-friendly address: " + userFriendly);
            
            // Create address from user-friendly format
            System.out.println("3. Creating address from user-friendly format...");
            Address addr2 = new Address(userFriendly);
            System.out.println("   Address is valid: " + addr2.isValid());
            System.out.println("   Workchain: " + addr2.getWorkchain());
            
            // Convert back to raw
            System.out.println("4. Converting back to raw format...");
            String rawAgain = addr2.toRaw();
            System.out.println("   Raw address: " + rawAgain);
            
            // Compare addresses
            System.out.println("5. Comparing addresses...");
            System.out.println("   Addresses are equal: " + addr1.equals(addr2));
            
            // Create an address with different workchain
            System.out.println("6. Creating address with different workchain...");
            Address addr3 = new Address("-1:1234567890123456789012345678901234567890123456789012345678901234");
            System.out.println("   Address is valid: " + addr3.isValid());
            System.out.println("   Workchain: " + addr3.getWorkchain());
            
            // Test with testnet flag
            System.out.println("7. Testing with testnet flag...");
            String testnetAddress = addr1.toUserFriendly(true, true); // bounceable, testnet
            System.out.println("   Testnet address: " + testnetAddress);
            
            System.out.println("\nAddress example completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error in address example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}