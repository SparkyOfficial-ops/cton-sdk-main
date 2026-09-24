package com.cton.sdk;

public class LibraryLoadTest {
    static {
        try {
            // Try to load the native library
            System.loadLibrary("cton-sdk-core");
            System.out.println("Successfully loaded cton-sdk-core library");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load cton-sdk-core library: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Library load test completed");
    }
}