package me.hiztree.pancaketowny;

import com.google.common.collect.Lists;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class PancakeTowny extends JavaPlugin implements Listener {

    private Logger logger = Logger.getLogger("PancakeTowny");
    private List<ItemStack> items;

    public void onEnable() {
        File dir = new File("plugins/PancakeTowny");
        File configFile = new File(dir, "config.yml");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                saveResource("config.yml", true);
            } catch (IOException e) {
                logger.severe("Could not create config.yml!");
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        items = Lists.newArrayList();

        for (String item : config.getStringList("Blocks")) {
            int itemID = 0;
            byte itemData = 0;

            if (item.contains(":")) {
                String[] itemSplit = item.split(":");
                itemID = Integer.parseInt(itemSplit[0]);
                itemData = Byte.parseByte(itemSplit[1]);
            } else {
                itemID = Integer.parseInt(item);
            }

            items.add(new ItemStack(itemID, 1, (short) 0, itemData));
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = event.getPlayer();

            if(!player.hasPermission("towny.admin")) {
                Block block = event.getClickedBlock();

                int itemID = block.getTypeId();
                byte itemData = block.getData();

                for (ItemStack item : items) {
                    if ((item.getTypeId() == itemID) && (item.getData().getData() == itemData)) {

                        try {
                            if (WorldCoord.parseWorldCoord(player).getTownBlock().hasTown()) {
                                Town town = WorldCoord.parseWorldCoord(player).getTownBlock().getTown();
                                Resident resident = TownyUniverse.getDataSource().getResident(player.getName());

                                if (!town.getResidents().contains(resident)) {
                                    event.setCancelled(true);
                                }
                            }
                        } catch (NotRegisteredException e) {}

                        break;
                    }
                }
            }
        }
    }
}
