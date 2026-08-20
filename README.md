# HighCraftPlugin

Paper plugin with custom substances, player consequence tracking, a cauldron branch, special water and ordinary ICE crystal drops.

## Build

Requirements:
- Java 21
- Maven 3.9+
- Paper 1.20.6+

Run:

```bash
mvn clean package
```

The plugin JAR will be in `target/HighCraftPlugin-1.0.0.jar`.

## Install

Copy the JAR into the server's `plugins/` directory and restart the Paper server.

## Commands

- `/drug stats`
- `/drug reload` (admin)
- `/drug give <player> <id> [amount]` (admin)

The persistent data is stored in `plugins/HighCraftPlugin/data.yml`.

## Important

This is a complete starter implementation of the requested architecture, but the recipe/effect tables in the original specification contain custom items and brewing-stage recipes that need to be expanded if every one of the 25 recipes must be represented as a strict vanilla-style recipe rather than as custom-item logic.
