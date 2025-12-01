package com.ClexaGod.simplehome.ui.form

import cn.nukkit.Player
import cn.nukkit.form.element.simple.ButtonImage
import cn.nukkit.form.window.SimpleForm
import cn.nukkit.utils.TextFormat
import com.ClexaGod.simplehome.SimpleHome
import com.ClexaGod.simplehome.ui.HomeUI

class MainMenu(private val plugin: SimpleHome, private val ui: HomeUI) {

    fun open(player: Player) {
        val homeCount = plugin.homeManager.getHomeCount(player)
        val maxHomes = plugin.configManager.maxHomes
        val descriptionColor = TextFormat.DARK_AQUA

        val window = SimpleForm("Home", "${TextFormat.GRAY}Select an action:\n\n${TextFormat.GRAY}Homes: ${TextFormat.WHITE}$homeCount/$maxHomes")
        
        window.addButton("My Homes\n${descriptionColor}Click to teleport", ButtonImage(ButtonImage.Type.PATH, "textures/ui/worldsIcon"))
        window.addButton("Set Home\n${descriptionColor}Add new home", ButtonImage(ButtonImage.Type.PATH, "textures/ui/realms_green_check"))
        window.addButton("Edit Home\n${descriptionColor}Change name/icon", ButtonImage(ButtonImage.Type.PATH, "textures/ui/pencil_edit_icon"))
        window.addButton("Delete Home\n${descriptionColor}Click to delete", ButtonImage(ButtonImage.Type.PATH, "textures/ui/realms_red_x"))

        window.onSubmit {
            p, response ->
            if (response == null) return@onSubmit
            when (response.buttonId()) {
                0 -> ui.homeListForm.open(p, true)
                1 -> ui.setHomeForm.open(p)
                2 -> ui.editHomeForm.openSelect(p)
                3 -> ui.homeListForm.open(p, false)
            }
        }

        window.send(player)
    }
}
