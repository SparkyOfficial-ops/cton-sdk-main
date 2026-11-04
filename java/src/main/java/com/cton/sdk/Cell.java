// Cell.java - Java обгортка для Cell C++ класу
// Author: Андрій Будильников (Sparky)

package com.cton.sdk;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Java обгортка для Cell C++ класу
 * 
 * Cell - це основна одиниця даних в TON, яка може містити:
 * - до 1023 бітів даних
 * - до 4 референсів на інші комірки
 */
public class Cell implements Closeable {
    // Інтерфейс для взаємодії з нативною бібліотекою
    public interface CtonLibrary extends Library {
        CtonLibrary INSTANCE = (CtonLibrary) Native.load("cton-sdk-core", CtonLibrary.class);
        
        // Створення нової комірки
        Pointer cell_create();
        
        // Звільнення пам'яті комірки
        void cell_destroy(Pointer cell);
        
        // Додавання беззнакового цілого до комірки
        boolean cell_store_uint(Pointer cell, int bits, long value);
        
        // Додавання знакового цілого до комірки
        boolean cell_store_int(Pointer cell, int bits, long value);
        
        // Додавання байтів до комірки
        boolean cell_store_bytes(Pointer cell, byte[] data, int length);
        
        // Додавання референсу до комірки
        boolean cell_store_ref(Pointer cell, Pointer refCell);
        
        // Отримання даних з комірки
        int cell_get_data(Pointer cell, byte[] buffer, int bufferSize);
        
        // Отримання розміру даних в бітах
        int cell_get_bit_size(Pointer cell);
        
        // Отримання кількості референсів
        int cell_get_refs_count(Pointer cell);
        
        // Отримання референсу за індексом
        Pointer cell_get_ref(Pointer cell, int index);
    }
    
    // Зробимо поле доступним для інших класів в тому самому пакеті
    // Make field accessible to other classes in the same package
    // Сделаем поле доступным для других классов в том же пакете
    Pointer nativeCell;
    private boolean closed = false;
    
    /**
     * Конструктор за замовчуванням
     */
    public Cell() {
        this.nativeCell = CtonLibrary.INSTANCE.cell_create();
    }
    
    /**
     * Конструктор для внутрішнього використання (для інших класів в тому самому пакеті)
     * Constructor for internal use (for other classes in the same package)
     * Конструктор для внутреннего использования (для других классов в том же пакете)
     */
    Cell(Pointer nativeCell) {
        this.nativeCell = nativeCell;
    }
    
    /**
     * Додати беззнакове ціле число до комірки
     * @param bits кількість бітів для зберігання
     * @param value значення для зберігання
     * @return this для ланцюгових викликів
     */
    public Cell storeUInt(int bits, long value) {
        if (closed) {
            throw new IllegalStateException("Cell has been closed");
        }
        if (!CtonLibrary.INSTANCE.cell_store_uint(nativeCell, bits, value)) {
            throw new RuntimeException("Failed to store uint in cell");
        }
        return this;
    }
    
    /**
     * Додати знакове ціле число до комірки
     * @param bits кількість бітів для зберігання
     * @param value значення для зберігання
     * @return this для ланцюгових викликів
     */
    public Cell storeInt(int bits, long value) {
        if (closed) {
            throw new IllegalStateException("Cell has been closed");
        }
        if (!CtonLibrary.INSTANCE.cell_store_int(nativeCell, bits, value)) {
            throw new RuntimeException("Failed to store int in cell");
        }
        return this;
    }
    
    /**
     * Додати байти до комірки
     * @param data дані для зберігання
     * @return this для ланцюгових викликів
     */
    public Cell storeBytes(byte[] data) {
        if (closed) {
            throw new IllegalStateException("Cell has been closed");
        }
        if (!CtonLibrary.INSTANCE.cell_store_bytes(nativeCell, data, data.length)) {
            throw new RuntimeException("Failed to store bytes in cell");
        }
        return this;
    }
    
    /**
     * Додати референс на іншу комірку
     * @param cell комірка для додавання як референс
     * @return this для ланцюгових викликів
     */
    public Cell storeRef(Cell cell) {
        if (closed) {
            throw new IllegalStateException("Cell has been closed");
        }
        if (!CtonLibrary.INSTANCE.cell_store_ref(nativeCell, cell.nativeCell)) {
            throw new RuntimeException("Failed to store reference in cell");
        }
        return this;
    }
    
    /**
     * Отримати дані з комірки
     * @return масив байтів з даними
     */
    public byte[] getData() {
        if (closed) {
            throw new IllegalStateException("Cell has been closed");
        }
        // Спочатку отримуємо розмір даних
        int bitSize = CtonLibrary.INSTANCE.cell_get_bit_size(nativeCell);
        int byteSize = (bitSize + 7) / 8; // Округлення вгору
        
        if (byteSize == 0) {
            return new byte[0];
        }
        
        byte[] buffer = new byte[byteSize];
        int result = CtonLibrary.INSTANCE.cell_get_data(nativeCell, buffer, byteSize);
        
        if (result < 0) {
            throw new RuntimeException("Failed to get data from cell");
        }
        
        return buffer;
    }
    
    /**
     * Отримати розмір даних в бітах
     * @return кількість бітів
     */
    public int getBitSize() {
        if (closed) {
            throw new IllegalStateException("Cell has been closed");
        }
        return CtonLibrary.INSTANCE.cell_get_bit_size(nativeCell);
    }
    
    public Slice toSlice() {
        return new Slice(this);
    }

    /**
     * SHA-256 of the standard cell representation: d1, d2, data with the
     * completion tag, then each reference's depth and hash.
     */
    public byte[] representationHash() {
        return reprHashAndDepth(this).hash;
    }

    private static final class HashDepth {
        final byte[] hash;
        final int depth;

        HashDepth(byte[] hash, int depth) {
            this.hash = hash;
            this.depth = depth;
        }
    }

    private static HashDepth reprHashAndDepth(Cell cell) {
        int bitSize = cell.getBitSize();
        byte[] data = cell.getData();
        int refsCount = cell.getRefsCount();
        int d1 = refsCount & 0x7F;
        int d2 = (bitSize / 8) + ((bitSize + 7) / 8);
        byte[] reprData = data.clone();
        if (bitSize % 8 != 0 && reprData.length > 0) {
            int last = reprData.length - 1;
            int mask = 0x80 >> (bitSize % 8);
            reprData[last] = (byte) (reprData[last] | mask);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(d1);
        out.write(d2);
        out.write(reprData, 0, reprData.length);
        int maxRefDepth = 0;
        for (int i = 0; i < refsCount; i++) {
            HashDepth child = reprHashAndDepth(cell.getRef(i));
            out.write((child.depth >> 8) & 0xFF);
            out.write(child.depth & 0xFF);
            out.write(child.hash, 0, child.hash.length);
            if (child.depth > maxRefDepth) {
                maxRefDepth = child.depth;
            }
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(out.toByteArray());
            int depth = refsCount == 0 ? 0 : maxRefDepth + 1;
            return new HashDepth(hash, depth);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public int getRefsCount() {
        if (closed) {
            throw new IllegalStateException("Cell has been closed");
        }
        return CtonLibrary.INSTANCE.cell_get_refs_count(nativeCell);
    }
    
    /**
     * Отримати референс за індексом
     * @param index індекс референсу
     * @return комірка-референс
     */
    public Cell getRef(int index) {
        if (closed) {
            throw new IllegalStateException("Cell has been closed");
        }
        Pointer refPtr = CtonLibrary.INSTANCE.cell_get_ref(nativeCell, index);
        if (refPtr == null) {
            return null;
        }
        return new Cell(refPtr);
    }
    
    /**
     * Закрити об'єкт і звільнити нативні ресурси
     */
    @Override
    public void close() {
        if (!closed && nativeCell != null) {
            CtonLibrary.INSTANCE.cell_destroy(nativeCell);
            nativeCell = null;
            closed = true;
        }
    }
}