package com.ClexaGod.simplehome.listener

import cn.nukkit.Player
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.player.PlayerMoveEvent
import com.ClexaGod.simplehome.SimpleHome

class EventListener(private val plugin: SimpleHome) : Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val from = event.from
        val to = event.to

        if (from.floorX != to.floorX || from.floorY != to.floorY || from.floorZ != to.floorZ) {
            if (plugin.teleportManager.isTeleporting(player)) {
                plugin.teleportManager.cancelTeleport(player, "Moved")
            }
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity is Player) {
            if (event is EntityDamageByEntityEvent) {
                val damager = event.damager
                if (damager is Player) {
                    if (plugin.teleportManager.isTeleporting(entity)) {
                        plugin.teleportManager.cancelTeleport(entity, "In combat")
                    }
                    if (plugin.teleportManager.isTeleporting(damager)) {
                        plugin.teleportManager.cancelTeleport(damager, "In combat")
                    }
                }
            }
        }
    }
}
