# Struttura Progetto - Clean Architecture

## 📁 Organizzazione Package

```
net.calvuz.quickstore/
│
├── 📱 presentation/           # Layer Presentazione (UI + ViewModels)
│   ├── ui/
│   │   ├── articles/
│   │   │   ├── list/         # Lista articoli
│   │   │   ├── detail/       # Dettaglio articolo
│   │   │   └── add/          # Aggiungi/modifica articolo
│   │   ├── movements/
│   │   │   ├── list/         # Lista movimentazioni
│   │   │   └── add/          # Registra movimento
│   │   ├── camera/           # Ricerca con foto
│   │   ├── home/             # Dashboard
│   │   └── common/           # Componenti riutilizzabili
│   └── navigation/           # Setup navigazione Compose
│
├── 🎯 domain/                 # Layer Domain (Business Logic)
│   ├── model/                # Domain Models (non entity!)
│   │   ├── Article.kt
│   │   ├── Inventory.kt
│   │   ├── Movement.kt
│   │   └── ArticleImage.kt
│   ├── usecase/              # Use Cases (Business Logic pura)
│   │   ├── article/
│   │   │   ├── AddArticleUseCase.kt
│   │   │   ├── GetArticleUseCase.kt
│   │   │   ├── UpdateArticleUseCase.kt
│   │   │   └── DeleteArticleUseCase.kt
│   │   ├── movement/
│   │   │   ├── AddMovementUseCase.kt
│   │   │   └── GetMovementsUseCase.kt
│   │   └── recognition/
│   │       └── SearchArticleByImageUseCase.kt
│   └── repository/           # Repository Interfaces
│       ├── ArticleRepository.kt
│       ├── MovementRepository.kt
│       └── ImageRecognitionRepository.kt
│
├── 💾 data/                   # Layer Data (Implementazioni)
│   ├── local/
│   │   ├── database/
│   │   │   ├── QuickStoreDatabase.kt
│   │   │   ├── Converters.kt
│   │   │   ├── ArticleDao.kt
│   │   │   ├── InventoryDao.kt
│   │   │   ├── MovementDao.kt
│   │   │   └── ArticleImageDao.kt
│   │   ├── entity/           # Room Entities
│   │   │   ├── ArticleEntity.kt
│   │   │   ├── InventoryEntity.kt
│   │   │   ├── MovementEntity.kt
│   │   │   └── ArticleImageEntity.kt
│   │   └── storage/
│   │       └── ImageStorageManager.kt
│   ├── opencv/               # OpenCV Integration
│   │   ├── OpenCVManager.kt
│   │   ├── FeatureExtractor.kt
│   │   └── ImageMatcher.kt
│   ├── mapper/               # Entity ↔ Domain mappers
│   │   ├── ArticleMapper.kt
│   │   ├── InventoryMapper.kt
│   │   └── MovementMapper.kt
│   └── repository/           # Repository Implementations
│       ├── ArticleRepositoryImpl.kt
│       ├── MovementRepositoryImpl.kt
│       └── ImageRecognitionRepositoryImpl.kt
│
├── 🔧 di/                     # Dependency Injection (Hilt)
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
│
├── 🛠️ util/                  # Utilities
│   ├── DateTimeUtils.kt
│   ├── Constants.kt
│   └── Extensions.kt
│
└── QuickStoreApplication.kt   # Application Class

```

## 🏗️ Principi Clean Architecture

### Layer Presentation
- **Responsabilità**: UI e gestione stato
- **Dipendenze**: Dipende da Domain
- **Tecnologie**: Jetpack Compose, ViewModels, Navigation
- **NON può**: Accedere direttamente ai DAO o Entity

### Layer Domain
- **Responsabilità**: Business logic pura
- **Dipendenze**: NESSUNA (solo Kotlin puro)
- **Contenuto**: Models, Use Cases, Repository Interfaces
- **NON può**: Conoscere Android Framework o Room

### Layer Data
- **Responsabilità**: Implementazione accesso dati
- **Dipendenze**: Dipende da Domain
- **Tecnologie**: Room, OpenCV, File System
- **NON può**: Essere conosciuto da Domain (solo tramite interfacce)

## 📋 Checklist Fase 1 (Completata)

- [x] Setup Gradle con dipendenze
- [x] Application class con Hilt
- [x] Database Entities (ArticleEntity, InventoryEntity, MovementEntity, ArticleImageEntity)
- [x] DAO interfaces (ArticleDao, InventoryDao, MovementDao, ArticleImageDao)
- [x] WarehouseDatabase con Room
- [x] TypeConverters per enum
- [x] DatabaseModule per Hilt DI
- [x] Struttura cartelle Clean Architecture

## 🚀 Prossimi Passi (Fase 2)

1. Creare Domain Models
2. Creare Mappers (Entity ↔ Domain)
3. Implementare Repository Interfaces
4. Implementare Repository Implementations
5. Creare Use Cases base
6. Setup OpenCV
7. Prime UI screens con Compose

## 📝 Note Importanti

### Timestamp UTC
Tutti i timestamp nel database sono in UTC (Long - milliseconds since epoch).
Conversione a LocalDateTime solo nel layer Presentation.

### Quantità Decimali
Inventory e Movement usano `Double` per supportare unità di misura frazionarie (kg, litri, metri).

### Foreign Keys
Tutte le foreign keys hanno `onDelete = CASCADE` per integrità referenziale automatica.

### Transaction
Le operazioni che toccano multiple tabelle (es: add movement + update inventory) devono essere wrapped in `@Transaction`.

## 🔗 Link Utili

- [Room Documentation](https://developer.android.com/training/data-storage/room)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Clean Architecture Guide](https://developer.android.com/topic/architecture)