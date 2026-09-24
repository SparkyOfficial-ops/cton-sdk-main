// NativeLibraryTest.java - Simple test to check if native library is loaded correctly
// Author: Андрій Будильников (Sparky)

package com.cton.sdk;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class NativeLibraryTest {
    public interface TestLibrary extends Library {
        TestLibrary INSTANCE = (TestLibrary) Native.load("cton-sdk-core", TestLibrary.class);
    }
    
    public static void main(String[] args) {
        try {
            // Try to load the library
            TestLibrary library = TestLibrary.INSTANCE;
            System.out.println("Native library loaded successfully!");
        } catch (Exception e) {
            System.err.println("Failed to load native library: " + e.getMessage());
            e.printStackTrace();
        }
    }
}