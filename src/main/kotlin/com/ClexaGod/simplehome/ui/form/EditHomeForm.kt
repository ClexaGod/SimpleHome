package com.ClexaGod.simplehome.ui.form

import cn.nukkit.Player
import cn.nukkit.form.element.custom.ElementDropdown
import cn.nukkit.form.element.custom.ElementInput
import cn.nukkit.form.element.simple.ButtonImage
import cn.nukkit.form.window.CustomForm
import cn.nukkit.form.window.SimpleForm
import cn.nukkit.utils.TextFormat
import com.ClexaGod.simplehome.SimpleHome
import com.ClexaGod.simplehome.ui.HomeUI
import com.ClexaGod.simplehome.utils.Constants

class EditHomeForm(private val plugin: SimpleHome, private val ui: HomeUI) {

    fun openSelect(player: Player) {
        val homes = plugin.homeManager.getHomes(player)
        
        if (homes.isEmpty()) {
            player.sendMessage("${TextFormat.RED}No homes found!")
            return
        }

        val window = SimpleForm("Edit Home", "${TextFormat.GRAY}Select a home to edit:")
        val homeNames = homes.keys.toList()
        val descriptionColor = TextFormat.DARK_AQUA
        
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
                openDetail(p, homeName)
            } else if (index == homeNames.size) {
                ui.mainMenu.open(p)
            }
        }
        
        window.send(player)
    }

    fun openDetail(player: Player, homeName: String) {
        val homeData = plugin.homeManager.getHomeData(player, homeName)
        if (homeData == null) {
            player.sendMessage("${TextFormat.RED}Home not found!")
            return
        }

        val currentIcon = homeData["icon"] as? String ?: "DEFAULT"
        val icons = Constants.HOME_ICONS.keys.toList()
        val defaultIconIndex = icons.indexOf(currentIcon).takeIf { it != -1 } ?: 0

        val window = CustomForm("Edit Home: $homeName")
        window.addElement(ElementInput("${TextFormat.GOLD}New Name ${TextFormat.GRAY}(Leave empty to keep)", "Home Name", homeName))
        window.addElement(ElementDropdown("${TextFormat.GOLD}Select Icon", icons, defaultIconIndex))
        
        window.onSubmit {
            p, response ->
            if (response == null) return@onSubmit
            val newNameRaw = response.getInputResponse(0)
            val iconIndex = response.getDropdownResponse(1).elementId()
            
            val newIcon = if (iconIndex in icons.indices) icons[iconIndex] else "DEFAULT"
            
            if (!newNameRaw.isNullOrBlank() && !plugin.homeManager.isValidName(newNameRaw)) {
                p.sendMessage("${TextFormat.RED}Invalid characters! Please use only English letters, numbers, '_' and '-'.")
                openDetail(p, homeName) // Re-open
                return@onSubmit
            }

            val finalName = if (newNameRaw.isNullOrBlank()) homeName else plugin.homeManager.sanitizeName(newNameRaw)
            
            if (finalName != homeName && plugin.homeManager.homeExists(p, finalName)) {
                p.sendMessage("${TextFormat.RED}Home '$finalName' already exists!")
                openDetail(p, homeName) // Re-open
                return@onSubmit
            }
            
            plugin.homeManager.updateHome(p, homeName, finalName, newIcon)
            p.sendMessage("${TextFormat.GREEN}Home updated successfully!")
            openSelect(p)
        }
        
        window.send(player)
    }
}
