// CellBuilderTest.java - Тести для CellBuilder
// Author: Андрій Будильников (Sparky)

package com.cton.sdk;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Тести для CellBuilder
 */
public class CellBuilderTest {
    
    @Test
    public void testBuildSimpleCell() {
        // Створюємо просту комірку з кількома значеннями
        Cell cell = new CellBuilder()
            .storeUInt(32, 0x12345678)
            .storeInt(16, -100)
            .storeBytes(new byte[]{0x01, 0x02, 0x03, 0x04})
            .build();
        
        // Перевіряємо, що комірка створена
        assertNotNull(cell);
        assertTrue(cell.getBitSize() > 0);
        
        // Перевіряємо, що дані присутні
        byte[] data = cell.getData();
        assertNotNull(data);
        assertTrue(data.length > 0);
    }
    
    @Test
    public void testBuildMessageBody() {
        // Приклад з опису проекту
        long opCode = 0x12345678;
        Address destinationAddress = new Address();
        BigInteger amount = BigInteger.valueOf(1000000000L); // 1 TON в нанотоні
        
        Cell messageBody = new CellBuilder()
            .storeUInt(32, opCode)
            .storeAddress(destinationAddress)
            .storeCoins(amount)
            .build();
        
        // Перевіряємо, що комірка створена
        assertNotNull(messageBody);
        assertTrue(messageBody.getBitSize() > 0);
    }
    
    @Test
    public void testCellWithReference() {
        // Створюємо внутрішню комірку
        Cell innerCell = new CellBuilder()
            .storeUInt(8, 0xFF)
            .build();
        
        // Створюємо зовнішню комірку з посиланням на внутрішню
        Cell outerCell = new CellBuilder()
            .storeUInt(32, 0x12345678)
            .storeRef(innerCell)
            .build();
        
        // Перевіряємо, що обидві комірки створені
        assertNotNull(innerCell);
        assertNotNull(outerCell);
        
        // Перевіряємо кількість референсів
        assertEquals(1, outerCell.getRefsCount());
        
        // Перевіряємо отримання референсу
        Cell refCell = outerCell.getRef(0);
        assertNotNull(refCell);
    }
    
    @Test
    public void testCellBitSize() {
        Cell cell = new CellBuilder()
            .storeUInt(16, 0x1234)
            .build();
            
        assertEquals(16, cell.getBitSize());
    }
    
    @Test
    public void testCellGetData() {
        Cell cell = new CellBuilder()
            .storeBytes(new byte[]{0x01, 0x02, 0x03})
            .build();
            
        byte[] data = cell.getData();
        assertNotNull(data);
        assertEquals(3, data.length);
        assertEquals(0x01, data[0]);
        assertEquals(0x02, data[1]);
        assertEquals(0x03, data[2]);
    }
    
    @Test
    public void testEmptyCell() {
        Cell cell = new CellBuilder().build();
        assertNotNull(cell);
        assertEquals(0, cell.getBitSize());
        assertEquals(0, cell.getData().length);
    }
    
    @Test
    public void testMaxRefs() {
        CellBuilder builder = new CellBuilder();
        
        // Додаємо максимальну кількість референсів (4)
        for (int i = 0; i < 4; i++) {
            Cell refCell = new CellBuilder().storeUInt(8, i).build();
            builder.storeRef(refCell);
        }
        
        Cell cell = builder.build();
        assertEquals(4, cell.getRefsCount());
        
        // Спроба додати ще один референс має викликати виняток
        Cell extraCell = new CellBuilder().build();
        assertThrows(RuntimeException.class, () -> {
            new CellBuilder()
                .storeRef(extraCell) // This should work
                .storeRef(extraCell)
                .storeRef(extraCell)
                .storeRef(extraCell)
                .storeRef(extraCell); // This should throw
        });
    }
}