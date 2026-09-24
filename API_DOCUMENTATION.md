# CTON-SDK API Documentation

## Overview

The CTON-SDK provides a comprehensive set of tools for working with the TON (The Open Network) blockchain. It combines high-performance C++ core components with convenient Java APIs through JNA bindings.

## Core Components

### Cell and CellBuilder

The `Cell` class represents TON's fundamental data structure, which is a tree-like structure that can contain up to 1023 bits of data and 4 references to other cells.

```java
// Create a new cell
CellBuilder builder = new CellBuilder();
builder.storeUInt(32, 42); // Store a 32-bit unsigned integer
Cell cell = builder.build();

// Access cell data
byte[] data = cell.getData();
int bitLength = cell.getBitLength();
```

### Address

The `Address` class handles TON addresses, including parsing, validation, and conversion between different formats.

```java
// Create an address from string
Address address = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");

// Get different representations
String raw = address.toRaw();
String userFriendly = address.toUserFriendly(true, false);
```

### Crypto

The `Crypto` package provides cryptographic operations, including key generation, signing, and verification.

```java
// Generate a private key
PrivateKey privateKey = Crypto.generatePrivateKey();

// Get the corresponding public key
PublicKey publicKey = privateKey.getPublicKey();

// Sign data
byte[] data = "Hello, TON!".getBytes();
byte[] signature = privateKey.sign(data);

// Verify signature
boolean isValid = publicKey.verify(data, signature);
```

### BOC (Bag of Cells)

The `Boc` class handles serialization and deserialization of cell trees.

```java
// Serialize a cell to BOC
Cell cell = // ... create a cell
Boc boc = new Boc(cell);
byte[] serialized = boc.serialize(true, true);

// Deserialize BOC back to cell
Boc deserializedBoc = new Boc(serialized);
Cell rootCell = deserializedBoc.getRoot();
```

## API Client

### TonApiClient

The `TonApiClient` provides HTTP access to TON Center API.

```java
// Create API client
TonApiClient client = new TonApiClient("https://toncenter.com/api/v2/");

// Get address information
JsonObject addressInfo = client.getAddressInformation("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");

// Run get-method on smart contract
JsonObject stack = new JsonObject();
JsonObject result = client.runGetMethod("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N", "seqno", stack);

// Send BOC
client.sendBoc(bocBytes);
```

### Asynchronous Operations

All API client methods have asynchronous counterparts using `CompletableFuture`:

```java
// Asynchronous address information
CompletableFuture<JsonObject> future = client.getAddressInformationAsync("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
JsonObject result = future.join();

// Asynchronous BOC sending
CompletableFuture<JsonObject> sendFuture = client.sendBocAsync(bocBytes);
```

## Contract Module

### Wallet

The `Wallet` interface provides high-level wallet functionality:

```java
// Create a wallet
Address walletAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
Wallet wallet = new WalletV3(walletAddress, apiClient);

// Get wallet balance
BigInteger balance = wallet.getBalance();

// Create a transfer
Address recipient = new Address("EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V");
BigInteger amount = BigInteger.valueOf(1_000_000_000L); // 1 TON
Cell transfer = wallet.createTransfer(recipient, amount, "Test transfer");
```

### MessageFactory

The `MessageFactory` creates different types of TON messages:

```java
// Create a text message
Cell textMessage = MessageFactory.createTextMessage("Hello from CTON-SDK!");

// Create an internal message
Cell internalMessage = MessageFactory.createInternalMessage(recipient, amount, textMessage, 0);

// Create an external message
Cell externalMessage = MessageFactory.createExternalMessage(recipient, amount, textMessage);
```

### Jetton

The `Jetton` class implements the TEP-74 Jetton token standard:

```java
// Create a Jetton instance
Address jettonAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
Jetton jetton = new Jetton(jettonAddress, apiClient);

// Get token information
BigInteger totalSupply = jetton.getTotalSupply();
Address minter = jetton.getJettonMinter();

// Get user balance
Address owner = new Address("EQA8cLh74oFKcL523Jz9Hw5ReXY6Yglz8g422w7NwzvzL03V");
BigInteger balance = jetton.getBalance(owner);
```

### NFT

The `Nft` class implements the TEP-62 NFT standard:

```java
// Create an NFT instance
Address nftAddress = new Address("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N");
Nft nft = new Nft(nftAddress, apiClient);

// Get NFT information
JsonObject nftData = nft.getNftData();
long index = nft.getIndex();
Address collection = nft.getCollection();
Address owner = nft.getOwner();
```

## Error Handling

The SDK uses standard Java exceptions:

```java
try {
    JsonObject result = client.getAddressInformation("invalid_address");
} catch (IOException e) {
    // Handle network or API errors
    System.err.println("API error: " + e.getMessage());
}
```

## Building and Running

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- C++17 compatible compiler (for native library)

### Building
```bash
# Build Java components
mvn clean install

# Build native library (requires C++ tools)
cd cpp/build
cmake ..
cmake --build . --config Release
```

## Examples

See the `examples/` directory for complete working examples:

- `SimpleTransfer.java` - Basic transfer example
- `ComprehensiveExample.java` - Shows all SDK features

## Author

Andriy Budilnikov (Sparky)

This documentation provides an overview of the CTON-SDK API. For detailed information about specific methods and classes, please refer to the source code and Javadoc comments.