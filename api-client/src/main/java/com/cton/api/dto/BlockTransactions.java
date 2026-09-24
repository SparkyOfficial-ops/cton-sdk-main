// BlockTransactions.java - DTO для транзакцій блоку
// Author: Андрій Будильников (Sparky)
// DTO for block transactions
// DTO для транзакций блока

package com.cton.api.dto;

import java.util.List;

/**
 * DTO для транзакцій блоку TON
 * 
 * DTO for TON block transactions
 * DTO для транзакций блока TON
 */
public class BlockTransactions {
    private int workchain;
    private long shard;
    private long seqno;
    private String rootHash;
    private String fileHash;
    private List<Transaction> transactions;
    
    // Конструктор за замовчуванням
    // Default constructor
    // Конструктор по умолчанию
    public BlockTransactions() {}
    
    // Геттери та сеттери
    // Getters and setters
    // Геттеры и сеттеры
    
    public int getWorkchain() {
        return workchain;
    }
    
    public void setWorkchain(int workchain) {
        this.workchain = workchain;
    }
    
    public long getShard() {
        return shard;
    }
    
    public void setShard(long shard) {
        this.shard = shard;
    }
    
    public long getSeqno() {
        return seqno;
    }
    
    public void setSeqno(long seqno) {
        this.seqno = seqno;
    }
    
    public String getRootHash() {
        return rootHash;
    }
    
    public void setRootHash(String rootHash) {
        this.rootHash = rootHash;
    }
    
    public String getFileHash() {
        return fileHash;
    }
    
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    @Override
    public String toString() {
        return "BlockTransactions{" +
                "workchain=" + workchain +
                ", shard=" + shard +
                ", seqno=" + seqno +
                ", rootHash='" + rootHash + '\'' +
                ", fileHash='" + fileHash + '\'' +
                ", transactions=" + transactions +
                '}';
    }
    
    /**
     * DTO для транзакції
     * 
     * DTO for transaction
     * DTO для транзакции
     */
    public static class Transaction {
        private String account;
        private String hash;
        private long lt;
        
        // Конструктор за замовчуванням
        // Default constructor
        // Конструктор по умолчанию
        public Transaction() {}
        
        // Геттери та сеттери
        // Getters and setters
        // Геттеры и сеттеры
        
        public String getAccount() {
            return account;
        }
        
        public void setAccount(String account) {
            this.account = account;
        }
        
        public String getHash() {
            return hash;
        }
        
        public void setHash(String hash) {
            this.hash = hash;
        }
        
        public long getLt() {
            return lt;
        }
        
        public void setLt(long lt) {
            this.lt = lt;
        }
        
        @Override
        public String toString() {
            return "Transaction{" +
                    "account='" + account + '\'' +
                    ", hash='" + hash + '\'' +
                    ", lt=" + lt +
                    '}';
        }
    }
}