package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in;

import java.io.File;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.parent.ScriptCreator;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.parent.ScriptEval;

public final class JavaScriptCreator implements ScriptCreator {
    @Override
    public String scriptKey() {
        return "script";
    }

    @Override
    public String getFileName(String name) {
        return name + ".js";
    }

    @Override
    public ScriptEval createScript(File file, ProjectAddon addon) {
        return new JavaScriptEval(file, addon);
    }
}
