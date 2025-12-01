package com.ClexaGod.simplehome.ui.form

import cn.nukkit.Player
import cn.nukkit.form.element.custom.ElementInput
import cn.nukkit.form.window.CustomForm
import cn.nukkit.utils.TextFormat
import com.ClexaGod.simplehome.SimpleHome

class SetHomeForm(private val plugin: SimpleHome) {

    fun open(player: Player) {
        val homeCount = plugin.homeManager.getHomeCount(player)
        val maxHomes = plugin.configManager.maxHomes

        if (homeCount >= maxHomes) {
            player.sendMessage("${TextFormat.RED}You have reached the maximum number of homes ($maxHomes)!")
            return
        }

        if (player.level.folderName !in plugin.configManager.allowedWorlds) {
            player.sendMessage("${TextFormat.RED}You cannot set a home in this world!")
            return
        }

        val window = CustomForm("Set Home")
        window.addElement(ElementInput("Home Name", "Remaining: ${maxHomes - homeCount}", ""))
        
        window.onSubmit { p, response ->
            if (response == null) return@onSubmit
            val homeName = response.getInputResponse(0)
            
            if (homeName.isNullOrBlank()) {
                p.sendMessage("${TextFormat.RED}Invalid home name!")
                return@onSubmit
            }

            if (!plugin.homeManager.isValidName(homeName)) {
                p.sendMessage("${TextFormat.RED}Invalid characters! Please use only English letters, numbers, '_' and '-'.")
                return@onSubmit
            }

            val safeName = plugin.homeManager.sanitizeName(homeName)
            if (plugin.homeManager.homeExists(p, safeName)) {
                p.sendMessage("${TextFormat.RED}Home '$safeName' already exists!")
                return@onSubmit
            }

            plugin.homeManager.setHome(p, safeName)
            p.sendMessage("${TextFormat.GREEN}Home '$safeName' set successfully!")
        }
        
        window.send(player)
    }
}
