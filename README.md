# SimpleHome - Advanced Home UI for PowerNukkitX

<div align="center">

![License](https://img.shields.io/badge/License-Apache_2.0-green.svg?style=flat-square)
![Platform](https://img.shields.io/badge/Platform-PowerNukkitX-blue.svg?style=flat-square)
![Language](https://img.shields.io/badge/Language-Kotlin_1.9-purple.svg?style=flat-square)
![Version](https://img.shields.io/badge/Version-1.0.1-orange.svg?style=flat-square)

**The Ultimate Home Management Plugin for PowerNukkitX**
*User-Friendly UI • Immersive Effects • Secure Data • High Performance*

[Download](#installation) • [Features](#features) • [Configuration](#configuration)

</div>

---

## Overview

**SimpleHome** is a production-ready, fully graphical home management plugin written in **Kotlin**. It leverages the **PowerNukkitX Form API** to replace complex commands with an intuitive UI. Designed for modern servers, it offers a seamless experience with cinematic teleportation effects, strict input validation, and secure UUID-based data storage.

## Features

### 🖥️ Professional User Interface (Form API)
*   **Interactive Forms:** Manage homes effortlessly via `SimpleForm` and `CustomForm` interfaces.
*   **CRUD Operations:** Set, List, Edit, and Delete homes without typing commands.
*   **Smart Navigation:** Integrated "Back" buttons and intuitive flow for better User Experience (UX).
*   **Visual Customization:** Assign distinct icons to homes for quick recognition in the list.

### ✨ Immersive Teleportation Experience
*   **Pre-Teleport Effects:** A dynamic **DNA-Helix particle animation** (`EnchantmentTable` & `Portal` particles) circles the player during warmup.
*   **Audio Feedback:** A "Minigame-style" countdown with rising pitch sound effects (`Note Pling`) builds anticipation.
*   **Arrival Impact:** Successful teleportation is marked by a burst of `ElectricSpark` particles and a resonant sound effect, providing immediate feedback.

### 🛡️ Security & Reliability
*   **Input Sanitization:** Forces strict Regex validation (`[a-zA-Z0-9_-]`) on home names to prevent invisible characters or exploits.
*   **UUID-Based Storage:** Player data is stored in `players/PlayerName.yml` but indexed internally by **UUID**, ensuring data persists even if a player changes their Gamertag.
*   **World Whitelist:** Strictly enforces an "Allowed Worlds" policy, preventing players from setting homes in restricted areas (e.g., PvP arenas, mini-games).

---

<div align="center">

<img src="assets/home_menu.jpg" width="700" alt="SimpleHome UI">
<br><br>
<img src="assets/home_list.jpg" width="700" alt="SimpleHome List">
<br><br>
<img src="assets/edit_home.jpg" width="700" alt="SimpleHome Edit">

</div>

---

## Installation

1.  Download the latest **`simplehome-1.0.1.jar`** from the [Releases](https://github.com/ClexaGod/SimpleHome/releases) page.
2.  Upload the file to your server's `plugins` folder.
3.  Restart the server.
4.  (Optional) Configure `config.yml` to match your server's needs.

---

## Commands & Permissions

| Command | Description | Permission | Default |
| :--- | :--- | :--- | :--- |
| **`/home`** | Opens the main Home UI. | `simplehome.command.home` | `true` (Everyone) |

---

## Configuration

The `config.yml` allows full control over the plugin's behavior.

```yaml
# SimpleHome Configuration File

settings:
  max-homes: 5          # Maximum number of homes a player can set
  teleport-delay: 3     # Warmup time in seconds before teleporting
  log-teleports: true   # Enable/Disable logging of teleport actions to 'teleport_logs.yml'

# World Whitelist: Players can ONLY set homes in these worlds.
allowed-worlds:
  - "world"
  - "plots"
  - "nether"

effects:
  particles: true       # Enable visual particle effects (Helix & Burst)
  sound: true           # Enable sound effects (Countdown & Arrival)
```

---

## Build from Source

Required: **JDK 21** and **Maven**.

```bash
git clone https://github.com/ClexaGod/SimpleHome.git
cd SimpleHome
mvn clean package
```

The compiled plugin will be generated in `target/simplehome-1.0.1.jar`.

---

<div align="center">

**Keywords:** *PowerNukkitX, PNX Plugin, Home System, Teleport UI, Kotlin, Form API, GUI, SimpleHome, Bedrock Edition*

Made with ❤️ by **ClexaGod** for the PowerNukkitX Community.

</div>
