// BlockHeader.java - DTO для заголовка блоку
// Author: Андрій Будильников (Sparky)
// DTO for block header
// DTO для заголовка блока

package com.cton.api.dto;

/**
 * DTO для заголовка блоку TON
 * 
 * DTO for TON block header
 * DTO для заголовка блока TON
 */
public class BlockHeader {
    private int workchain;
    private long shard;
    private long seqno;
    private String rootHash;
    private String fileHash;
    private long time;
    
    // Конструктор за замовчуванням
    // Default constructor
    // Конструктор по умолчанию
    public BlockHeader() {}
    
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
    
    public long getTime() {
        return time;
    }
    
    public void setTime(long time) {
        this.time = time;
    }
    
    @Override
    public String toString() {
        return "BlockHeader{" +
                "workchain=" + workchain +
                ", shard=" + shard +
                ", seqno=" + seqno +
                ", rootHash='" + rootHash + '\'' +
                ", fileHash='" + fileHash + '\'' +
                ", time=" + time +
                '}';
    }
}