package com.ClexaGod.simplehome.manager

import cn.nukkit.Player
import cn.nukkit.level.Location
import cn.nukkit.level.Sound
import cn.nukkit.level.particle.ElectricSparkParticle
import cn.nukkit.level.particle.HugeExplodeParticle
import cn.nukkit.math.Vector3
import cn.nukkit.utils.Config
import cn.nukkit.utils.TextFormat
import com.ClexaGod.simplehome.SimpleHome
import com.ClexaGod.simplehome.task.TeleportTask
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class TeleportManager(private val plugin: SimpleHome) {

    private val teleportingPlayers = mutableMapOf<String, Int>() 
    private val logConfig: Config

    init {
        logConfig = Config(File(plugin.dataFolder, "teleport_logs.yml"), Config.YAML)
    }

    fun teleportToHome(player: Player, homeName: String) {
        val homeData = plugin.homeManager.getHomeData(player, homeName)
        if (homeData == null) {
            player.sendMessage("${TextFormat.RED}Home not found!")
            return
        }

        val worldName = homeData["world"] as String
        if (!plugin.server.isLevelLoaded(worldName)) {
            if (!plugin.server.loadLevel(worldName)) {
                player.sendMessage("${TextFormat.RED}World '$worldName' could not be loaded!")
                return
            }
        }

        val level = plugin.server.getLevelByName(worldName)
        val location = Location(
            homeData["x"] as Double,
            homeData["y"] as Double,
            homeData["z"] as Double,
            homeData["yaw"] as Double,
            homeData["pitch"] as Double,
            level
        )

        if (teleportingPlayers.containsKey(player.name)) {
            player.sendMessage("${TextFormat.RED}Teleportation is already in progress!")
            return
        }

        val delay = plugin.configManager.teleportDelay
        if (delay > 0) {
            
            val task = TeleportTask(this, player, location, homeName, homeData, delay)

            val taskId = plugin.server.scheduler.scheduleRepeatingTask(plugin, task, 1).taskId
            teleportingPlayers[player.name] = taskId
        } else {
            executeTeleport(player, location, homeName, homeData)
        }
    }

    fun executeTeleport(player: Player, location: Location, homeName: String, homeData: Map<String, Any>) {
        player.teleport(location)
        playArrivalEffects(player)
        logTeleport(player, homeName, homeData)
        player.sendMessage("${TextFormat.GREEN}Teleported to ${TextFormat.WHITE}$homeName${TextFormat.GREEN}.")
        teleportingPlayers.remove(player.name)
    }

    fun cancelTeleport(player: Player, reason: String) {
        if (teleportingPlayers.containsKey(player.name)) {
            val taskId = teleportingPlayers[player.name]!!
            plugin.server.scheduler.cancelTask(taskId)
            teleportingPlayers.remove(player.name)
            player.sendMessage("${TextFormat.RED}Teleportation cancelled: $reason")
            
            if (plugin.configManager.isSoundEnabled) {
                player.level.addSound(player, Sound.NOTE_BASS, 1f, 0.8f)
            }
        }
    }

    fun isCancelled(player: Player): Boolean {
        return !teleportingPlayers.containsKey(player.name)
    }
    
    fun isTeleporting(player: Player): Boolean {
        return teleportingPlayers.containsKey(player.name)
    }

    private fun playArrivalEffects(player: Player) {
        if (plugin.configManager.isParticlesEnabled) {
            
            val radius = 1.5
            for (i in 0..360 step 15) { 
                val angle = Math.toRadians(i.toDouble())
                val x = cos(angle) * radius
                val z = sin(angle) * radius
                val vec = player.add(x, 0.5, z)
                player.level.addParticle(ElectricSparkParticle(vec))
            }
            
            player.level.addParticle(HugeExplodeParticle(player.add(0.0, 1.0, 0.0)))
        }

        if (plugin.configManager.isSoundEnabled) {
        
            player.level.addSound(player, Sound.BEACON_ACTIVATE, 1f, 1f)
            player.level.addSound(player, Sound.RANDOM_EXPLODE, 0.5f, 1f)
        }
    }

    private fun logTeleport(player: Player, homeName: String, homeData: Map<String, Any>) {
        if (!plugin.configManager.isLogEnabled) return

        val logData = mapOf(
            "player" to player.name,
            "home" to homeName,
            "coordinates" to mapOf(
                "x" to homeData["x"],
                "y" to homeData["y"],
                "z" to homeData["z"],
                "world" to homeData["world"]
            ),
            "time" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
            "ip" to player.address
        )
        
        logConfig.set("${System.currentTimeMillis()}-${UUID.randomUUID()}", logData)
        logConfig.save()
    }
}