# Package Structure Recommendations

## Current Issues

1. **Misleading package name**: The `supplier` package contains more than just suppliers - it includes stores, memory implementations, and suppliers
2. **Mixed concerns**: The root `memory` package mixes JPA entities/repositories with other concerns
3. **Unclear separation**: No clear distinction between persistence layer, memory implementations, providers, and services

## Recommended Structure

```
com.example.memory
├── entity/                          # JPA persistence layer
│   ├── ChatMessageEntity
│   └── ChatMemoryRepository
│
├── store/                           # ChatMemoryStore implementations
│   ├── jpa/
│   │   └── JPAChatMemoryStore
│   └── compressing/
│       └── CompressingChatMemoryStore
│
├── memory/                          # ChatMemory implementations
│   └── jpa/
│       └── JPAChatMemory
│
├── provider/                        # ChatMemoryProvider suppliers
│   ├── jpa/
│   │   └── JPAChatMemoryProviderSupplier
│   └── compressing/
│       └── CompressingChatMemoryProviderSupplier
│
├── service/                         # AI service interfaces (bots)
│   ├── CompressedMemoryBot
│   └── JPAMemoryBot
│
└── api/                             # REST API endpoints
    └── BotPlaygroundResource
```

## Alternative Structure (More Domain-Driven)

If you prefer a more domain-driven approach that groups by feature:

```
com.example.memory
├── persistence/                     # Database layer
│   ├── entity/
│   │   └── ChatMessageEntity
│   └── repository/
│       └── ChatMemoryRepository
│
├── jpa/                             # JPA-based memory implementation
│   ├── store/
│   │   └── JPAChatMemoryStore
│   ├── memory/
│   │   └── JPAChatMemory
│   ├── provider/
│   │   └── JPAChatMemoryProviderSupplier
│   └── service/
│       └── JPAMemoryBot
│
├── compressing/                     # Compressing memory implementation
│   ├── store/
│   │   └── CompressingChatMemoryStore
│   ├── provider/
│   │   └── CompressingChatMemoryProviderSupplier
│   └── service/
│       └── CompressedMemoryBot
│
└── api/                             # REST API endpoints
    └── BotPlaygroundResource
```

## Recommended Approach: Option 1 (Layer-Based)

**Benefits:**
- Clear separation of concerns (entity, store, memory, provider, service, api)
- Easy to understand the architecture layers
- Follows common Java package organization patterns
- Makes it easy to swap implementations (e.g., different stores)

**Package Mapping:**
- `entity/` - JPA entities and repositories
- `store/` - ChatMemoryStore implementations (grouped by type: jpa, compressing)
- `memory/` - ChatMemory implementations (grouped by type: jpa)
- `provider/` - Supplier implementations (grouped by type: jpa, compressing)
- `service/` - AI service interfaces
- `api/` - REST endpoints

## Migration Steps

1. Create new package structure
2. Move files to appropriate packages
3. Update all import statements
4. Update test package structure to match
5. Verify compilation and tests pass

## Notes

- The `store/`, `memory/`, and `provider/` packages use sub-packages (jpa/, compressing/) to group related implementations
- This makes it easy to add new implementations (e.g., `store/redis/`, `memory/inmemory/`)
- The structure clearly shows the dependency flow: entity → store → memory → provider → service → api

