# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**wthaigd** ("What the hell am I gonna do") is a Minecraft 1.7.10 mod targeting the GTNH (GregTech New Horizons) 2.7.x modpack. It provides a collaborative in-game task management system with features like task creation/assignment, team management, SQLite-backed persistence, and a custom GUI built with ModularUI2.

## Build System

This project uses the **GTNH Gradle Convention Plugin** (`com.gtnewhorizons.gtnhconvention`) with RetroFuturaGradle. Java 17 syntax is enabled via Jabel while targeting JVM 8.

```bash
# Build the mod jar
./gradlew build

# Run Minecraft client (working dir: run/client)
./gradlew runClient

# Run dedicated server (working dir: run/server)
./gradlew runServer

# Run with specific Java version variants
./gradlew runClient17    # Java 17
./gradlew runClient21    # Java 21

# Code formatting (Spotless)
./gradlew spotlessApply
./gradlew spotlessCheck
```

## Key Dependencies

- **Lombok** — used extensively for `@Data`, `@Getter`, etc. on entity classes
- **SQLite JDBC** (`org.xerial:sqlite-jdbc`) — persistence layer
- **ModularUI2** — GUI framework (from GTNH)
- **Kryo** — serialization
- **Reflections** — runtime class scanning

## Architecture

### Mod Entry & Lifecycle

- `Wthaigd.java` — `@Mod` entry point, delegates all lifecycle events to proxy
- `core/CommonProxy` — server-side lifecycle: config, loaders (blocks, items, recipes, commands), event handlers, network registration
- `core/ClientProxy` — extends CommonProxy with client-only: keybinds, ModularUI theme, blur handler
- `core/EventHandler` — Forge event bus handlers
- `core/PacketHandler` — FML SimpleNetworkWrapper channel registration and message handlers
- `core/ConfigSetting` — mod configuration

### Loader Pattern

Registration of game objects follows a loader pattern in `loader/`:
- `BlockLoader`, `ItemLoader`, `CreativeTabsLoader`, `RecipeLoader` — called during `preInit`
- `CommandLoader` — called during `serverStarting`
- `GUILoader` — GUI registration

### Task System (`task/`)

The core domain model for task management:

- **Entities** (`task/entity/`): `Task`, `Player`, `Team`, `Tag` — annotated with custom `@Table`, `@Column`, `@Reference`, `@FieldCheck` annotations for ORM-like DB mapping
- **Records** (`task/entity/record/`): Junction/audit tables — `TagLink`, `TeamMember`, `TaskInteraction`, `StatusChangeRecord`, `Notification`, etc.
- **DAOs** (`task/dao/`): Data access objects per entity — `TaskDao`, `PlayerDao`, `TeamDao`, `TagDao`, plus record DAOs
- `TaskCommand` — in-game `/task` command implementation
- `TaskSqlHelper` — task-specific database initialization

### Database Layer (`helper/dataBase/`)

Custom annotation-driven SQLite ORM:

- `SQLiteManager` — manages an **in-memory SQLite database** that loads from/saves to a file (`main.db` in world directory). Initialization happens at world load, persistence on save/shutdown.
- `SQLHelper` — fluent SQL builder entry point (create/insert/select/update/delete)
- `EntityHandler` — reflection-based entity-to-SQL mapping using custom annotations
- `builder/` — SQL builder classes: `CreateTableBuilder`, `SelectBuilder`, `InsertBuilder`, `UpdateBuilder`, `DeleteBuilder`, `AlterTableBuilder`, `DropTableBuilder`
- Annotations: `@Table`, `@Column`, `@Reference` (foreign keys), `@FieldCheck` (validation)

### GUI System (`gui/`)

- `MainModularScreen` / `screen/panel/MainPanel` — main mod GUI using ModularUI2
- `widget/` — custom widgets: `CustomButton`, `CustomColumn`, `CustomRow`, `RectConfigBuilder`, `ICustomAble`
- `KeyBindGuiHandler` — keybind to open mod GUI
- `ModularTheme` — custom UI theme

### Rendering (`helper/render/`)

- `ShaderHelper` / `GLShaderDrawHelper` — OpenGL shader management (GLSL shaders in `resources/assets/wthaigd/shaders/`)
- `GLDrawHelper` / `RenderHelper` — drawing utilities for rounded rectangles with shadows/borders
- `BlurHandler` — background blur effect

### Networking (`helper/network/`)

- `NetWorkData` — FML `IMessage` implementation for client-server communication
- `NetWorkHelper` — network utility functions

## Code Conventions

- Root package: `com.pinkyudeer.wthaigd`
- Mod ID: `wthaigd`
- Comments and variable descriptions are frequently in Chinese
- Entity fields use custom annotations (`@Table`, `@Column`, `@Reference`) — not standard JPA
- The project uses Spotless for formatting (configured via GTNH blowdryer tag `0.2.2`)
