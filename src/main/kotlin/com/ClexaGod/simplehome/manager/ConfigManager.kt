package com.ClexaGod.simplehome.manager

import com.ClexaGod.simplehome.SimpleHome

class ConfigManager(private val plugin: SimpleHome) {

    val maxHomes: Int
        get() = plugin.config.getInt("settings.max-homes", 5)

    val teleportDelay: Int
        get() = plugin.config.getInt("settings.teleport-delay", 3)

    val isLogEnabled: Boolean
        get() = plugin.config.getBoolean("settings.log-teleports", true)

    val allowedWorlds: List<String>
        get() = plugin.config.getStringList("allowed-worlds")

    val isParticlesEnabled: Boolean
        get() = plugin.config.getBoolean("effects.particles", true)

    val isSoundEnabled: Boolean
        get() = plugin.config.getBoolean("effects.sound", true)
}
