// AddressInformation.java - DTO для інформації про адресу
// Author: Андрій Будильников (Sparky)
// DTO for address information
// DTO для информации об адресе

package com.cton.api.dto;

/**
 * DTO для інформації про адресу TON
 * 
 * DTO for TON address information
 * DTO для информации об адресе TON
 */
public class AddressInformation {
    private String balance;
    private String state;
    private String data;
    private String code;
    private String lastTransactionId;
    
    // Конструктор за замовчуванням
    // Default constructor
    // Конструктор по умолчанию
    public AddressInformation() {}
    
    // Геттери та сеттери
    // Getters and setters
    // Геттеры и сеттеры
    
    public String getBalance() {
        return balance;
    }
    
    public void setBalance(String balance) {
        this.balance = balance;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getLastTransactionId() {
        return lastTransactionId;
    }
    
    public void setLastTransactionId(String lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }
    
    @Override
    public String toString() {
        return "AddressInformation{" +
                "balance='" + balance + '\'' +
                ", state='" + state + '\'' +
                ", data='" + data + '\'' +
                ", code='" + code + '\'' +
                ", lastTransactionId='" + lastTransactionId + '\'' +
                '}';
    }
}