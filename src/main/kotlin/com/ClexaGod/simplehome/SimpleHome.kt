package com.ClexaGod.simplehome

import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.TextFormat
import com.ClexaGod.simplehome.command.HomeCommand
import com.ClexaGod.simplehome.ui.HomeUI
import com.ClexaGod.simplehome.listener.EventListener
import com.ClexaGod.simplehome.manager.ConfigManager
import com.ClexaGod.simplehome.manager.HomeManager
import com.ClexaGod.simplehome.manager.TeleportManager

class SimpleHome : PluginBase() {

    lateinit var configManager: ConfigManager
        private set
    lateinit var homeManager: HomeManager
        private set
    lateinit var teleportManager: TeleportManager
        private set
    lateinit var ui: HomeUI
        private set

    override fun onEnable() {
        
        saveDefaultConfig()

        
        configManager = ConfigManager(this)
        homeManager = HomeManager(this)
        teleportManager = TeleportManager(this)
        ui = HomeUI(this)

        server.commandMap.register("simplehome", HomeCommand(this))

        
        server.pluginManager.registerEvents(EventListener(this), this)

        logger.info("${TextFormat.GREEN}SimpleHome ${TextFormat.RED}(https://github.com/ClexaGod/SimpleHome) ${TextFormat.GREEN}enabled successfully!")
    }

    override fun onDisable() {
        logger.info("${TextFormat.RED}SimpleHome disabled.")
    }
}
