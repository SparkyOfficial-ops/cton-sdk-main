// JettonExtensions.java - розширені функції для Jetton токенів
// Author: Андрій Будильников (Sparky)
// Extended functions for Jetton tokens
// Расширенные функции для Jetton токенов

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Boc;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;
import com.cton.sdk.Crypto;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Розширені функції для Jetton токенів
 * 
 * Extended functions for Jetton tokens
 * Расширенные функции для Jetton токенов
 */
public class JettonExtensions extends Jetton {
    
    /**
     * Конструктор
     * @param address адреса контракту токена
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public JettonExtensions(Address address, TonApiClient apiClient) {
        super(address, apiClient);
    }
    
    /**
     * Безкомісійний переказ jetton токенів
     * Feeless transfer of jetton tokens
     * Бесплатный перевод jetton токенов
     * 
     * @param fromWalletAddress адреса кошелька відправника
     * @param toAddress адреса одержувача
     * @param amount кількість токенів
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void feelessTransfer(Address fromWalletAddress, Address toAddress, BigInteger amount, 
                               Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для безкомісійного переказу
        // Создаем ячейку с телом сообщения для бесплатного перевода
        // Create cell with message body for feeless transfer
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для transfer (0xf8a7ea5)
        // Добавляем opcode для transfer (0xf8a7ea5)
        // Add opcode for transfer (0xf8a7ea5)
        bodyBuilder.storeUInt(32, 0xf8a7ea5);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо кількість токенів
        // Добавляем количество токенов
        // Add token amount
        bodyBuilder.storeCoins(amount);
        
        // Додаємо адресу одержувача
        // Добавляем адрес получателя
        // Add recipient address
        bodyBuilder.storeAddress(toAddress);
        
        // Додаємо адресу відправника (response destination)
        // Добавляем адрес отправителя (response destination)
        // Add sender address (response destination)
        bodyBuilder.storeAddress(fromWalletAddress);
        
        // Додаємо прапор custom_payload (0 - немає)
        // Добавляем флаг custom_payload (0 - нет)
        // Add custom_payload flag (0 - none)
        bodyBuilder.storeUInt(1, 0);
        
        // Додаємо forward_amount (0 для безкомісійного переказу)
        // Добавляем forward_amount (0 для бесплатного перевода)
        // Add forward_amount (0 for feeless transfer)
        bodyBuilder.storeCoins(BigInteger.ZERO);
        
        // Додаємо прапор forward_payload (0 - немає)
        // Добавляем флаг forward_payload (0 - нет)
        // Add forward_payload flag (0 - none)
        bodyBuilder.storeUInt(1, 0);
        
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
     * Створення vesting розкладу для токенів
     * Create vesting schedule for tokens
     * Создание vesting расписания для токенов
     * 
     * @param beneficiary адреса бенефіціара
     * @param totalAmount загальна кількість токенів
     * @param startDate дата початку vesting
     * @param endDate дата закінчення vesting
     * @param cliffDate дата кліфу (коли починається розблокування)
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void createVestingSchedule(Address beneficiary, BigInteger totalAmount, 
                                     Date startDate, Date endDate, Date cliffDate,
                                     Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для створення vesting розкладу
        // Создаем ячейку с телом сообщения для создания vesting расписания
        // Create cell with message body for creating vesting schedule
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для створення vesting розкладу (0x12345678)
        // Добавляем opcode для создания vesting расписания (0x12345678)
        // Add opcode for creating vesting schedule (0x12345678)
        bodyBuilder.storeUInt(32, 0x12345678);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо адресу бенефіціара
        // Добавляем адрес бенефициара
        // Add beneficiary address
        bodyBuilder.storeAddress(beneficiary);
        
        // Додаємо загальну кількість токенів
        // Добавляем общее количество токенов
        // Add total token amount
        bodyBuilder.storeCoins(totalAmount);
        
        // Додаємо дату початку (в секундах)
        // Добавляем дату начала (в секундах)
        // Add start date (in seconds)
        bodyBuilder.storeUInt(32, startDate.getTime() / 1000);
        
        // Додаємо дату закінчення (в секундах)
        // Добавляем дату окончания (в секундах)
        // Add end date (in seconds)
        bodyBuilder.storeUInt(32, endDate.getTime() / 1000);
        
        // Додаємо дату кліфу (в секундах)
        // Добавляем дату клифа (в секундах)
        // Add cliff date (in seconds)
        bodyBuilder.storeUInt(32, cliffDate.getTime() / 1000);
        
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
     * Голосування за пропозицію у governance
     * Vote for a proposal in governance
     * Голосование за предложение в governance
     * 
     * @param proposalId ідентифікатор пропозиції
     * @param vote голос (true - за, false - проти)
     * @param voterWalletAddress адреса кошелька виборця
     * @param privateKey приватний ключ для підпису
     * @throws IOException якщо сталася помилка мережі
     */
    public void voteOnProposal(long proposalId, boolean vote, Address voterWalletAddress,
                              Crypto.PrivateKey privateKey) throws IOException {
        // Створюємо комірку з тілом повідомлення для голосування
        // Создаем ячейку с телом сообщения для голосования
        // Create cell with message body for voting
        CellBuilder bodyBuilder = new CellBuilder();
        
        // Додаємо opcode для голосування (0x87654321)
        // Добавляем opcode для голосования (0x87654321)
        // Add opcode for voting (0x87654321)
        bodyBuilder.storeUInt(32, 0x87654321);
        
        // Додаємо query_id (0 для простоти)
        // Добавляем query_id (0 для простоты)
        // Add query_id (0 for simplicity)
        bodyBuilder.storeUInt(64, 0);
        
        // Додаємо ідентифікатор пропозиції
        // Добавляем идентификатор предложения
        // Add proposal ID
        bodyBuilder.storeUInt(32, proposalId);
        
        // Додаємо голос (1 - за, 0 - проти)
        // Добавляем голос (1 - за, 0 - против)
        // Add vote (1 - for, 0 - against)
        bodyBuilder.storeUInt(1, vote ? 1 : 0);
        
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
     * Отримати інформацію про пропозицію governance
     * Get information about governance proposal
     * Получить информацию о предложении governance
     * 
     * @param proposalId ідентифікатор пропозиції
     * @return інформація про пропозицію
     * @throws IOException якщо сталася помилка мережі
     */
    public JsonObject getProposalInfo(long proposalId) throws IOException {
        JsonObject stack = new JsonObject();
        
        // Додаємо ідентифікатор пропозиції до стеку
        // Добавляем идентификатор предложения в стек
        // Add proposal ID to stack
        JsonArray proposalArray = new JsonArray();
        proposalArray.add("tvm.Slice");
        proposalArray.add(String.valueOf(proposalId));
        stack.add("proposal_id", proposalArray);
        
        return runGetMethod("get_proposal_data", stack);
    }
}