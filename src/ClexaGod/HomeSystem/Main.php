<?php

namespace ClexaGod\HomeSystem;

use pocketmine\plugin\PluginBase;
use pocketmine\event\Listener;
use pocketmine\player\Player;
use pocketmine\command\Command;
use pocketmine\command\CommandSender;
use pocketmine\utils\Config;
use pocketmine\world\Position;
use pocketmine\scheduler\Task;
use pocketmine\event\player\PlayerMoveEvent;
use pocketmine\event\entity\EntityDamageByEntityEvent;
use pocketmine\event\entity\EntityDamageEvent;
use pocketmine\world\particle\HappyVillagerParticle;
use pocketmine\network\mcpe\protocol\PlaySoundPacket;
use pocketmine\network\mcpe\protocol\StopSoundPacket;
use jojoe77777\FormAPI\SimpleForm;
use jojoe77777\FormAPI\CustomForm;

class Main extends PluginBase implements Listener {
    private Config $homes;
    private Config $config;
    private Config $teleportLogs;
    private array $allowedWorlds;
    private int $maxHomes;
    private array $teleportQueue = [];
    public array $teleportingPlayers = [];
    private array $homeIcons = [
    "DEFAULT" => "textures/ui/village_hero_effect",
    "FREINDS" => "textures/ui/FriendsIcon",
    "BED" => "textures/items/bed_red",
    "CHEST" => "textures/blocks/chest_front",
    "MİNECRAFT" => "textures/pack_icon",
    "HEART" => "textures/ui/heart",
    "DIAMOND" => "textures/items/diamond",
    "FURNACE" => "textures/blocks/furnace_front_off",
    "ANVIL" => "textures/ui/anvil_icon",
    "CRAFTING" => "textures/blocks/crafting_table_front",
    "WORLD" => "textures/ui/worldsIcon",
    "COMPASS" => "textures/items/compass_item",
    "ENDER_CHEST" => "textures/blocks/ender_chest_front",
    "BEACON" => "textures/blocks/beacon",
    "BOOK" => "textures/items/book_written",
    "EMERALD" => "textures/items/emerald", 
    "JUMP" => "textures/gui/controls/jump"
];

    public function onEnable(): void {
        $this->getServer()->getPluginManager()->registerEvents($this, $this);
        
        if (!file_exists($this->getDataFolder())) {
            mkdir($this->getDataFolder());
        }
        
        $this->saveDefaultConfig();
        $this->config = $this->getConfig();
        $this->homes = new Config($this->getDataFolder() . "homes.yml", Config::YAML, []);
        $this->teleportLogs = new Config($this->getDataFolder() . "teleport_logs.yml", Config::YAML, []);
        
        $this->loadConfigValues();
    }

    private function loadConfigValues(): void {
        $this->allowedWorlds = $this->config->getNested("allowed-worlds", ["world"]);
        $this->maxHomes = $this->config->getNested("settings.max-homes", 5);
    }

    public function onCommand(CommandSender $sender, Command $command, string $label, array $args): bool {
        if(!$sender instanceof Player) {
            $sender->sendMessage("Only players can use this command!");
            return false;
        }

        if($command->getName() === "home") {
            $this->openMainMenu($sender);
            return true;
        }
        
        return false;
    }

    private function sanitizeName(string $name): string {
        return preg_replace('/[^a-zA-Z0-9_\-]/', '', trim($name));
    }

    private function openMainMenu(Player $player): void {
    $form = new SimpleForm(function(Player $player, ?int $data) {
        if($data === null) return;
        
        switch($data) {
            case 0: 
                $this->openHomeListMenu($player, true);
                break;
            case 1: 
                $this->openSetHomeMenu($player);
                break;
            case 2: 
                $this->openEditHomeMenu($player);
                break;
            case 3:
                $this->openHomeListMenu($player, false);
                break;
        }
    });

    $homes = $this->getPlayerHomes($player);
    $form->setTitle("§6Home System");
    $form->setContent("§7Select the action you want to take:\n\n§7Count home: §f" . count($homes) . "/" . $this->maxHomes);
    $form->addButton("§6Go to my homes\n§7Click to teleport", 0, "textures/ui/worldsIcon");
    $form->addButton("§6Save Home\n§7Add new home", 0, "textures/ui/realms_green_check");
    $form->addButton("§6Home Edit\n§7Change name and icon", 0, "textures/ui/pencil_edit_icon");
    $form->addButton("§cDelete Home\n§7Click to delete home", 0, "textures/ui/realms_red_x");
    $player->sendForm($form);
}

    private function openSetHomeMenu(Player $player): void {
    $homes = $this->getPlayerHomes($player);
    
    if(count($homes) >= $this->maxHomes) {
        $player->sendMessage("You have reached the maximum number of homes! (" . $this->maxHomes . " Home)");
        return;
    }

    if(!$this->isWorldAllowed($player->getWorld()->getFolderName())) {
        $player->sendMessage("You can't save a home in this world!");
        return;
    }

    $form = new CustomForm(function(Player $player, ?array $data) {
        if($data === null) {
            $this->openMainMenu($player);
            return;
        }

        if(!isset($data[0])) {
            $player->sendMessage("Please enter a valid home name!");
            return;
        }

        $homeName = $data[0];
        
        if(empty($homeName)) {
            $player->sendMessage(" Please enter a valid home name!");
            return;
        }

        if($this->homeExists($player, $this->sanitizeName($homeName))) {
            $player->sendMessage("There's already a home with that name!");
            return;
        }

        $this->setHome($player, $homeName);
    });

    $form->setTitle("Home Save");
    $form->addInput("Home name", "Remaining home rights: " . ($this->maxHomes - count($homes)), "");
    $player->sendForm($form);
}

    private function openHomeListMenu(Player $player, bool $isTeleport): void {
    $homes = $this->getPlayerHomes($player);
    
    if(empty($homes)) {
        $player->sendMessage("§cNo registered home!");
        return;
    }

    $form = new SimpleForm(function(Player $player, ?int $data) use ($homes, $isTeleport) {
        if($data === null) {
            $this->openMainMenu($player);
            return;
        }

        $homeCount = count($homes);
        if($data === $homeCount) { 
            $this->openMainMenu($player);
            return;
        }

        $homeNames = array_keys($homes);
        if(isset($homeNames[$data])) {
            if($isTeleport) {
                $this->teleportToHome($player, $homeNames[$data]);
            } else {
                $this->confirmDelete($player, $homeNames[$data]);
            }
        }
    });

    $form->setTitle($isTeleport ? "§6My homes" : "§cDelete Home");
    $form->setContent($isTeleport ? "§7Select the home you want to teleport to:" : "§7Select the home you want to delete:");
    
    foreach($homes as $name => $home) {
        $icon = $home["icon"] ?? "DEFAULT";
        $form->addButton($name . "\n§7Location: §f" . round($home["x"]) . ", " . round($home["y"]) . ", " . round($home["z"]), 0, $this->homeIcons[$icon]);
    }
    
    $form->addButton("§7Go Back", 0, "textures/ui/arrow_left");
    
    $player->sendForm($form);
}

    private function showCoordinates(Player $player, string $homeName): void {
        $homes = $this->getPlayerHomes($player);
        if(!isset($homes[$homeName])) return;

        $home = $homes[$homeName];
        $coords = "X: " . round($home["x"], 2) . 
                 "\nY: " . round($home["y"], 2) . 
                 "\nZ: " . round($home["z"], 2) . 
                 "\nWorld: " . $home["world"];

        $form = new SimpleForm(function(Player $player, ?int $data) {
            if($data === null) {
                $this->openHomeListMenu($player, true);
            }
        });

        $form->setTitle($homeName . " - Coordinates");
        $form->setContent($coords);
        $form->addButton("Go Back", 0, "textures/ui/arrow_left");
        $player->sendForm($form);
    }

    private function confirmDelete(Player $player, string $homeName): void {
        $form = new SimpleForm(function(Player $player, ?int $data) use ($homeName) {
            if($data === null) {
                $this->openHomeListMenu($player, false);
                return;
            }

            switch($data) {
                case 0:
                    $this->deleteHome($player, $homeName);
                    break;
                case 1:
                    $this->openHomeListMenu($player, false);
                    break;
            }
        });

        $form->setTitle("Delete Home - verification");
        $form->setContent("Are you sure you want to delete the house named?" $homeName. );
        $form->addButton("Yes", 0, "textures/ui/realms_green_check");
        $form->addButton("No", 0, "textures/ui/realms_red_x");
        $player->sendForm($form);
    }

    private function getPlayerHomes(Player $player): array {
        return $this->homes->getNested($player->getName() . ".homes", []);
    }

    private function homeExists(Player $player, string $name): bool {
        $homes = $this->getPlayerHomes($player);
        return isset($homes[$name]);
    }

    private function isWorldAllowed(string $worldName): bool {
        return in_array($worldName, $this->allowedWorlds);
    }

    private function setHome(Player $player, string $name): void {
    $homes = $this->getPlayerHomes($player);
    $safeName = $this->sanitizeName($name);
    
    if(empty($safeName)) {
        $player->sendMessage("§cInvalid home name!");
        return;
    }

    if(!$this->isWorldAllowed($player->getWorld()->getFolderName())) {
        $player->sendMessage("§cYou can't save a home in this world!");
        return;
    }
    
    $pos = $player->getPosition();
    $homes[$safeName] = [
        "x" => $pos->getX(),
        "y" => $pos->getY(),
        "z" => $pos->getZ(),
        "world" => $pos->getWorld()->getFolderName(),
        "icon" => "DEFAULT"
    ];
    
    $this->homes->setNested($player->getName() . ".homes", $homes);
    $this->homes->save();
    
    $player->sendMessage("§aHome successfully registered: §f" . $safeName);
}

    private function deleteHome(Player $player, string $name): void {
        $homes = $this->getPlayerHomes($player);
        $safeName = $this->sanitizeName($name);
        
        if(!isset($homes[$safeName])) {
            $player->sendMessage("This home could not be found!");
            return;
        }
        
        unset($homes[$safeName]);
        $this->homes->setNested($player->getName() . ".homes", $homes);
        $this->homes->save();
        
        $player->sendMessage("The home was successfully wiped: " . $safeName);
    }

    public function logTeleport(Player $player, string $homeName, array $homeData): void {
        if(!$this->config->getNested("settings.log-teleports", true)) return;

        $log = [
            "player" => $player->getName(),
            "home" => $homeName,
            "coordinates" => [
                "x" => $homeData["x"],
                "y" => $homeData["y"],
                "z" => $homeData["z"],
                "world" => $homeData["world"]
            ],
            "time" => date("Y-m-d H:i:s"),
            "ip" => $player->getNetworkSession()->getIp()
        ];

        $this->teleportLogs->set(time() . "-" . uniqid(), $log);
        $this->teleportLogs->save();
    }

    public function playTeleportEffects(Player $player): void {
        if(!$this->config->getNested("effects.enabled", true)) return;

        
        if($this->config->getNested("effects.particles", true)) {
            for($i = 0; $i < 20; $i++) {
                $player->getWorld()->addParticle(
                    $player->getPosition()->add(
                        mt_rand(-10, 10) / 10,
                        mt_rand(0, 20) / 10,
                        mt_rand(-10, 10) / 10
                    ),
                    new HappyVillagerParticle()
                );
            }
        }

        if($this->config->getNested("effects.sound", true)) {
            $pk = new PlaySoundPacket();
            $pk->soundName = "mob.endermen.portal";
            $pk->x = $player->getPosition()->getX();
            $pk->y = $player->getPosition()->getY();
            $pk->z = $player->getPosition()->getZ();
            $pk->volume = 1;
            $pk->pitch = 1;
            $player->getNetworkSession()->sendDataPacket($pk);
        }
    }

    private function teleportToHome(Player $player, string $name): void {
    $homes = $this->getPlayerHomes($player);
    $safeName = $this->sanitizeName($name);
    
    if(!isset($homes[$safeName])) {
        $player->sendMessage("§cThis home could not be found!");
        return;
    }

    $home = $homes[$safeName];
    $world = $this->getServer()->getWorldManager()->getWorldByName($home["world"]);
    
    if($world === null) {
        $player->sendMessage("§cThe world where the home is located is not loaded!");
        return;
    }

    if(isset($this->teleportingPlayers[$player->getName()])) {
        $player->sendMessage("§cThe warming process is already underway!");
        return;
    }
    
    try {
        $position = new Position(
            (float)$home["x"],
            (float)$home["y"],
            (float)$home["z"],
            $world
        );

        $delay = $this->config->getNested("settings.teleport-delay", 3);
        if($delay > 0) {
            $player->sendMessage("§eStarting to warm up, Please §f" . $delay . " §eWait a second...");
            
            $task = new TeleportTask($this, $player, $position, $safeName, $home, $delay);
            $taskHandler = $this->getScheduler()->scheduleRepeatingTask($task, 20);
            $this->teleportingPlayers[$player->getName()] = $taskHandler;
        } else {
            $player->teleport($position);
            $this->playTeleportEffects($player);
            $this->logTeleport($player, $safeName, $home);
            $player->sendMessage("§a" . $safeName . " §fyou teleported home");
        }
    } catch (\Exception $e) {
        $player->sendMessage("§cThere was an error during the warm-up!");
        $this->getLogger()->error("Teleport Error: " . $e->getMessage());
    }
}

    public function onPlayerMove(PlayerMoveEvent $event): void {
    $player = $event->getPlayer();
    $from = $event->getFrom();
    $to = $event->getTo();
   
    if(isset($this->teleportingPlayers[$player->getName()]) && 
       ($from->getFloorX() !== $to->getFloorX() || 
        $from->getFloorY() !== $to->getFloorY() || 
        $from->getFloorZ() !== $to->getFloorZ())) {
        
        $this->cancelTeleport($player);
    }
}
     public function onEntityDamage(EntityDamageEvent $event): void {
    $player = $event->getEntity();
    
    if(!$player instanceof Player) return;
    
    if($event instanceof EntityDamageByEntityEvent) {
        $damager = $event->getDamager();
        if($damager instanceof Player) {

            if(isset($this->teleportingPlayers[$player->getName()])) {
                $this->cancelTeleport($player);
            }
            if(isset($this->teleportingPlayers[$damager->getName()])) {
                $this->cancelTeleport($damager);
            }
        }
    }
}
     private function cancelTeleport(Player $player): void {
    if(isset($this->teleportingPlayers[$player->getName()])) {
        $taskHandler = $this->teleportingPlayers[$player->getName()];
        $taskHandler->cancel(); 
        unset($this->teleportingPlayers[$player->getName()]); 
        $player->sendMessage("§cThe warm-up has been canceled! Canceled for moving or fighting.");
    }
}
   private function openEditHomeMenu(Player $player): void {
    $homes = $this->getPlayerHomes($player);
    
    if(empty($homes)) {
        $player->sendMessage("§cNo registered homes!");
        return;
    }

    $form = new SimpleForm(function(Player $player, ?int $data) use ($homes) {
        if($data === null) {
            $this->openMainMenu($player);
            return;
        }

        $homeNames = array_keys($homes);
        if(isset($homeNames[$data])) {
            $this->openHomeEditDetailMenu($player, $homeNames[$data]);
        }
    });

    $form->setTitle("§6Home Edit");
    $form->setContent("§7Select the home you want to edit:");
    
    foreach($homes as $name => $home) {
        $icon = $home["icon"] ?? "DEFAULT";
        $form->addButton($name . "\n§7Location: §f" . round($home["x"]) . ", " . round($home["y"]) . ", " . round($home["z"]), 0, $this->homeIcons[$icon]);
    }
    $player->sendForm($form);
}
      
      private function openHomeEditDetailMenu(Player $player, string $homeName): void {
    $homes = $this->getPlayerHomes($player);
    if(!isset($homes[$homeName])) {
        $player->sendMessage("§cNo home found!");
        return;
    }

    $home = $homes[$homeName];
    
    if(!isset($home["x"]) || !isset($home["y"]) || !isset($home["z"]) || !isset($home["world"])) {
        $player->sendMessage("§cHome data is corrupted!");
        return;
    }

    $currentIcon = $home["icon"] ?? "DEFAULT";

    $form = new CustomForm(function(Player $player, ?array $data) use ($homeName, $home) {
        if($data === null) {
            $this->openEditHomeMenu($player);
            return;
        }

        $newName = $data[0];
        $selectedIcon = array_keys($this->homeIcons)[$data[1]];

        if(!empty($newName) && $newName !== $homeName) {
            $safeName = $this->sanitizeName($newName);
            if($this->homeExists($player, $safeName) && $safeName !== $homeName) {
                $player->sendMessage("§cThere's already a home with that name!");
                return;
            }
            
            $homes = $this->getPlayerHomes($player);
            if($safeName !== $homeName) {
                $homeData = $homes[$homeName];
                unset($homes[$homeName]);
                $homes[$safeName] = $homeData;
                $homeName = $safeName;
            }

            $homes[$homeName] = [
                "x" => $home["x"],
                "y" => $home["y"],
                "z" => $home["z"],
                "world" => $home["world"],
                "icon" => $selectedIcon
            ];
            
            $this->homes->setNested($player->getName() . ".homes", $homes);
            $this->homes->save();
            
            $player->sendMessage("§aThe home has been successfully updated!");
            $this->openEditHomeMenu($player);
        } else {
            $homes = $this->getPlayerHomes($player);
            $homes[$homeName]["icon"] = $selectedIcon;
            
            $this->homes->setNested($player->getName() . ".homes", $homes);
            $this->homes->save();
            
            $player->sendMessage("§aThe home has been successfully updated!");
            $this->openEditHomeMenu($player);
        }
    });

    $form->setTitle("§6Home Edit: §f" . $homeName);
    $form->addInput("§6New Name §7(You can leave blank)", "Home name", $homeName);
    
    $iconNames = array_map(function($key) {
        return "§6" . ucfirst(strtolower($key));
    }, array_keys($this->homeIcons));
    $currentIconIndex = array_search($currentIcon, array_keys($this->homeIcons));
    $form->addDropdown("§6Icon Selection", $iconNames, $currentIconIndex);
    
    $player->sendForm($form);
}
}

class TeleportTask extends Task {
    private Main $plugin;
    private Player $player;
    private Position $position;
    private string $homeName;
    private array $homeData;
    private int $countdown;
    private bool $cancelled = false;

    public function __construct(Main $plugin, Player $player, Position $position, string $homeName, array $homeData, int $countdown) {
        $this->plugin = $plugin;
        $this->player = $player;
        $this->position = $position;
        $this->homeName = $homeName;
        $this->homeData = $homeData;
        $this->countdown = $countdown;
    }
    public function onRun(): void {
        if(!$this->player->isOnline() || $this->cancelled) {
            $this->getHandler()->cancel();
            return;
        }

        if($this->countdown > 0) {
            $this->player->sendTip("§eTo be warmed §f" . $this->countdown . " §esecond...");
            $this->countdown--;
            return;
        }

        $this->player->teleport($this->position);
        $this->plugin->playTeleportEffects($this->player);
        $this->plugin->logTeleport($this->player, $this->homeName, $this->homeData);
        $this->player->sendMessage("§a" . $this->homeName . " §fyou have been named home!");
        
        unset($this->plugin->teleportingPlayers[$this->player->getName()]);
        $this->getHandler()->cancel();
    }

    public function cancel(): void {
        $this->cancelled = true;
    }
}