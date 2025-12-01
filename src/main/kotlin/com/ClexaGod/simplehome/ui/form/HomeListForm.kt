package com.ClexaGod.simplehome.ui.form

import cn.nukkit.Player
import cn.nukkit.form.element.simple.ButtonImage
import cn.nukkit.form.window.SimpleForm
import cn.nukkit.utils.TextFormat
import com.ClexaGod.simplehome.SimpleHome
import com.ClexaGod.simplehome.ui.HomeUI
import com.ClexaGod.simplehome.utils.Constants

class HomeListForm(private val plugin: SimpleHome, private val ui: HomeUI) {

    fun open(player: Player, isTeleport: Boolean) {
        val homes = plugin.homeManager.getHomes(player)
        
        if (homes.isEmpty()) {
            player.sendMessage("${TextFormat.RED}No homes found!")
            return
        }

        val title = if (isTeleport) "${TextFormat.GOLD}My Homes" else "${TextFormat.RED}Delete Home"
        val content = if (isTeleport) "${TextFormat.GRAY}Select a home to teleport:" else "${TextFormat.GRAY}Select a home to delete:"
        val descriptionColor = TextFormat.DARK_AQUA
        
        val window = SimpleForm(title, content)
        
        val homeNames = homes.keys.toList()
        
        for ((name, data) in homes) {
            @Suppress("UNCHECKED_CAST")
            val homeData = data as Map<String, Any>
            val iconKey = homeData["icon"] as? String ?: "DEFAULT"
            val iconPath = Constants.HOME_ICONS[iconKey] ?: "textures/ui/village_hero_effect"
            
            val x = (homeData["x"] as Double).toInt()
            val y = (homeData["y"] as Double).toInt()
            val z = (homeData["z"] as Double).toInt()
            
            window.addButton("$name\n${descriptionColor}Loc: $x, $y, $z", ButtonImage(ButtonImage.Type.PATH, iconPath))
        }
        
        window.addButton("Back", ButtonImage(ButtonImage.Type.PATH, "textures/ui/arrow_left"))
        
        window.onSubmit {
            p, response ->
            if (response == null) return@onSubmit
            val index = response.buttonId()
            
            if (index >= 0 && index < homeNames.size) {
                val homeName = homeNames[index]
                if (isTeleport) {
                    plugin.teleportManager.teleportToHome(p, homeName)
                } else {
                    ui.confirmDeleteForm.open(p, homeName)
                }
            } else if (index == homeNames.size) {
                ui.mainMenu.open(p)
            }
        }
        
        window.send(player)
    }
}
