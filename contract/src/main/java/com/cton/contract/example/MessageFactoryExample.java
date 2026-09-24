// MessageFactoryExample.java - приклад використання MessageFactory
// Author: Андрій Будильников (Sparky)
// Example of using MessageFactory
// Пример использования MessageFactory

package com.cton.contract.example;

import com.cton.contract.MessageFactory;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import java.math.BigInteger;
import java.io.IOException;

/**
 * Приклад використання MessageFactory
 */
public class MessageFactoryExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("CTON-SDK MessageFactory Example");
            System.out.println("===============================");
            
            // Створюємо адресу одержувача
            // Создаем адрес получателя
            // Create recipient address
            Address recipient = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
            
            // Створюємо текстове повідомлення
            // Создаем текстовое сообщение
            // Create text message
            Cell textMessage = MessageFactory.createTextMessage("Hello from CTON-SDK!");
            System.out.println("Created text message");
            
            // Створюємо внутрішнє повідомлення
            // Создаем внутреннее сообщение
            // Create internal message
            BigInteger amount = BigInteger.valueOf(1000000000L); // 1 TON
            Cell internalMessage = MessageFactory.createInternalMessage(recipient, amount, textMessage, 0);
            System.out.println("Created internal message");
            
            // Створюємо зовнішнє повідомлення
            // Создаем внешнее сообщение
            // Create external message
            Cell externalMessage = MessageFactory.createExternalMessage(recipient, amount, textMessage);
            System.out.println("Created external message");
            
            System.out.println("All message types created successfully!");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}