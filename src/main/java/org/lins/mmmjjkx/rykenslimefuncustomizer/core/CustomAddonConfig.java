package org.lins.mmmjjkx.rykenslimefuncustomizer.core;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.api.abstracts.ScriptEval;

public record CustomAddonConfig(File configFile, YamlConfiguration config, ScriptEval onReloadHandler) {
    public CustomAddonConfig(File configFile, YamlConfiguration config, @Nullable ScriptEval onReloadHandler) {
        this.configFile = configFile;
        this.config = config;
        this.onReloadHandler = onReloadHandler;
    }

    public void tryReload() {
        try {
            config.load(configFile);
            if (onReloadHandler != null) {
                onReloadHandler.evalFunction("onConfigReload", config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
