package com.ClexaGod.simplehome.ui

import cn.nukkit.Player
import com.ClexaGod.simplehome.SimpleHome
import com.ClexaGod.simplehome.ui.form.*

class HomeUI(private val plugin: SimpleHome) {

    val mainMenu = MainMenu(plugin, this)
    val setHomeForm = SetHomeForm(plugin)
    val homeListForm = HomeListForm(plugin, this)
    val editHomeForm = EditHomeForm(plugin, this)
    val confirmDeleteForm = ConfirmDeleteForm(plugin, this)

    fun openMainMenu(player: Player) {
        mainMenu.open(player)
    }
}
