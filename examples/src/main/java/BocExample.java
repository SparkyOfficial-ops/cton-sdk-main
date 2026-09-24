// BocExample.java - приклад роботи з BOC (Bag of Cells)
// Author: Андрій Будильников (Sparky)
// Example of working with BOC (Bag of Cells)
// Пример работы с BOC (Bag of Cells)

import java.math.BigInteger;

import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

public class BocExample {
    public static void main(String[] args) {
        System.out.println("CTON-SDK BOC Example");
        System.out.println("===================");
        
        try {
            // Create a cell with some data
            System.out.println("1. Creating a cell with data...");
            CellBuilder builder = new CellBuilder();
            builder.storeUInt(32, 0x12345678); // Store a 32-bit unsigned integer
            builder.storeBytes("Hello, TON!".getBytes()); // Store some bytes
            Cell cell = builder.build();
            System.out.println("   Cell created successfully");
            System.out.println("   Cell bit size: " + cell.getBitSize());
            System.out.println("   Cell refs count: " + cell.getRefsCount());
            
            // Create another cell
            System.out.println("2. Creating another cell...");
            CellBuilder builder2 = new CellBuilder();
            builder2.storeInt(64, -123456789L); // Store a 64-bit signed integer
            builder2.storeCoins(new BigInteger("1000000000")); // Store some coins (in nanotons)
            Cell cell2 = builder2.build();
            System.out.println("   Second cell created successfully");
            
            // Add the second cell as a reference to the first
            System.out.println("3. Adding reference between cells...");
            cell.storeRef(cell2);
            System.out.println("   Reference added");
            System.out.println("   Cell refs count: " + cell.getRefsCount());
            
            // Create a BOC with the root cell
            System.out.println("4. Creating BOC...");
            Boc boc = new Boc(cell);
            System.out.println("   BOC created successfully");
            
            // Serialize the BOC
            System.out.println("5. Serializing BOC...");
            byte[] serializedBoc = boc.serialize(true, true); // with index and CRC
            System.out.println("   BOC serialized successfully");
            System.out.println("   Serialized BOC size: " + serializedBoc.length + " bytes");
            
            // Print first few bytes of serialized BOC
            System.out.print("   First 10 bytes: ");
            for (int i = 0; i < Math.min(10, serializedBoc.length); i++) {
                System.out.printf("%02X ", serializedBoc[i]);
            }
            System.out.println();
            
            // Deserialize the BOC
            System.out.println("6. Deserializing BOC...");
            Boc deserializedBoc = Boc.deserialize(serializedBoc);
            System.out.println("   BOC deserialized successfully");
            
            // Get the root cell
            System.out.println("7. Getting root cell...");
            Cell rootCell = deserializedBoc.getRoot();
            System.out.println("   Root cell obtained");
            System.out.println("   Root cell bit size: " + rootCell.getBitSize());
            System.out.println("   Root cell refs count: " + rootCell.getRefsCount());
            
            System.out.println("\nBOC example completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error in BOC example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}