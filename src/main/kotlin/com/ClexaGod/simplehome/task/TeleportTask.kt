package com.ClexaGod.simplehome.task

import cn.nukkit.Player
import cn.nukkit.level.Location
import cn.nukkit.level.Sound
import cn.nukkit.level.particle.EnchantmentTableParticle
import cn.nukkit.level.particle.PortalParticle
import cn.nukkit.math.Vector3
import cn.nukkit.scheduler.Task
import cn.nukkit.utils.TextFormat
import com.ClexaGod.simplehome.manager.TeleportManager
import kotlin.math.cos
import kotlin.math.sin

class TeleportTask(
    private val manager: TeleportManager,
    private val player: Player,
    private val location: Location,
    private val homeName: String,
    private val homeData: Map<String, Any>,
    private var secondsLeft: Int
) : Task() {

    private var ticksPassed = 0

    override fun onRun(currentTick: Int) {
        if (!player.isOnline || manager.isCancelled(player)) {
            this.cancel()
            return
        }


        val angle = ticksPassed * 0.5 
        val radius = 1.0
        val y = (ticksPassed % 20) / 10.0 
        
        val x = cos(angle) * radius
        val z = sin(angle) * radius
        
        val particlePos = player.add(x, y, z)
        player.level.addParticle(EnchantmentTableParticle(particlePos))
        player.level.addParticle(PortalParticle(player.add(-x, y + 0.5, -z))) 


        if (ticksPassed % 20 == 0) {
            if (secondsLeft > 0) {

                player.sendTip("${TextFormat.BOLD}${TextFormat.GOLD}Teleporting in ${TextFormat.AQUA}$secondsLeft ${TextFormat.GOLD}...")

                val pitch = 0.5f + ((5 - secondsLeft).coerceAtLeast(0) * 0.2f)
                player.level.addSound(player, Sound.NOTE_PLING, 1f, pitch)
                
                secondsLeft--
            } else {
                manager.executeTeleport(player, location, homeName, homeData)
                this.cancel()
            }
        }

        ticksPassed++
    }
}