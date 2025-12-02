package com.ClexaGod.simplehome.manager

import com.ClexaGod.simplehome.SimpleHome

class ConfigManager(private val plugin: SimpleHome) {

    val defaultLimit: Int
        get() = plugin.config.getInt("home-limits.default", 3)

    val homeLimits: Map<String, Int>
        get() {
            val section = plugin.config.getSection("home-limits")
            val limits = mutableMapOf<String, Int>()
            
            if (section != null) {
                for (key in section.keys) {
                    if (key != "default") {
                        limits[key] = plugin.config.getInt("home-limits.$key")
                    }
                }
            }
            return limits
        }

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
