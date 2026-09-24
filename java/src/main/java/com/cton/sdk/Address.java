// Address.java - Java обгортка для Address C++ класу
// Author: Андрій Будильников (Sparky)

package com.cton.sdk;

import java.io.Closeable;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Java обгортка для Address C++ класу
 * 
 * TON адреса складається з:
 * - workchain (1 байт)
 * - hash part (32 байти)
 */
public class Address implements Closeable {
    // Інтерфейс для взаємодії з нативною бібліотекою
    public interface CtonLibrary extends Library {
        CtonLibrary INSTANCE = (CtonLibrary) Native.load("cton-sdk-core", CtonLibrary.class);
        
        // Створення нової адреси
        Pointer address_create();
        
        // Створення адреси з рядка
        Pointer address_create_from_string(String addressStr);
        
        // Звільнення пам'яті адреси
        void address_destroy(Pointer address);
        
        // Отримання workchain
        byte address_get_workchain(Pointer address);
        
        // Отримання hash part
        void address_get_hash_part(Pointer address, byte[] hashPart);
        
        // Отримання raw-представлення адреси
        Pointer address_to_raw(Pointer address);
        
        // Отримання user-friendly представлення адреси
        Pointer address_to_user_friendly(Pointer address, boolean isBounceable, boolean isTestOnly);
        
        // Перевірка валідності адреси
        boolean address_is_valid(Pointer address);
        
        // Встановлення workchain
        void address_set_workchain(Pointer address, byte workchain);
        
        // Встановлення hash part
        void address_set_hash_part(Pointer address, byte[] hashPart);
        
        // Функція для звільнення пам'яті рядків
        void free_string(Pointer str);
    }
    
    // Зробимо поле доступним для інших класів в тому самому пакеті
    // Make field accessible to other classes in the same package
    // Сделаем поле доступным для других классов в том же пакете
    Pointer nativeAddress;
    private boolean closed = false;
    
    /**
     * Конструктор за замовчуванням
     */
    public Address() {
        this.nativeAddress = CtonLibrary.INSTANCE.address_create();
    }
    
    /**
     * Конструктор для внутрішнього використання (для інших класів в тому самому пакеті)
     * Constructor for internal use (for other classes in the same package)
     * Конструктор для внутреннего использования (для других классов в том же пакете)
     */
    Address(Pointer nativeAddress) {
        this.nativeAddress = nativeAddress;
    }
    
    /**
     * Конструктор з рядка
     * @param addressStr рядок з адресою
     */
    public Address(String addressStr) {
        this.nativeAddress = CtonLibrary.INSTANCE.address_create_from_string(addressStr);
        if (this.nativeAddress == null) {
            throw new IllegalArgumentException("Invalid address string: " + addressStr);
        }
    }
    
    /**
     * Отримати робочий ланцюг
     * @return робочий ланцюг
     */
    public byte getWorkchain() {
        if (closed) {
            throw new IllegalStateException("Address has been closed");
        }
        return CtonLibrary.INSTANCE.address_get_workchain(nativeAddress);
    }
    
    /**
     * Встановити робочий ланцюг
     * @param workchain робочий ланцюг
     */
    public void setWorkchain(byte workchain) {
        if (closed) {
            throw new IllegalStateException("Address has been closed");
        }
        CtonLibrary.INSTANCE.address_set_workchain(nativeAddress, workchain);
    }
    
    /**
     * Отримати хеш-частину адреси
     * @return хеш-частина (32 байти)
     */
    public byte[] getHashPart() {
        if (closed) {
            throw new IllegalStateException("Address has been closed");
        }
        byte[] hashPart = new byte[32];
        CtonLibrary.INSTANCE.address_get_hash_part(nativeAddress, hashPart);
        return hashPart;
    }
    
    /**
     * Встановити хеш-частину адреси
     * @param hashPart хеш-частина (32 байти)
     */
    public void setHashPart(byte[] hashPart) {
        if (closed) {
            throw new IllegalStateException("Address has been closed");
        }
        if (hashPart.length != 32) {
            throw new IllegalArgumentException("Hash part must be exactly 32 bytes");
        }
        CtonLibrary.INSTANCE.address_set_hash_part(nativeAddress, hashPart);
    }
    
    /**
     * Отримати raw-представлення адреси
     * @return рядок з raw адресою
     */
    public String toRaw() {
        if (closed) {
            throw new IllegalStateException("Address has been closed");
        }
        Pointer rawPtr = CtonLibrary.INSTANCE.address_to_raw(nativeAddress);
        if (rawPtr == null) {
            return null;
        }
        String result = rawPtr.getString(0);
        CtonLibrary.INSTANCE.free_string(rawPtr);
        return result;
    }
    
    /**
     * Отримати user-friendly представлення адреси
     * @param isBounceable чи адреса bounceable
     * @param isTestOnly чи адреса для тестнету
     * @return рядок з user-friendly адресою
     */
    public String toUserFriendly(boolean isBounceable, boolean isTestOnly) {
        if (closed) {
            throw new IllegalStateException("Address has been closed");
        }
        Pointer ufPtr = CtonLibrary.INSTANCE.address_to_user_friendly(nativeAddress, isBounceable, isTestOnly);
        if (ufPtr == null) {
            return null;
        }
        String result = ufPtr.getString(0);
        CtonLibrary.INSTANCE.free_string(ufPtr);
        return result;
    }
    
    /**
     * Перевірити чи адреса коректна
     * @return true якщо адреса коректна
     */
    public boolean isValid() {
        if (closed) {
            throw new IllegalStateException("Address has been closed");
        }
        return CtonLibrary.INSTANCE.address_is_valid(nativeAddress);
    }
    
    /**
     * Закрити об'єкт і звільнити нативні ресурси
     */
    @Override
    public void close() {
        if (!closed && nativeAddress != null) {
            CtonLibrary.INSTANCE.address_destroy(nativeAddress);
            nativeAddress = null;
            closed = true;
        }
    }
}