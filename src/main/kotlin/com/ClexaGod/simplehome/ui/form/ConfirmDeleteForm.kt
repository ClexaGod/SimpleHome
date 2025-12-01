package com.ClexaGod.simplehome.ui.form

import cn.nukkit.Player
import cn.nukkit.form.element.simple.ButtonImage
import cn.nukkit.form.window.SimpleForm
import cn.nukkit.utils.TextFormat
import com.ClexaGod.simplehome.SimpleHome
import com.ClexaGod.simplehome.ui.HomeUI

class ConfirmDeleteForm(private val plugin: SimpleHome, private val ui: HomeUI) {

    fun open(player: Player, homeName: String) {
        val window = SimpleForm("Delete Home", "Are you sure you want to delete home '$homeName'?")
        window.addButton("Yes", ButtonImage(ButtonImage.Type.PATH, "textures/ui/realms_green_check"))
        window.addButton("No", ButtonImage(ButtonImage.Type.PATH, "textures/ui/realms_red_x"))
        
        window.onSubmit {
            p, response ->
            if (response == null) return@onSubmit
            if (response.buttonId() == 0) { // Yes
                plugin.homeManager.deleteHome(p, homeName)
                p.sendMessage("${TextFormat.GREEN}Home '$homeName' deleted.")
            }
            ui.homeListForm.open(p, false)
        }
        
        window.send(player)
    }
}
