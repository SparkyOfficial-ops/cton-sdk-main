// BulkTransfer.java - клас для багато транзакцій
// Author: Андрій Будильников (Sparky)
// Bulk transfer class
// Класс для множественных транзакций

package com.cton.contract;

import java.math.BigInteger;

import com.cton.sdk.Address;

/**
 * Представляє одну транзакцію в багатотранзакційному переказі
 * 
 * Represents a single transaction in a bulk transfer
 * Представляет одну транзакцию в множественном переводе
 */
public class BulkTransfer {
    private final Address destination;
    private final BigInteger amount;
    private final String comment;
    private final int mode;
    
    /**
     * Конструктор
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     * @param comment коментар (необов'язково)
     * @param mode режим транзакції
     */
    public BulkTransfer(Address destination, BigInteger amount, String comment, int mode) {
        this.destination = destination;
        this.amount = amount;
        this.comment = comment;
        this.mode = mode;
    }
    
    /**
     * Конструктор з режимом за замовчуванням
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     * @param comment коментар (необов'язково)
     */
    public BulkTransfer(Address destination, BigInteger amount, String comment) {
        this(destination, amount, comment, 3);
    }
    
    /**
     * Конструктор без коментаря
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     */
    public BulkTransfer(Address destination, BigInteger amount) {
        this(destination, amount, null, 3);
    }
    
    // Геттери
    // Getters
    // Геттеры
    public Address getDestination() {
        return destination;
    }
    
    public BigInteger getAmount() {
        return amount;
    }
    
    public String getComment() {
        return comment;
    }
    
    public int getMode() {
        return mode;
    }
}