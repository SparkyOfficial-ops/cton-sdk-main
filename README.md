# CTON-SDK - Advanced TON Blockchain SDK

[![License](https://img.shields.io/badge/license-custom-blue.svg)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/SparkyOfficial/cton-sdk?style=social)](https://github.com/SparkyOfficial/cton-sdk/stargazers)
[![GitHub Issues](https://img.shields.io/github/issues/SparkyOfficial/cton-sdk)](https://github.com/SparkyOfficial/cton-sdk/issues)

Advanced TON Blockchain SDK with performance, type safety, and extensibility. Built with C++ core and Java bindings for maximum efficiency.

## 🌐 Links

- [Website](https://cton-sdk.sparky.org)
- [Telegram](https://t.me/sparkyofc)
- [Discord](https://discord.gg/gz8KUkWWMj)

## 📖 Table of Contents

- [Features](#features)
- [Modules](#modules)
- [Installation](#installation)
- [Examples](#examples)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

## 🚀 Features

- **High Performance**: C++ core implementation with minimal memory allocations
- **Type Safety**: Modern Java API with Builder patterns and Fluent Interface
- **TON Compatible**: Full support for Cells, BOC, Addresses, and all TON blockchain features
- **Multi-language**: C++ core with Java JNA bindings
- **Cryptography**: Ed25519, secp256k1, BIP-39 mnemonics, ChaCha20 encryption
- **Extensible**: Modular architecture for easy addition of new token standards

## 📦 Modules

The SDK is organized into several modules:

### Core Module (`java/`)
The main SDK with core functionality:
- Cryptographic operations (Ed25519, secp256k1)
- Cell and BOC manipulation
- Address handling
- Mnemonic generation and key derivation

### API Client (`api-client/`)
Client for interacting with TON blockchain APIs:
- HTTP client for TON Center API
- Asynchronous operations support
- Wallet operations
- Smart contract interactions

### Contract Module (`contract/`)
High-level abstractions for common contracts:
- Wallet implementations
- Jetton token support
- NFT support
- Extensible contract framework

## 📥 Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.cton</groupId>
    <artifactId>cton-sdk</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.cton:cton-sdk:0.1.0-SNAPSHOT'
```

### Manual Installation

Download the latest release from [GitHub Releases](https://github.com/SparkyOfficial/cton-sdk/releases).

## 💡 Examples

### Basic Crypto Operations

```java
// Generate a new private key
PrivateKey privateKey = PrivateKey.generate();

// Get the corresponding public key
PublicKey publicKey = privateKey.getPublicKey();

// Create a message to sign
String messageStr = "Hello, TON!";
byte[] message = messageStr.getBytes();

// Sign the message
byte[] signature = Crypto.sign(privateKey, message);

// Verify the signature
boolean isValid = Crypto.verify(publicKey, message, signature);
System.out.println("Signature valid: " + isValid);
```

### Wallet Operations

```java
// Create a wallet
Wallet wallet = new WalletV3(walletAddress, apiClient);

// Get wallet balance
BigInteger balance = wallet.getBalance();

// Create a transfer
Address recipient = new Address("EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V");
BigInteger amount = BigInteger.valueOf(1_000_000_000L); // 1 TON
Cell transfer = wallet.createTransfer(recipient, amount, "Test transfer");
```

### Jetton Token Interaction

```java
// Create a Jetton instance
Jetton jetton = new Jetton(jettonAddress, apiClient);

// Get token information
BigInteger totalSupply = jetton.getTotalSupply();
Address minter = jetton.getJettonMinter();

// Get user balance
Address owner = new Address("EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V");
BigInteger balance = jetton.getBalance(owner);
```

More examples can be found in the [examples](examples/) directory.

## 🔧 Building from Source

### Prerequisites

- CMake 3.10 or higher
- Visual Studio or compatible C++ compiler (Windows)
- GCC or Clang (Linux/macOS)
- JDK 11 or higher
- Maven 3.6 or higher
- OpenSSL 3.6.0 (included in the project)

### Build Process

1. Clone the repository:
   ```bash
   git clone https://github.com/SparkyOfficial/cton-sdk.git
   cd cton-sdk
   ```

2. Build the complete SDK:
   ```bash
   # Windows
   build_all.bat

   # Or build components separately
   build_cpp.bat    # Build C++ core
   build_java.bat   # Build Java components
   ```

3. Run examples:
   ```bash
   cd examples
   mvn compile exec:java -Dexec.mainClass="CryptoExample"
   ```

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## ⭐ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=SparkyOfficial/cton-sdk&type=Date)](https://star-history.com/#SparkyOfficial/cton-sdk&Date)

## 📄 License

This project is licensed under a MIT license - see the [LICENSE](LICENSE) file for details.

---

**Created with ❤️ by [Sparky](https://github.com/SparkyOfficial)**