package org.lins.mmmjjkx.rykenslimefuncustomizer.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MenuPreview implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("rykensc.menu_preview")) {
            if (args.length == 0) {

            }
        }
        return false;
    }
}