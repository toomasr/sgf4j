# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SGF4J is a Java library for parsing, manipulating, and serializing Smart Game Format (SGF) files used in Go (the ancient board game). It parses SGF files, allows navigation of the game tree, simulates games on a virtual board with capture detection, and serializes games back to SGF format.

## Build Commands

```bash
./mvnw clean install          # Build and install locally
./mvnw test                   # Run all tests
./mvnw test -Dtest=TestParser # Run a single test class
./mvnw clean package          # Package the project
```

## Architecture

### Core Classes (com.toomasr.sgf4j.parser)

- **Sgf** - Entry point/facade with static factory methods: `createFromPath()`, `createFromString()`, `createFromInputStream()`, `writeToFile()`
- **Game** - Represents a complete Go game with properties map, root node, and timing info
- **GameNode** - Tree node with parent/children/next/prev relationships and properties map
- **Parser** - Converts SGF strings to Game objects using regex pattern matching

### Board Simulation (com.toomasr.sgf4j.parser.board)

- **VirtualBoard** - 19x19 board that tracks stones and handles captures via `makeMove()`/`undoMove()`
- **Square** - Single board intersection with StoneState (BLACK/WHITE/EMPTY)
- **Group** - Connected stones of same color for capture detection

### Key Relationships

```
Sgf.createFromPath() → Parser.parse() → Game
                                          ├─ rootNode: GameNode (tree structure)
                                          ├─ properties: Map<String, String>
                                          └─ postProcess() (numbering, visual depth)

VirtualBoard.makeMove(node) ← GameNode (separate concern for replay)
```

### Game Tree Navigation

GameNode forms a tree structure:
- `nextNode`/`prevNode` - main line traversal
- `parentNode`/`children` - variation branching
- `isMove()`, `isBlack()`, `isWhite()`, `isPass()` - node type detection

## Testing

Tests use JUnit 4. Key test classes:
- `TestParser` - Core parsing functionality
- `TestBoard` - Board mechanics and capture logic
- `TestSaveSgf` - Serialization back to SGF format
- `TestProblematic` - Edge cases from real SGF files

Test resources in `src/test/resources/` include sample SGF files. The project has been validated against 90,000+ real SGF files.

## SGF Format Notes

- Properties are defined in `Parser.java` static sets (65+ property types)
- Common property constants in `SgfProperties.java`
- Coordinates use alphabetic notation (skipping 'I' per Go convention) - see `Util.java`
