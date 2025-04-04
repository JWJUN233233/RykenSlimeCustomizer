package org.lins.mmmjjkx.rykenslimefuncustomizer.core.yaml.item;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.PiglinBarterDrop;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactivity;
import io.github.thebusybiscuit.slimefun4.core.attributes.Soulbound;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.utils.ColoredMaterial;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.api.DropFromBlock;
import org.lins.mmmjjkx.rykenslimefuncustomizer.api.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.api.abstracts.BaseRadiationItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.api.abstracts.CustomItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.api.script.JavaScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.core.slimefun.WitherProofBlockImpl;
import org.lins.mmmjjkx.rykenslimefuncustomizer.core.slimefun.item.CustomDefaultItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.core.slimefun.item.CustomUnplaceableItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.core.slimefun.item.existence.*;
import org.lins.mmmjjkx.rykenslimefuncustomizer.core.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ClassUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public class ItemReader extends YamlReader<SlimefunItem> {
    public ItemReader(YamlConfiguration config, ProjectAddon addon) {
        super(config, addon);
    }

    @SneakyThrows
    @Override
    public SlimefunItem readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        String id = addon.getId(s, section.getString("id_alias"));

        ExceptionHandler.HandleResult result = ExceptionHandler.handleIdConflict(id);

        if (result == ExceptionHandler.HandleResult.FAILED) return null;

        String igId = section.getString("item_group");

        Pair<ExceptionHandler.HandleResult, ItemGroup> group = ExceptionHandler.handleItemGroupGet(addon, igId);
        if (group.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        SlimefunItemStack sfis = getPreloadItem(id);
        if (sfis == null) return null;

        ItemStack[] itemStacks = CommonUtils.readRecipe(section.getConfigurationSection("recipe"), addon);
        String recipeType = section.getString("recipe_type", "NULL");

        boolean piglin = section.getBoolean("piglin_trade_chance", false);
        RecipeType rt;
        if (piglin) {
            rt = RecipeType.BARTER_DROP;
        } else {
            Pair<ExceptionHandler.HandleResult, RecipeType> rt1 = ExceptionHandler.getRecipeType(
                    "在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "错误的配方类型" + recipeType + "!", recipeType);

            if (rt1.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

            rt = rt1.getSecondValue();
        }

        JavaScriptEval eval = null;
        if (section.contains("script")) {
            String script = section.getString("script", "");
            File file = new File(addon.getScriptsFolder(), script + ".js");
            if (!file.exists()) {
                ExceptionHandler.handleWarning(
                        "在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "找不到脚本文件" + file.getName());
            } else {
                ExceptionHandler.debugLog("加载了附属" + addon.getAddonId() + "中物品" + s + "的脚本文件" + file.getName());
                eval = new JavaScriptEval(file, addon);
            }
        }

        CustomItem instance;

        boolean energy = section.contains("energy_capacity");
        boolean hasRadiation = section.contains("radiation");

        // 将在粘液发布下一个正式版后删除
        if (hasRadiation) {
            return setupRadiationItem(section, sfis, group.getSecondValue(), s, rt, itemStacks, eval, addon);
        }

        if (energy) {
            double energyCapacity = section.getDouble("energy_capacity");
            if (energyCapacity < 1) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "能源容量不能小于1");
                return null;
            }

            CommonUtils.addLore(sfis, true, CMIChatColor.translate("&8⇨ &e⚡ &70 / " + energyCapacity + " J"));

            instance = new CustomEnergyItem(group.getSecondValue(), sfis, rt, itemStacks, (float) energyCapacity, eval);
        } else if (section.getBoolean("placeable", false)) {
            instance = new CustomDefaultItem(group.getSecondValue(), sfis, rt, itemStacks);
        } else if (section.contains("rainbow")) {
            String materialType = section.getString("rainbow", "");
            if (!sfis.getType().isBlock()) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "非方块无法设置彩虹属性");
                return null;
            }
            if (materialType.equalsIgnoreCase("CUSTOM")) {
                List<String> materials = section.getStringList("rainbow_materials");
                if (materials.isEmpty()) {
                    ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "彩虹属性材料列表为空");
                    return null;
                }
                List<Material> colorMaterials = new ArrayList<>();

                for (String material : materials) {
                    Pair<ExceptionHandler.HandleResult, Material> materialPair = ExceptionHandler.handleEnumValueOf(
                            "在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "错误的彩虹属性材料: " + material,
                            Material.class,
                            material);
                    Material material1 = materialPair.getSecondValue();
                    if (materialPair.getFirstValue() == ExceptionHandler.HandleResult.FAILED || material1 == null) {
                        return null;
                    }
                    colorMaterials.add(material1);
                }

                instance = new CustomRainbowBlock(group.getSecondValue(), sfis, rt, itemStacks, colorMaterials);
            } else {
                Pair<ExceptionHandler.HandleResult, ColoredMaterial> coloredMaterialPair =
                        ExceptionHandler.handleEnumValueOf(
                                "在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "错误的可染色材料类型: " + materialType,
                                ColoredMaterial.class,
                                materialType);
                ColoredMaterial coloredMaterial = coloredMaterialPair.getSecondValue();
                if (coloredMaterialPair.getFirstValue() == ExceptionHandler.HandleResult.FAILED
                        || coloredMaterial == null) {
                    return null;
                }
                instance = new CustomRainbowBlock(group.getSecondValue(), sfis, rt, itemStacks, coloredMaterial);
            }
        } else {
            instance = new CustomUnplaceableItem(group.getSecondValue(), sfis, rt, itemStacks, eval);
        }

        Object[] constructorArgs = instance.constructorArgs();

        if (section.getBoolean("anti_wither", false)) {
            if (!sfis.getType().isBlock()) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "非方块无法设置防凋零属性");
                return null;
            }

            Class<? extends CustomItem> clazz = (Class<? extends CustomItem>) ClassUtils.generateClass(
                    instance.getClass(), "WitherProof", "Item", new Class[] {WitherProofBlockImpl.class}, null);

            instance = (CustomItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
        }

        if (section.getBoolean("soulbound", false)) {
            Class<? extends CustomItem> clazz = (Class<? extends CustomItem>) ClassUtils.generateClass(
                    instance.getClass(), "Soulbound", "Item", new Class[] {Soulbound.class}, null);

            instance = (CustomItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
        }

        if (section.contains("piglin_trade_chance")) {
            int chance = section.getInt("piglin_trade_chance", 100);
            if (chance < 0 || chance > 100) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "猪灵交易掉落几率必须在0-100之间！已转为100");
                chance = 100;
            }

            int finalChance = chance;
            Class<? extends CustomItem> clazz = (Class<? extends CustomItem>) ClassUtils.generateClass(
                    instance.getClass(),
                    "PiglinTradeAble",
                    "Item",
                    new Class[] {PiglinBarterDrop.class},
                    builder -> builder.method(ElementMatchers.isDeclaredBy(PiglinBarterDrop.class))
                            .intercept(FixedValue.value(finalChance)));

            instance = (CustomItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
        }

        instance.setHidden(section.getBoolean("hidden", false));
        instance.setUseableInWorkbench(section.getBoolean("vanilla", false));

        if (section.contains("drop_from")) {
            int chance = section.getInt("drop_chance", 100);
            int amount = section.isInt("drop_amount") ? section.getInt("drop_amount", 1) : -1;

            if (chance < 0 || chance > 100) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "掉落几率" + chance + "不在0-100范围内! 已转为100");
                chance = 100;
            }

            String dropMaterial = section.getString("drop_from", "");

            Optional<Material> xm = Optional.ofNullable(Material.matchMaterial(dropMaterial));
            if (xm.isPresent()) {
                Material material = xm.get();
                if (amount == -1) {
                    String between = section.getString("drop_amount", "1");
                    if (between.contains("-")) {
                        String[] split = between.split("-");
                        if (split.length == 2) {
                            int min = Integer.parseInt(split[0]);
                            int max = Integer.parseInt(split[1]);
                            DropFromBlock.addDrop(material, new DropFromBlock.Drop(sfis, chance, addon, min, max));
                        } else {
                            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: "
                                    + "无法读取掉落数量区间" + between + "，已将掉落数量转为1");
                            DropFromBlock.addDrop(material, new DropFromBlock.Drop(sfis, chance, addon));
                        }
                    }
                } else {
                    DropFromBlock.addDrop(material, new DropFromBlock.Drop(sfis, chance, addon, amount, amount));
                }
            } else {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载物品" + s + "时遇到了问题: " + "指定掉落方块材料类型" + dropMaterial + "不存在!");
            }
        }

        instance.register(RykenSlimefunCustomizer.INSTANCE);

        return instance;
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String key) {
        ConfigurationSection section = configuration.getConfigurationSection(key);

        if (section == null) return null;

        ConfigurationSection item = section.getConfigurationSection("item");
        ItemStack stack = CommonUtils.readItem(item, false, addon);
        if (stack == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品" + key + "时遇到了问题: " + "物品为空或格式错误导致无法加载");
            return null;
        }

        return List.of(new SlimefunItemStack(addon.getId(key, section.getString("id_alias")), stack));
    }

    @SneakyThrows
    @Deprecated(forRemoval = true, since = "RSC 1.4")
    private SlimefunItem setupRadiationItem(
            ConfigurationSection section,
            SlimefunItemStack original,
            ItemGroup itemGroup,
            String id,
            RecipeType recipeType,
            ItemStack[] recipe,
            JavaScriptEval eval,
            ProjectAddon addon) {

        String radio = section.getString("radiation");
        Pair<ExceptionHandler.HandleResult, Radioactivity> radioactivityPair = ExceptionHandler.handleEnumValueOf(
                "在附属" + addon.getAddonId() + "中加载物品" + id + "时遇到了问题: " + "错误的辐射等级级别: " + radio,
                Radioactivity.class,
                radio);
        Radioactivity radioactivity = radioactivityPair.getSecondValue();

        if (radioactivityPair.getFirstValue() == ExceptionHandler.HandleResult.FAILED || radioactivity == null) {
            return null;
        }

        boolean energy = section.contains("energy_capacity");

        CommonUtils.addLore(original, true, CMIChatColor.translate(radioactivity.getLore()));

        BaseRadiationItem instance;

        if (energy) {
            double energyCapacity = section.getDouble("energy_capacity");
            instance = new CustomEnergyRadiationItem(
                    itemGroup, original, recipeType, recipe, radioactivity, (float) energyCapacity, eval);
        } else if (section.getBoolean("placeable", false)) {
            instance = new CustomDefaultRadiationItem(itemGroup, original, recipeType, recipe, radioactivity);
        } else {
            instance = new CustomRadiationItem(itemGroup, original, recipeType, recipe, eval, radioactivity);
        }

        Object[] constructorArgs = instance.constructArgs();

        if (section.getBoolean("anti_wither", false)) {
            if (!original.getType().isBlock()) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品" + id + "时遇到了问题: " + "非方块无法设置防凋零属性");
                return null;
            }

            Class<? extends BaseRadiationItem> clazz = (Class<? extends BaseRadiationItem>) ClassUtils.generateClass(
                    instance.getClass(), "WitherProof", "Item", new Class[] {WitherProofBlockImpl.class}, null);

            instance = (BaseRadiationItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
        }

        if (section.getBoolean("soulbound", false)) {
            Class<? extends BaseRadiationItem> clazz = (Class<? extends BaseRadiationItem>) ClassUtils.generateClass(
                    instance.getClass(), "Soulbound", "Item", new Class[] {Soulbound.class}, null);

            instance = (BaseRadiationItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
        }

        boolean piglin = section.contains("piglin_trade_chance");

        if (piglin) {
            int chance = section.getInt("piglin_trade_chance", 100);
            if (chance < 0 || chance > 100) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载物品" + id + "时遇到了问题: " + "猪灵交易掉落几率必须在0-100之间！已转为100");
                chance = 100;
            }

            int finalChance = chance;
            Class<? extends CustomItem> clazz = (Class<? extends CustomItem>) ClassUtils.generateClass(
                    instance.getClass(),
                    "PiglinTradeAble",
                    "Item",
                    new Class[] {PiglinBarterDrop.class},
                    builder -> builder.method(ElementMatchers.isDeclaredBy(PiglinBarterDrop.class))
                            .intercept(FixedValue.value(finalChance)));

            instance = (BaseRadiationItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
        }

        instance.setHidden(section.getBoolean("hidden", false));
        instance.setUseableInWorkbench(section.getBoolean("vanilla", false));

        if (section.contains("drop_from")) {
            int chance = section.getInt("drop_chance", 100);
            int amount = section.isInt("drop_amount") ? section.getInt("drop_amount", 1) : -1;

            if (chance < 0 || chance > 100) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品" + id + "时遇到了问题: " + "掉落几率" + chance
                        + "不在0-100范围内! 已转为100");
                chance = 100;
            }

            String dropMaterial = section.getString("drop_from", "");

            Optional<Material> xm = Optional.ofNullable(Material.matchMaterial(dropMaterial));
            if (xm.isPresent()) {
                Material material = xm.get();
                if (amount == -1) {
                    String between = section.getString("drop_amount", "1");
                    if (between.contains("-")) {
                        String[] split = between.split("-");
                        if (split.length == 2) {
                            int min = Integer.parseInt(split[0]);
                            int max = Integer.parseInt(split[1]);
                            DropFromBlock.addDrop(material, new DropFromBlock.Drop(original, chance, addon, min, max));
                        } else {
                            ExceptionHandler.handleError(
                                    "无法在附属" + addon.getAddonName() + "中读取掉落数量区间" + between + "，已把掉落数量转为1");
                            DropFromBlock.addDrop(material, new DropFromBlock.Drop(original, chance, addon));
                        }
                    }
                } else {
                    DropFromBlock.addDrop(material, new DropFromBlock.Drop(original, chance, addon, amount, amount));
                }
            } else {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载物品" + id + "时遇到了问题: " + "指定掉落方块材料类型" + dropMaterial + "不存在!");
            }
        }

        instance.register(RykenSlimefunCustomizer.INSTANCE);

        return instance;
    }
}
