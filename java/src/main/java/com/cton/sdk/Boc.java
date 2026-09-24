// Boc.java - Java обгортка для Boc C++ класу
// Author: Андрій Будильников (Sparky)

package com.cton.sdk;

import java.io.Closeable;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Java обгортка для Boc C++ класу
 * 
 * BOC (Bag of Cells) - це формат серіалізації дерева комірок (Cell)
 * в бінарне представлення для передачі по мережі або зберігання
 */
public class Boc implements Closeable {
    // Інтерфейс для взаємодії з нативною бібліотекою
    public interface CtonLibrary extends Library {
        CtonLibrary INSTANCE = (CtonLibrary) Native.load("cton-sdk-core", CtonLibrary.class);
        
        // Створення нового BOC
        Pointer boc_create();
        
        // Створення BOC з кореневої комірки
        Pointer boc_create_with_root(Pointer rootCell);
        
        // Звільнення пам'яті BOC
        void boc_destroy(Pointer boc);
        
        // Серіалізувати BOC в бінарне представлення
        Pointer boc_serialize(Pointer boc, boolean hasIdx, boolean hashCRC);
        
        // Отримати розмір серіалізованих даних BOC
        int boc_get_serialized_size(Pointer boc, boolean hasIdx, boolean hashCRC);
        
        // Десеріалізувати BOC з бінарного представлення
        Pointer boc_deserialize(byte[] data, int length);
        
        // Отримати кореневу комірку
        Pointer boc_get_root(Pointer boc);
        
        // Встановити кореневу комірку
        void boc_set_root(Pointer boc, Pointer rootCell);
        
        // Функція для звільнення пам'яті
        void free_string(Pointer str);
    }
    
    private Pointer nativeBoc;
    private boolean closed = false;
    
    /**
     * Конструктор за замовчуванням
     */
    public Boc() {
        this.nativeBoc = CtonLibrary.INSTANCE.boc_create();
    }
    
    /**
     * Конструктор з кореневої комірки
     * @param root коренева комірка
     */
    public Boc(Cell root) {
        this.nativeBoc = CtonLibrary.INSTANCE.boc_create_with_root(root.nativeCell);
    }
    
    /**
     * Приватний конструктор для внутрішнього використання
     */
    Boc(Pointer nativeBoc) {
        this.nativeBoc = nativeBoc;
    }
    
    /**
     * Серіалізувати BOC в бінарне представлення
     * @param hasIdx чи включати індекс
     * @param hashCRC чи включати CRC хеш
     * @return бінарне представлення BOC
     */
    public byte[] serialize(boolean hasIdx, boolean hashCRC) {
        if (closed) {
            throw new IllegalStateException("Boc has been closed");
        }
        
        // Отримуємо розмір серіалізованих даних
        int size = CtonLibrary.INSTANCE.boc_get_serialized_size(nativeBoc, hasIdx, hashCRC);
        if (size <= 0) {
            return new byte[0];
        }
        
        // Серіалізуємо дані
        Pointer dataPtr = CtonLibrary.INSTANCE.boc_serialize(nativeBoc, hasIdx, hashCRC);
        if (dataPtr == null) {
            return new byte[0];
        }
        
        // Конвертуємо Pointer в byte[]
        byte[] result = dataPtr.getByteArray(0, size);
        
        // Звільняємо пам'ять, виділену в C++
        Native.free(Pointer.nativeValue(dataPtr));
        
        return result;
    }
    
    /**
     * Десеріалізувати BOC з бінарного представлення
     * @param data бінарні дані BOC
     * @return об'єкт Boc
     */
    public static Boc deserialize(byte[] data) {
        Pointer bocPtr = CtonLibrary.INSTANCE.boc_deserialize(data, data.length);
        return new Boc(bocPtr);
    }
    
    /**
     * Отримати кореневу комірку
     * @return коренева комірка
     */
    public Cell getRoot() {
        if (closed) {
            throw new IllegalStateException("Boc has been closed");
        }
        Pointer cellPtr = CtonLibrary.INSTANCE.boc_get_root(nativeBoc);
        return new Cell(cellPtr);
    }
    
    /**
     * Встановити кореневу комірку
     * @param root нова коренева комірка
     */
    public void setRoot(Cell root) {
        if (closed) {
            throw new IllegalStateException("Boc has been closed");
        }
        CtonLibrary.INSTANCE.boc_set_root(nativeBoc, root.nativeCell);
    }
    
    /**
     * Закрити об'єкт і звільнити нативні ресурси
     */
    @Override
    public void close() {
        if (!closed && nativeBoc != null) {
            CtonLibrary.INSTANCE.boc_destroy(nativeBoc);
            nativeBoc = null;
            closed = true;
        }
    }
}