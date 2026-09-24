// MessageFactory.java - фабрика повідомлень для TON
// Author: Андрій Будильников (Sparky)
// Message factory for TON
// Фабрика сообщений для TON

package com.cton.contract;

import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import java.math.BigInteger;
import java.io.IOException;

/**
 * Фабрика для створення різних типів повідомлень TON
 * 
 * Factory for creating different types of TON messages
 * Фабрика для создания различных типов сообщений TON
 */
public class MessageFactory {
    
    /**
     * Створити внутрішнє повідомлення
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     * @param body тіло повідомлення (може бути null)
     * @param mode режим надсилання
     * @return комірка з внутрішнім повідомленням
     * @throws IOException якщо сталася помилка
     */
    public static Cell createInternalMessage(Address destination, BigInteger amount, Cell body, int mode) throws IOException {
        // Створюємо комірку для внутрішнього повідомлення
        // Создаем ячейку для внутреннего сообщения
        // Create cell for internal message
        CellBuilder builder = new CellBuilder();
        
        // Додаємо прапори (ініціалізуємо як 0)
        // Добавляем флаги (инициализируем как 0)
        // Add flags (initialize as 0)
        builder.storeUInt(1, 0); // ihr_disabled
        builder.storeUInt(1, 1); // bounce
        builder.storeUInt(1, 0); // bounced
        
        // Додаємо адресу одержувача
        // Добавляем адрес получателя
        // Add recipient address
        builder.storeAddress(destination);
        
        // Додаємо суму
        // Добавляем сумму
        // Add amount
        builder.storeCoins(amount);
        
        // Додаємо extra currencies (ініціалізуємо як 0)
        // Добавляем дополнительные валюты (инициализируем как 0)
        // Add extra currencies (initialize as 0)
        builder.storeUInt(1, 0); // extra currencies flag
        
        // Додаємо ініціалізаційні параметри (ініціалізуємо як 0)
        // Добавляем инициализационные параметры (инициализируем как 0)
        // Add init parameters (initialize as 0)
        builder.storeUInt(1, 0); // init flag
        
        // Додаємо тіло повідомлення
        // Добавляем тело сообщения
        // Add message body
        if (body != null) {
            builder.storeUInt(1, 1); // body flag
            builder.storeRef(body);
        } else {
            builder.storeUInt(1, 0); // body flag
        }
        
        return builder.build();
    }
    
    /**
     * Створити зовнішнє повідомлення
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     * @param body тіло повідомлення (може бути null)
     * @return комірка з зовнішнім повідомленням
     * @throws IOException якщо сталася помилка
     */
    public static Cell createExternalMessage(Address destination, BigInteger amount, Cell body) throws IOException {
        // Створюємо комірку для зовнішнього повідомлення
        // Создаем ячейку для внешнего сообщения
        // Create cell for external message
        CellBuilder builder = new CellBuilder();
        
        // Додаємо прапори (зовнішнє повідомлення)
        // Добавляем флаги (внешнее сообщение)
        // Add flags (external message)
        builder.storeUInt(2, 2); // ext_in_msg_info$10
        
        // Додаємо адресу відправника (null для зовнішніх повідомлень)
        // Добавляем адрес отправителя (null для внешних сообщений)
        // Add sender address (null for external messages)
        builder.storeUInt(2, 0); // addr_none$00
        
        // Додаємо адресу одержувача
        // Добавляем адрес получателя
        // Add recipient address
        builder.storeAddress(destination);
        
        // Додаємо ініціалізаційні параметри (ініціалізуємо як 0)
        // Добавляем инициализационные параметры (инициализируем как 0)
        // Add init parameters (initialize as 0)
        builder.storeUInt(1, 0); // init flag
        
        // Додаємо тіло повідомлення
        // Добавляем тело сообщения
        // Add message body
        if (body != null) {
            builder.storeRef(body);
        }
        
        return builder.build();
    }
    
    /**
     * Створити просте повідомлення з текстовим коментарем
     * @param text текст коментаря
     * @return комірка з текстовим повідомленням
     * @throws IOException якщо сталася помилка
     */
    public static Cell createTextMessage(String text) throws IOException {
        if (text == null || text.isEmpty()) {
            // Повертаємо порожню комірку
            // Возвращаем пустую ячейку
            // Return empty cell
            return new CellBuilder().build();
        }
        
        // Створюємо комірку з текстовим повідомленням
        // Создаем ячейку с текстовым сообщением
        // Create cell with text message
        CellBuilder builder = new CellBuilder();
        
        // Додаємо операційний код (0 для текстового повідомлення)
        // Добавляем операционный код (0 для текстового сообщения)
        // Add operation code (0 for text message)
        builder.storeUInt(32, 0);
        
        // Додаємо текст
        // Добавляем текст
        // Add text
        builder.storeBytes(text.getBytes());
        
        return builder.build();
    }
}