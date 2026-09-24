// DeFiContracts.java - DeFi контракти для ліквідності, стейкінгу та кредитування
// Author: Андрій Будильников (Sparky)
// DeFi contracts for liquidity, staking and lending
// DeFi контракты для ликвидности, стейкинга и кредитования

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import com.cton.sdk.Crypto;

/**
 * DeFi контракти для ліквідності, стейкінгу та кредитування
 * 
 * DeFi contracts for liquidity, staking and lending
 * DeFi контракты для ликвидности, стейкинга и кредитования
 */
public class DeFiContracts extends Contract {
    
    /**
     * Конструктор
     * @param address адреса контракту
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public DeFiContracts(Address address, TonApiClient apiClient) {
        super(address, apiClient);
    }
    
    /**
     * Додати ліквідність до пулу
     * Add liquidity to pool
     * Добавить ликвидность в пул
     * 
     * @param token0Amount кількість токенів першого типу
     * @param token1Amount кількість токенів другого типу
     * @param liquidityWalletAddress адреса кошелька ліквідності
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void addLiquidity(BigInteger token0Amount, BigInteger token1Amount, 
                            Address liquidityWalletAddress, Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для додавання ліквідності
        // Создаем ячейку с телом сообщения для добавления ликвидности
        // Create cell with message body for adding liquidity
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для додавання ліквідності (0x12345679)
        // Добавляем opcode для добавления ликвидности (0x12345679)
        // Add opcode for adding liquidity (0x12345679)
        bodyBuilder.storeUInt(32, 0x12345679);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо кількість токенів першого типу
        // Добавляем количество токенов первого типа
        // Add amount of first token type
        bodyBuilder.storeCoins(token0Amount);
        
        // Додаємо кількість токенів другого типу
        // Добавляем количество токенов второго типа
        // Add amount of second token type
        bodyBuilder.storeCoins(token1Amount);
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення
        // Создаем внешнее сообщение
        // Create external message
        CellBuilder messageBuilder = new CellBuilder();
        
        // Створюємо BOC для повідомлення
        // Создаем BOC для сообщения
        // Create BOC for message
        Boc boc = new Boc(body);
        byte[] messageBytes = boc.serialize(true, true);
        
        // Підписуємо повідомлення приватним ключем
        // Подписываем сообщение приватным ключом
        // Sign message with private key
        byte[] signature = Crypto.sign(privateKey, messageBytes);
        
        // Створюємо зовнішнє повідомлення з підписом
        // Создаем внешнее сообщение с подписью
        // Create external message with signature
        messageBuilder.storeBytes(signature);
        messageBuilder.storeRef(body);
        
        Cell message = messageBuilder.build();
        
        // Надсилаємо повідомлення через API
        // Отправляем сообщение через API
        // Send message through API
        if (apiClient != null) {
            apiClient.sendBoc(messageBytes);
        } else {
            throw new IOException("API client not set");
        }
    }
    
    /**
     * Видалити ліквідність з пулу
     * Remove liquidity from pool
     * Удалить ликвидность из пула
     * 
     * @param liquidityAmount кількість ліквідних токенів для видалення
     * @param liquidityWalletAddress адреса кошелька ліквідності
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void removeLiquidity(BigInteger liquidityAmount, Address liquidityWalletAddress,
                               Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для видалення ліквідності
        // Создаем ячейку с телом сообщения для удаления ликвидности
        // Create cell with message body for removing liquidity
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для видалення ліквідності (0x98765432)
        // Добавляем opcode для удаления ликвидности (0x98765432)
        // Add opcode for removing liquidity (0x98765432)
        bodyBuilder.storeUInt(32, 0x98765432);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо кількість ліквідних токенів для видалення
        // Добавляем количество ликвидных токенов для удаления
        // Add amount of liquidity tokens to remove
        bodyBuilder.storeCoins(liquidityAmount);
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення
        // Создаем внешнее сообщение
        // Create external message
        CellBuilder messageBuilder = new CellBuilder();
        
        // Створюємо BOC для повідомлення
        // Создаем BOC для сообщения
        // Create BOC for message
        Boc boc = new Boc(body);
        byte[] messageBytes = boc.serialize(true, true);
        
        // Підписуємо повідомлення приватним ключем
        // Подписываем сообщение приватным ключом
        // Sign message with private key
        byte[] signature = Crypto.sign(privateKey, messageBytes);
        
        // Створюємо зовнішнє повідомлення з підписом
        // Создаем внешнее сообщение с подписью
        // Create external message with signature
        messageBuilder.storeBytes(signature);
        messageBuilder.storeRef(body);
        
        Cell message = messageBuilder.build();
        
        // Надсилаємо повідомлення через API
        // Отправляем сообщение через API
        // Send message through API
        if (apiClient != null) {
            apiClient.sendBoc(messageBytes);
        } else {
            throw new IOException("API client not set");
        }
    }
    
    /**
     * Стейкінг токенів
     * Staking tokens
     * Стейкинг токенов
     * 
     * @param amount кількість токенів для стейкінгу
     * @param stakingWalletAddress адреса кошелька стейкінгу
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void stakeTokens(BigInteger amount, Address stakingWalletAddress,
                           Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для стейкінгу
        // Создаем ячейку с телом сообщения для стейкинга
        // Create cell with message body for staking
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для стейкінгу (0x11111111)
        // Добавляем opcode для стейкинга (0x11111111)
        // Add opcode for staking (0x11111111)
        bodyBuilder.storeUInt(32, 0x11111111);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо кількість токенів для стейкінгу
        // Добавляем количество токенов для стейкинга
        // Add amount of tokens to stake
        bodyBuilder.storeCoins(amount);
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення
        // Создаем внешнее сообщение
        // Create external message
        CellBuilder messageBuilder = new CellBuilder();
        
        // Створюємо BOC для повідомлення
        // Создаем BOC для сообщения
        // Create BOC for message
        Boc boc = new Boc(body);
        byte[] messageBytes = boc.serialize(true, true);
        
        // Підписуємо повідомлення приватним ключем
        // Подписываем сообщение приватным ключом
        // Sign message with private key
        byte[] signature = Crypto.sign(privateKey, messageBytes);
        
        // Створюємо зовнішнє повідомлення з підписом
        // Создаем внешнее сообщение с подписью
        // Create external message with signature
        messageBuilder.storeBytes(signature);
        messageBuilder.storeRef(body);
        
        Cell message = messageBuilder.build();
        
        // Надсилаємо повідомлення через API
        // Отправляем сообщение через API
        // Send message through API
        if (apiClient != null) {
            apiClient.sendBoc(messageBytes);
        } else {
            throw new IOException("API client not set");
        }
    }
    
    /**
     * Вивести стейкінг токенів
     * Withdraw staked tokens
     * Вывести стейкинг токенов
     * 
     * @param amount кількість токенів для виведення
     * @param stakingWalletAddress адреса кошелька стейкінгу
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void unstakeTokens(BigInteger amount, Address stakingWalletAddress,
                             Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для виведення стейкінгу
        // Создаем ячейку с телом сообщения для вывода стейкинга
        // Create cell with message body for unstaking
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для виведення стейкінгу (0x22222222)
        // Добавляем opcode для вывода стейкинга (0x22222222)
        // Add opcode for unstaking (0x22222222)
        bodyBuilder.storeUInt(32, 0x22222222);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо кількість токенів для виведення
        // Добавляем количество токенов для вывода
        // Add amount of tokens to withdraw
        bodyBuilder.storeCoins(amount);
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення
        // Создаем внешнее сообщение
        // Create external message
        CellBuilder messageBuilder = new CellBuilder();
        
        // Створюємо BOC для повідомлення
        // Создаем BOC для сообщения
        // Create BOC for message
        Boc boc = new Boc(body);
        byte[] messageBytes = boc.serialize(true, true);
        
        // Підписуємо повідомлення приватним ключем
        // Подписываем сообщение приватным ключом
        // Sign message with private key
        byte[] signature = Crypto.sign(privateKey, messageBytes);
        
        // Створюємо зовнішнє повідомлення з підписом
        // Создаем внешнее сообщение с подписью
        // Create external message with signature
        messageBuilder.storeBytes(signature);
        messageBuilder.storeRef(body);
        
        Cell message = messageBuilder.build();
        
        // Надсилаємо повідомлення через API
        // Отправляем сообщение через API
        // Send message through API
        if (apiClient != null) {
            apiClient.sendBoc(messageBytes);
        } else {
            throw new IOException("API client not set");
        }
    }
    
    /**
     * Взяти кредит
     * Take a loan
     * Взять кредит
     * 
     * @param collateralAmount кількість токенів в якості застави
     * @param loanAmount кількість токенів для позики
     * @param lendingWalletAddress адреса кошелька кредитування
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void takeLoan(BigInteger collateralAmount, BigInteger loanAmount,
                        Address lendingWalletAddress, Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для взяття кредиту
        // Создаем ячейку с телом сообщения для взятия кредита
        // Create cell with message body for taking a loan
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для взяття кредиту (0x33333333)
        // Добавляем opcode для взятия кредита (0x33333333)
        // Add opcode for taking a loan (0x33333333)
        bodyBuilder.storeUInt(32, 0x33333333);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо кількість токенів в якості застави
        // Добавляем количество токенов в качестве залога
        // Add amount of tokens as collateral
        bodyBuilder.storeCoins(collateralAmount);
        
        // Додаємо кількість токенів для позики
        // Добавляем количество токенов для кредита
        // Add amount of tokens for loan
        bodyBuilder.storeCoins(loanAmount);
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення
        // Создаем внешнее сообщение
        // Create external message
        CellBuilder messageBuilder = new CellBuilder();
        
        // Створюємо BOC для повідомлення
        // Создаем BOC для сообщения
        // Create BOC for message
        Boc boc = new Boc(body);
        byte[] messageBytes = boc.serialize(true, true);
        
        // Підписуємо повідомлення приватним ключем
        // Подписываем сообщение приватным ключом
        // Sign message with private key
        byte[] signature = Crypto.sign(privateKey, messageBytes);
        
        // Створюємо зовнішнє повідомлення з підписом
        // Создаем внешнее сообщение с подписью
        // Create external message with signature
        messageBuilder.storeBytes(signature);
        messageBuilder.storeRef(body);
        
        Cell message = messageBuilder.build();
        
        // Надсилаємо повідомлення через API
        // Отправляем сообщение через API
        // Send message through API
        if (apiClient != null) {
            apiClient.sendBoc(messageBytes);
        } else {
            throw new IOException("API client not set");
        }
    }
    
    /**
     * Погасити кредит
     * Repay a loan
     * Погасить кредит
     * 
     * @param repaymentAmount кількість токенів для погашення
     * @param lendingWalletAddress адреса кошелька кредитування
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void repayLoan(BigInteger repaymentAmount, Address lendingWalletAddress,
                         Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для погашення кредиту
        // Создаем ячейку с телом сообщения для погашения кредита
        // Create cell with message body for repaying a loan
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для погашення кредиту (0x44444444)
        // Добавляем opcode для погашения кредита (0x44444444)
        // Add opcode for repaying a loan (0x44444444)
        bodyBuilder.storeUInt(32, 0x44444444);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо кількість токенів для погашення
        // Добавляем количество токенов для погашения
        // Add amount of tokens for repayment
        bodyBuilder.storeCoins(repaymentAmount);
        
        Cell body = bodyBuilder.build();
        
        // Створюємо зовнішнє повідомлення
        // Создаем внешнее сообщение
        // Create external message
        CellBuilder messageBuilder = new CellBuilder();
        
        // Створюємо BOC для повідомлення
        // Создаем BOC для сообщения
        // Create BOC for message
        Boc boc = new Boc(body);
        byte[] messageBytes = boc.serialize(true, true);
        
        // Підписуємо повідомлення приватним ключем
        // Подписываем сообщение приватным ключом
        // Sign message with private key
        byte[] signature = Crypto.sign(privateKey, messageBytes);
        
        // Створюємо зовнішнє повідомлення з підписом
        // Создаем внешнее сообщение с подписью
        // Create external message with signature
        messageBuilder.storeBytes(signature);
        messageBuilder.storeRef(body);
        
        Cell message = messageBuilder.build();
        
        // Надсилаємо повідомлення через API
        // Отправляем сообщение через API
        // Send message through API
        if (apiClient != null) {
            apiClient.sendBoc(messageBytes);
        } else {
            throw new IOException("API client not set");
        }
    }
}