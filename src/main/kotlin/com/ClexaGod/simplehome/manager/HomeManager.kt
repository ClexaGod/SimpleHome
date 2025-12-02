package com.ClexaGod.simplehome.manager

import cn.nukkit.Player
import cn.nukkit.utils.Config
import com.ClexaGod.simplehome.SimpleHome
import java.io.File

class HomeManager(private val plugin: SimpleHome) {

    init {
        val playersDir = File(plugin.dataFolder, "players")
        if (!playersDir.exists()) {
            playersDir.mkdirs()
        }
    }

    private fun getPlayerConfig(player: Player): Config {
        return Config(File(plugin.dataFolder, "players/${player.name}.yml"), Config.YAML)
    }

    fun getHomes(player: Player): Map<String, Any> {
        val config = getPlayerConfig(player)
        return config.getSection("homes") ?: emptyMap()
    }

    fun getHomeData(player: Player, homeName: String): Map<String, Any>? {
        val homes = getHomes(player)
        @Suppress("UNCHECKED_CAST")
        return homes[homeName] as? Map<String, Any>
    }

    fun setHome(player: Player, homeName: String, icon: String = "DEFAULT") {
        val config = getPlayerConfig(player)
        val loc = player.location
        
        val homeData = mapOf(
            "x" to loc.x,
            "y" to loc.y,
            "z" to loc.z,
            "world" to loc.level.folderName,
            "yaw" to loc.yaw,
            "pitch" to loc.pitch,
            "icon" to icon
        )

    
        config.set("uuid", player.uniqueId.toString())
        config.set("homes.$homeName", homeData)
        config.save()
    }

    fun deleteHome(player: Player, homeName: String) {
        val config = getPlayerConfig(player)
        val homes = config.getSection("homes")
        if (homes.containsKey(homeName)) {
            homes.remove(homeName)
            config.set("homes", homes)
            config.save()
        }
    }

    fun updateHome(player: Player, oldName: String, newName: String, newIcon: String) {
        val config = getPlayerConfig(player)
        val homes = config.getSection("homes")
        
        if (homes.containsKey(oldName)) {
            @Suppress("UNCHECKED_CAST")
            val data = (homes[oldName] as Map<String, Any>).toMutableMap()
            data["icon"] = newIcon
            
            
            homes.remove(oldName)
            homes[newName] = data
            
            config.set("homes", homes)
            config.save()
        }
    }

    fun getHomeCount(player: Player): Int {
        return getHomes(player).size
    }

    fun getMaxHomes(player: Player): Int {
        if (player.isOp) {
            return Int.MAX_VALUE
        }

        var max = plugin.configManager.defaultLimit
        val customLimits = plugin.configManager.homeLimits

        for ((group, limit) in customLimits) {
            if (player.hasPermission("simplehome.limit.$group")) {
                if (limit > max) {
                    max = limit
                }
            }
        }
        return max
    }

    fun homeExists(player: Player, homeName: String): Boolean {
        return getHomes(player).containsKey(homeName)
    }
    
    fun isValidName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z0-9_\\-]+$"))
    }
    
    fun sanitizeName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_\\-]"), "").trim()
    }
}
