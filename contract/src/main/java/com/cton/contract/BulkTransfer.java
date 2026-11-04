// BulkTransfer.java - клас для багато транзакцій
// Author: Андрій Будильников (Sparky)
// Bulk transfer class
// Класс для множественных транзакций

package com.cton.contract;

import java.math.BigInteger;

import com.cton.sdk.Address;
import com.cton.sdk.Cell;

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
    private final boolean bounce;
    private final Cell body;
    
    /**
     * Конструктор
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     * @param comment коментар (необов'язково)
     * @param mode режим транзакції
     * @param bounce {@code false} для виплати на гаманець, який може бути ще не активований
     */
    public BulkTransfer(Address destination, BigInteger amount, String comment, int mode, boolean bounce) {
        this(destination, amount, comment, mode, bounce, null);
    }

    /**
     * @param body готовое тело внутреннего сообщения, например Jetton {@code transfer}
     * @param amount прикреплённые наноTON (газ), не сумма токена
     */
    public BulkTransfer(Address destination, BigInteger amount, Cell body, int mode, boolean bounce) {
        this(destination, amount, null, mode, bounce, body);
    }

    private BulkTransfer(Address destination, BigInteger amount, String comment, int mode, boolean bounce, Cell body) {
        this.destination = destination;
        this.amount = amount;
        this.comment = comment;
        this.mode = mode;
        this.bounce = bounce;
        this.body = body;
    }

    /**
     * Конструктор
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     * @param comment коментар (необов'язково)
     * @param mode режим транзакції
     */
    public BulkTransfer(Address destination, BigInteger amount, String comment, int mode) {
        this(destination, amount, comment, mode, destination != null && destination.isBounceable());
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

    /**
     * @return {@code false} when the transfer must not bounce (non-bounceable, {@code UQ} / {@code 0Q})
     */
    public boolean isBounce() {
        return bounce;
    }

    /**
     * @return тело сообщения, если оно задано вместо текстового комментария
     */
    public Cell getBody() {
        return body;
    }
}