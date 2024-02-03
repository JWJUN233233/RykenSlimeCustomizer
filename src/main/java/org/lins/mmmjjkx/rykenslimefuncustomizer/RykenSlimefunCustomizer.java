package org.lins.mmmjjkx.rykenslimefuncustomizer;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.commands.MainCommand;

import java.util.Objects;

public final class RykenSlimefunCustomizer extends JavaPlugin implements SlimefunAddon {
    public static RykenSlimefunCustomizer INSTANCE;
    public static ProjectAddonManager addonManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE = this;
        addonManager = new ProjectAddonManager();

        saveDefaultConfig();
        saveConfig();

        if (getConfig().getBoolean("saveExample")) {
            saveExample();
        }

        addonManager.setup(this);

        Objects.requireNonNull(getCommand("rykenslimecustomizer")).setExecutor(new MainCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void reload() {
        INSTANCE.reloadConfig();
        addonManager.reload(INSTANCE);
        
        if (INSTANCE.getConfig().getBoolean("saveExample")) {
            saveExample();
        }
    }

    @NotNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/SlimefunReloadingProject/RykenSlimeCustomizer/issues";
    }

    public static void saveExample() {
        String head = "addons/example/";
        INSTANCE.saveResource(head + "info.yml", true);
        INSTANCE.saveResource(head + "menus.yml", false);
        INSTANCE.saveResource(head + "groups.yml", false);
        INSTANCE.saveResource(head + "machines.yml", false);
        INSTANCE.saveResource(head + "researches.yml", false);
        INSTANCE.saveResource(head + "items.yml", false);
        INSTANCE.saveResource(head + "generators.yml", false);
        INSTANCE.saveResource(head + "mat_generators.yml", false);
        INSTANCE.saveResource(head + "scripts/example_item_2.js", false);
        INSTANCE.saveResource(head + "scripts/example_machine.js", false);
        INSTANCE.saveResource(head + "scripts/example_machine_energy.js", false);
    }
}
