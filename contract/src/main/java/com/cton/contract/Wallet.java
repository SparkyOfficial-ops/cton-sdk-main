// Wallet.java - абстракція кошелька
// Author: Андрій Будильников (Sparky)
// Wallet abstraction
// Абстракция кошелька

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.sdk.Address;
import com.cton.sdk.Cell;

/**
 * Інтерфейс для роботи з TON кошельком
 * 
 * Interface for working with TON wallet
 * Интерфейс для работы с TON кошельком
 */
public interface Wallet {
    
    /**
     * Отримати адресу кошелька
     * @return адреса кошелька
     */
    Address getAddress();
    
    /**
     * Отримати баланс кошелька
     * @return баланс у нанотоні
     * @throws IOException якщо сталася помилка мережі
     */
    BigInteger getBalance() throws IOException;
    
    /**
     * Створити транзакцію для переказу коштів
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     * @param comment коментар (необов'язково)
     * @return комірка з транзакцією
     * @throws IOException якщо сталася помилка
     */
    Cell createTransfer(Address destination, BigInteger amount, String comment) throws IOException;
    
    /**
     * Створити транзакцію для переказу коштів без коментаря
     * @param destination адреса одержувача
     * @param amount сума в нанотоні
     * @return комірка з транзакцією
     * @throws IOException якщо сталася помилка
     */
    default Cell createTransfer(Address destination, BigInteger amount) throws IOException {
        return createTransfer(destination, amount, null);
    }
    
    /**
     * Надіслати транзакцію
     * @param transaction комірка з транзакцією
     * @throws IOException якщо сталася помилка мережі
     */
    void sendTransaction(Cell transaction) throws IOException;
    
    /**
     * Отримати послідовний номер (seqno) кошелька
     * @return послідовний номер
     * @throws IOException якщо сталася помилка мережі
     */
    long getSeqno() throws IOException;
    
    /**
     * Отримати версію кошелька
     * @return версія кошелька
     * @throws IOException якщо сталася помилка мережі
     */
    int getVersion() throws IOException;
    
    /**
     * Отримати ідентифікатор підкошелька
     * @return ідентифікатор підкошелька
     * @throws IOException якщо сталася помилка мережі
     */
    default int getSubwalletId() throws IOException {
        return 698983191; // Default subwallet ID
    }
    
    /**
     * Створити багато транзакцій для високонавантажених кошельків
     * @param transfers масив транзакцій
     * @return комірка з багатьма транзакціями
     * @throws IOException якщо сталася помилка
     */
    default Cell createBulkTransfer(com.cton.contract.BulkTransfer[] transfers) throws IOException {
        throw new UnsupportedOperationException("Bulk transfer not supported by this wallet type");
    }
}