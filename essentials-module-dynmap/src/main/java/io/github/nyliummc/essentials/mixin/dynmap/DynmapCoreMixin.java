package io.github.nyliummc.essentials.mixin.dynmap;

import io.github.nyliummc.essentials.api.EssentialsMod;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;
import org.dynmap.DynmapCore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.*;

@Mixin(DynmapCore.class)
class DynmapCoreMixin {
    @Redirect(method="initConfiguration", at=@At(value="NEW", target="java.io.File", ordinal=0))
    File configFile() throws IOException {
        File f = new File(FabricLoader.getInstance().getConfigDirectory().getCanonicalPath() + "/essentials-dynmap.yaml");
        if (!f.exists()) {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("essentials-dynmap.yaml");
            OutputStream out = new FileOutputStream(f);
            IOUtils.copy(in, out);
        }
        return f;
    }
}
