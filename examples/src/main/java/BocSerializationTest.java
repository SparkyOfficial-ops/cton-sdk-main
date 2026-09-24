// BocSerializationTest.java - Тест для перевірки серіалізації BOC
// Author: Андрій Будильников (Sparky)

import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

public class BocSerializationTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing BOC serialization...");
            
            // Створюємо просту комірку
            Cell cell = new CellBuilder()
                .storeUInt(32, 0x12345678)
                .storeBytes(new byte[]{0x01, 0x02, 0x03, 0x04})
                .build();
            
            // Створюємо BOC з комірки
            Boc boc = new Boc(cell);
            
            // Серіалізуємо BOC
            byte[] serialized = boc.serialize(true, true);
            
            System.out.println("Serialized BOC size: " + serialized.length + " bytes");
            System.out.println("Serialization successful!");
            
            // Спробуємо десеріалізацію
            Boc deserializedBoc = Boc.deserialize(serialized);
            Cell rootCell = deserializedBoc.getRoot();
            
            System.out.println("Deserialization successful!");
            System.out.println("Root cell bit size: " + rootCell.getBitSize());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}