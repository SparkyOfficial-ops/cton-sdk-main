// Subscription.java - реалізація контракту підписки
// Author: Андрій Будильников (Sparky)
// Subscription contract implementation
// Реализация контракта подписки

package com.cton.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;

import com.cton.api.TonApiClient;
import com.cton.sdk.Address;
import com.cton.sdk.Cell;
import com.cton.sdk.CellBuilder;

/**
 * Реалізація контракту підписки TON
 * 
 * Subscription contract implementation
 * Реализация контракта подписки TON
 */
public class Subscription extends Contract {
    
    /**
     * Конструктор
     * @param address адреса контракту
     * @param apiClient API клієнт для взаємодії з мережею
     */
    public Subscription(Address address, TonApiClient apiClient) {
        super(address, apiClient);
    }
    
    /**
     * Створити повідомлення для підписки
     * @param beneficiary адреса бенефіціара
     * @param amount сума в нанотоні
     * @param period період у секундах
     * @param timeout таймаут у секундах
     * @param comment коментар
     * @return комірка з повідомленням підписки
     * @throws IOException якщо сталася помилка
     */
    public Cell createSubscriptionMessage(Address beneficiary, BigInteger amount, 
                                        long period, long timeout, String comment) throws IOException {
        CellBuilder builder = new CellBuilder();
        
        // Додаємо адресу бенефіціара
        builder.storeAddress(beneficiary);
        
        // Додаємо суму
        builder.storeCoins(amount);
        
        // Додаємо період
        builder.storeUInt(32, period);
        
        // Додаємо таймаут
        builder.storeUInt(32, timeout);
        
        // Додаємо час початку (поточний час)
        long startTime = Instant.now().getEpochSecond();
        builder.storeUInt(32, startTime);
        
        // Додаємо коментар якщо він є
        if (comment != null && !comment.isEmpty()) {
            CellBuilder commentBuilder = new CellBuilder();
            commentBuilder.storeBytes(comment.getBytes());
            builder.storeRef(commentBuilder.build());
        }
        
        return builder.build();
    }
    
    /**
     * Створити повідомлення для скасування підписки
     * @param subscriptionId ідентифікатор підписки
     * @return комірка з повідомленням скасування
     * @throws IOException якщо сталася помилка
     */
    public Cell createCancelSubscriptionMessage(long subscriptionId) throws IOException {
        CellBuilder builder = new CellBuilder();
        
        // Додаємо код операції для скасування (1)
        builder.storeUInt(32, 1);
        
        // Додаємо ідентифікатор підписки
        builder.storeUInt(64, subscriptionId);
        
        return builder.build();
    }
    
    /**
     * Отримати інформацію про підписку
     * @param subscriptionId ідентифікатор підписки
     * @return інформація про підписку
     * @throws IOException якщо сталася помилка мережі
     */
    public SubscriptionInfo getSubscriptionInfo(long subscriptionId) throws IOException {
        // Виконуємо get-метод get_subscription
        com.google.gson.JsonObject stack = new com.google.gson.JsonObject();
        stack.addProperty("subscription_id", String.valueOf(subscriptionId));
        
        com.google.gson.JsonObject result = runGetMethod("get_subscription", stack);
        
        if (!result.has("result") || result.get("result").isJsonNull()) {
            throw new IOException("Failed to get subscription info");
        }
        
        // Обробляємо результат (спрощена реалізація)
        // Parse the actual stack data
        com.google.gson.JsonArray stackArray = result.getAsJsonArray("result");
        if (stackArray.size() < 5) {
            throw new IOException("Invalid subscription info format");
        }
        
        // Parse amount (element 0)
        com.google.gson.JsonElement amountElement = stackArray.get(0);
        if (!amountElement.isJsonArray()) {
            throw new IOException("Invalid amount data format");
        }
        com.google.gson.JsonArray amountArray = amountElement.getAsJsonArray();
        if (amountArray.size() < 2) {
            throw new IOException("Invalid amount array format");
        }
        BigInteger amount = new BigInteger(amountArray.get(1).getAsString());
        
        // Parse period (element 1)
        com.google.gson.JsonElement periodElement = stackArray.get(1);
        if (!periodElement.isJsonArray()) {
            throw new IOException("Invalid period data format");
        }
        com.google.gson.JsonArray periodArray = periodElement.getAsJsonArray();
        if (periodArray.size() < 2) {
            throw new IOException("Invalid period array format");
        }
        long period = Long.parseLong(periodArray.get(1).getAsString());
        
        // Parse timeout (element 2)
        com.google.gson.JsonElement timeoutElement = stackArray.get(2);
        if (!timeoutElement.isJsonArray()) {
            throw new IOException("Invalid timeout data format");
        }
        com.google.gson.JsonArray timeoutArray = timeoutElement.getAsJsonArray();
        if (timeoutArray.size() < 2) {
            throw new IOException("Invalid timeout array format");
        }
        long timeout = Long.parseLong(timeoutArray.get(1).getAsString());
        
        // Parse last payment (element 3)
        com.google.gson.JsonElement lastPaymentElement = stackArray.get(3);
        if (!lastPaymentElement.isJsonArray()) {
            throw new IOException("Invalid last payment data format");
        }
        com.google.gson.JsonArray lastPaymentArray = lastPaymentElement.getAsJsonArray();
        if (lastPaymentArray.size() < 2) {
            throw new IOException("Invalid last payment array format");
        }
        long lastPayment = Long.parseLong(lastPaymentArray.get(1).getAsString());
        
        // Parse active status (element 4)
        com.google.gson.JsonElement activeElement = stackArray.get(4);
        if (!activeElement.isJsonArray()) {
            throw new IOException("Invalid active status data format");
        }
        com.google.gson.JsonArray activeArray = activeElement.getAsJsonArray();
        if (activeArray.size() < 2) {
            throw new IOException("Invalid active status array format");
        }
        boolean active = !"0".equals(activeArray.get(1).getAsString());
        
        return new SubscriptionInfo(subscriptionId, amount, period, timeout, lastPayment, active);
    }
    
    /**
     * Отримати загальну кількість підписок
     * @return кількість підписок
     * @throws IOException якщо сталася помилка мережі
     */
    public long getTotalSubscriptions() throws IOException {
        // Виконуємо get-метод get_total_subscriptions
        com.google.gson.JsonObject stack = new com.google.gson.JsonObject();
        com.google.gson.JsonObject result = runGetMethod("get_total_subscriptions", stack);
        
        if (!result.has("result") || result.get("result").isJsonNull()) {
            throw new IOException("Failed to get total subscriptions");
        }
        
        // Обробляємо результат (спрощена реалізація)
        // Parse the actual stack data
        com.google.gson.JsonArray stackArray = result.getAsJsonArray("result");
        if (stackArray.size() < 1) {
            throw new IOException("Invalid total subscriptions format");
        }
        
        // Total subscriptions - перший елемент стеку
        com.google.gson.JsonElement totalElement = stackArray.get(0);
        if (!totalElement.isJsonArray()) {
            throw new IOException("Invalid total subscriptions data format");
        }
        
        com.google.gson.JsonArray totalArray = totalElement.getAsJsonArray();
        if (totalArray.size() < 2) {
            throw new IOException("Invalid total subscriptions array format");
        }
        
        // Другий елемент містить значення
        String totalStr = totalArray.get(1).getAsString();
        return Long.parseLong(totalStr);
    }
    
    /**
     * Клас для інформації про підписку
     */
    public static class SubscriptionInfo {
        private final long id;
        private final BigInteger amount;
        private final long period;
        private final long timeout;
        private final long lastPayment;
        private final boolean active;
        
        public SubscriptionInfo(long id, BigInteger amount, long period, 
                              long timeout, long lastPayment, boolean active) {
            this.id = id;
            this.amount = amount;
            this.period = period;
            this.timeout = timeout;
            this.lastPayment = lastPayment;
            this.active = active;
        }
        
        // Геттери
        public long getId() { return id; }
        public BigInteger getAmount() { return amount; }
        public long getPeriod() { return period; }
        public long getTimeout() { return timeout; }
        public long getLastPayment() { return lastPayment; }
        public boolean isActive() { return active; }
    }
}