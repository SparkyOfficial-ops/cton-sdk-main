# Contributing to CTON-SDK

Thank you for your interest in contributing to the CTON-SDK! This document provides guidelines and information to help you contribute effectively.

## How to Contribute

### Reporting Issues
- Use the GitHub issue tracker to report bugs or suggest features
- Before creating a new issue, please check if it already exists
- Provide detailed information about the issue, including:
  - Steps to reproduce
  - Expected behavior
  - Actual behavior
  - Environment information (OS, Java version, etc.)

### Code Contributions
1. Fork the repository
2. Create a feature branch for your changes
3. Make your changes with clear, concise commit messages
4. Add or update tests as needed
5. Ensure all tests pass
6. Submit a pull request with a clear description of your changes

## Development Setup

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- CMake 3.10 or higher
- OpenSSL development libraries
- C++17 compatible compiler

### Building the Project
```bash
# Build everything (requires C++ build tools)
./build_all.bat

# Or build Java components only
mvn clean install
```

## Code Style

### Java Code
- Follow standard Java naming conventions
- Use meaningful variable and method names
- Write clear, concise comments in English
- Follow the existing code style in the project

### C++ Code
- Use C++17 features when appropriate
- Follow modern C++ best practices
- Write clear, concise comments in English
- Follow the existing code style in the project

## Testing

### Unit Tests
- All new functionality should include unit tests
- Tests should be placed in the appropriate test directories
- Use JUnit 5 for Java tests
- Use Google Test or Catch2 for C++ tests (when implemented)

### Integration Tests
- Integration tests should verify that components work together correctly
- Tests should be fast and reliable
- Avoid external dependencies when possible

## Documentation

### Code Comments
- Comment complex or non-obvious code
- Use clear, concise language
- Update comments when code changes

### API Documentation
- Document all public APIs
- Include examples for complex APIs
- Keep documentation up to date with code changes

## Pull Request Process

1. Ensure your code follows the project's coding standards
2. Add tests for new functionality
3. Update documentation as needed
4. Verify all tests pass
5. Submit a pull request with a clear description

## Code of Conduct

Please be respectful and professional in all interactions. We welcome contributions from everyone and strive to create a welcoming environment for all developers.

## Contact

For questions or discussions about contributing, please open an issue on GitHub.

## License

By contributing to CTON-SDK, you agree that your contributions will be licensed under the project's license.

Author: Andriy Budilnikov (Sparky)