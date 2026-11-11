# HighCraftPlugin

Paper plugin with custom substances, player consequence tracking, a cauldron branch, special water and ordinary ICE crystal drops.

## Build

Requirements:
- Java 21
- Maven 3.9+
- Paper 1.21.4

Run:

```bash
mvn clean package
```

The plugin JAR will be in `target/HighCraftPlugin-1.0.1.jar`.

## Install

Copy the JAR into the server's `plugins/` directory and restart the Paper server.

## Commands

- `/drug stats`
- `/drug reload` (admin)
- `/drug give <player> <id> [amount]` (admin)

The persistent data is stored in `plugins/HighCraftPlugin/data.yml`.

## Important

This is a complete starter implementation of the requested architecture, but the recipe/effect tables in the original specification contain custom items and brewing-stage recipes that need to be expanded if every one of the 25 recipes must be represented as a strict vanilla-style recipe rather than as custom-item logic.


## 1.0.1 hotfix

- Recipe `NamespacedKey` values are normalized to lowercase ASCII before registration. Bukkit/Paper keys cannot contain Cyrillic characters; Russian item display names remain unchanged.
- Compiled against Paper API 1.21.4 and declares API version 1.21.
- If an older `HighCraftPlugin-1.0.0.jar` is installed, stop the server, replace it with `HighCraftPlugin-1.0.1.jar`, and start the server again. Do not keep both JARs in `plugins/`.
