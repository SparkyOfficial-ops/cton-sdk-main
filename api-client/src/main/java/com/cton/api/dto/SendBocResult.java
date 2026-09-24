// SendBocResult.java - DTO для результату надсилання BOC
// Author: Андрій Будильников (Sparky)
// DTO for BOC sending result
// DTO для результата отправки BOC

package com.cton.api.dto;

/**
 * DTO для результату надсилання BOC
 * 
 * DTO for BOC sending result
 * DTO для результата отправки BOC
 */
public class SendBocResult {
    private String hash;
    
    // Конструктор за замовчуванням
    // Default constructor
    // Конструктор по умолчанию
    public SendBocResult() {}
    
    // Геттери та сеттери
    // Getters and setters
    // Геттеры и сеттеры
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    @Override
    public String toString() {
        return "SendBocResult{" +
                "hash='" + hash + '\'' +
                '}';
    }
}