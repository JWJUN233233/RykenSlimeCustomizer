package org.lins.mmmjjkx.rykenslimefuncustomizer.objects;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomGenerator;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomMaterialGenerator;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.parent.AbstractEmptyMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomGeoResource;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter(AccessLevel.PACKAGE)
public class ProjectAddon {
    //info.yml
    private @NotNull final String addonId;
    private @NotNull final String addonName;
    private @NotNull final String addonVersion;
    private @NotNull final List<String> pluginDepends;
    private @NotNull final List<String> depends;
    private @NotNull final File folder;

    //groups.yml
    private List<ItemGroup> itemGroups = new ArrayList<>();
    //menus.yml
    private List<CustomMenu> menus = new ArrayList<>();
    //geo_resources.yml
    private List<CustomGeoResource> geoResources = new ArrayList<>();
    //items.yml
    private List<CustomItem> items = new ArrayList<>();
    //machines.yml
    private List<AbstractEmptyMachine> machines = new ArrayList<>();
    //researches.yml
    private List<Research> researches = new ArrayList<>();
    //generators.yml
    private List<CustomGenerator> generators = new ArrayList<>();
    //mat_generators.yml
    private List<CustomMaterialGenerator> materialGenerators = new ArrayList<>();

    public File getScriptsFolder() {
        File scripts = new File(folder, "scripts");
        if (!scripts.exists()) {
            scripts.mkdirs();
        }
        return scripts;
    }

    public File getSavedItemsFolder() {
        File savedItems = new File(folder, "saveditems");
        if (!savedItems.exists()) {
            savedItems.mkdirs();
        }
        return savedItems;
    }

    public void unregister() {
        itemGroups.forEach(ig -> Slimefun.getRegistry().getAllItemGroups().remove(ig));
        researches.forEach(Research::disable);
        menus.forEach(m -> Slimefun.getRegistry().getMenuPresets().remove(m.getID()));
        items.forEach(this::unregisterItem);
        machines.forEach(this::unregisterItem);
        generators.forEach(this::unregisterItem);
        geoResources.forEach(this::unregisterItem);
        materialGenerators.forEach(this::unregisterItem);

        researches.clear();
        items.clear();
        machines.clear();
        itemGroups.clear();
        menus.clear();
        geoResources.clear();
        generators.clear();
        materialGenerators.clear();
    }

    public void unregisterItem(SlimefunItem item) {
        item.disable();
        Slimefun.getRegistry().getDisabledSlimefunItems().remove(item);
        Slimefun.getRegistry().getSlimefunItemIds().remove(item.getId());
        Slimefun.getRegistry().getAllSlimefunItems().remove(item);
    }
}
