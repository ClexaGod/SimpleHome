package com.ClexaGod.simplehome.command

import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.utils.TextFormat
import com.ClexaGod.simplehome.SimpleHome

class HomeCommand(private val plugin: SimpleHome) : Command("home") {

    init {
        this.description = "Opens the Home System menu"
        this.usage = "/home"
        this.permission = "simplehome.command.home"
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${TextFormat.RED}This command is only for players.")
            return true
        }

        if (!testPermission(sender)) {
            return true
        }

        plugin.ui.openMainMenu(sender)
        return true
    }
}
