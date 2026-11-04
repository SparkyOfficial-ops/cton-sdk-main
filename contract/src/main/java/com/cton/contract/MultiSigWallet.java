// MultiSigWallet.java - реалізація багатопідписного кошелька
// Author: Андрій Будильников (Sparky)
// Multi-signature wallet implementation
// Реализация многоподписного кошелька

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import com.cton.sdk.Crypto;

/**
 * Реалізація багатопідписного кошелька для TON блокчейну
 * 
 * Multi-signature wallet implementation for TON blockchain
 * Реализация многоподписного кошелька для TON блокчейна
 */
public class MultiSigWallet extends BaseWallet {
    private final List<Address> signers;
    private final int requiredSignatures;
    private final int subwalletId;
    
    /**
     * Конструктор багатопідписного кошелька
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     * @param signers список адрес підписувачів
     * @param requiredSignatures кількість необхідних підписів
     * @param subwalletId ідентифікатор підкошелька
     */
    public MultiSigWallet(Address address, TonApiClient apiClient, List<Address> signers, 
                         int requiredSignatures, int subwalletId) {
        super(address, apiClient);
        this.signers = new ArrayList<>(signers);
        this.requiredSignatures = requiredSignatures;
        this.subwalletId = subwalletId;
        
        // Перевірка валідності параметрів
        if (requiredSignatures > signers.size()) {
            throw new IllegalArgumentException("Required signatures cannot be greater than number of signers");
        }
        if (requiredSignatures <= 0) {
            throw new IllegalArgumentException("Required signatures must be positive");
        }
    }
    
    /**
     * Конструктор багатопідписного кошелька зі значенням subwalletId за замовчуванням
     * @param address адреса кошелька
     * @param apiClient API клієнт для взаємодії з мережею
     * @param signers список адрес підписувачів
     * @param requiredSignatures кількість необхідних підписів
     */
    public MultiSigWallet(Address address, TonApiClient apiClient, List<Address> signers, 
                         int requiredSignatures) {
        this(address, apiClient, signers, requiredSignatures, 698983191);
    }
    
    @Override
    public Cell createTransfer(Address destination, BigInteger amount, String comment) throws IOException {
        // Отримуємо послідовний номер
        long seqno = getSeqno();
        
        // Створюємо комірку з тілом повідомлення
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо код операції (0 для простого переказу)
        bodyBuilder.storeUInt(32, 0);
        
        // Додаємо коментар якщо він є
        if (comment != null && !comment.isEmpty()) {
            bodyBuilder.storeBytes(comment.getBytes());
        }
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення для багатопідписного кошелька
        CellBuilder messageBuilder = new CellBuilder();
        
        // Додаємо ідентифікатор запиту
        messageBuilder.storeUInt(8, 0); // expire_at (0 = no expiration)
        
        // Додаємо адресу одержувача
        messageBuilder.storeAddress(destination);
        
        // Додаємо суму
        messageBuilder.storeCoins(amount);
        
        // Додаємо тіло повідомлення
        messageBuilder.storeRef(body);
        
        return messageBuilder.build();
    }
    
    /**
     * Створити транзакцію, яка потребує багатопідпису
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     * @param comment коментар (необов'язково)
     * @return комірка з транзакцією, яка потребує багатопідпису
     * @throws IOException якщо сталася помилка
     */
    public Cell createMultiSigTransfer(Address destination, BigInteger amount, String comment) throws IOException {
        // Створюємо звичайну транзакцію
        Cell transfer = createTransfer(destination, amount, comment);
        
        // Створюємо спеціальне повідомлення для багатопідпису
        CellBuilder multiSigBuilder = new CellBuilder();
        
        // Додаємо seqno
        multiSigBuilder.storeUInt(32, getSeqno());
        
        // Додаємо кількість необхідних підписів
        multiSigBuilder.storeUInt(8, requiredSignatures);
        
        // Додаємо транзакцію
        multiSigBuilder.storeRef(transfer);
        
        return multiSigBuilder.build();
    }
    
    /**
     * Підписати транзакцію одним з підписувачів
     * @param transaction транзакція для підпису
     * @param privateKey приватний ключ підписувача
     * @return підписана транзакція
     * @throws IOException якщо сталася помилка
     */
    public Cell signTransaction(Cell transaction, Crypto.PrivateKey privateKey) throws IOException {
        // Серіалізуємо транзакцію
        Boc boc = new Boc(transaction);
        byte[] messageBytes = boc.serialize(true, true);
        
        // Підписуємо повідомлення приватним ключем
        byte[] signature = Crypto.sign(privateKey, messageBytes);
        
        // Створюємо комірку з підписом
        CellBuilder signatureBuilder = new CellBuilder();
        signatureBuilder.storeBytes(signature);
        signatureBuilder.storeRef(transaction);
        
        return signatureBuilder.build();
    }
    
    /**
     * Зібрати всі підписи в одну транзакцію
     * @param signedTransactions список підписаних транзакцій
     * @return фінальна транзакція для відправки
     * @throws IOException якщо сталася помилка
     */
    public Cell collectSignatures(List<Cell> signedTransactions) throws IOException {
        if (signedTransactions.size() < requiredSignatures) {
            throw new IllegalArgumentException("Not enough signatures provided");
        }
        
        // Створюємо фінальну комірку з усіма підписами
        CellBuilder finalBuilder = new CellBuilder();
        
        // Додаємо всі підписи
        for (int i = 0; i < Math.min(requiredSignatures, signedTransactions.size()); i++) {
            finalBuilder.storeRef(signedTransactions.get(i));
        }
        
        return finalBuilder.build();
    }
    
    /**
     * Отримати список адрес підписувачів
     * @return список адрес підписувачів
     */
    public List<Address> getSigners() {
        return new ArrayList<>(signers);
    }
    
    /**
     * Отримати кількість необхідних підписів
     * @return кількість необхідних підписів
     */
    public int getRequiredSignatures() {
        return requiredSignatures;
    }
    
    @Override
    public int getVersion() throws IOException {
        return 100; // Версія для багатопідписного кошелька
    }
    
    @Override
    public int getSubwalletId() throws IOException {
        return subwalletId;
    }
}