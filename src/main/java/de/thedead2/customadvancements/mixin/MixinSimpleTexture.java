package de.thedead2.customadvancements.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import de.thedead2.customadvancements.util.ModHelper;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.thedead2.customadvancements.util.ModHelper.LOGGER;
import static de.thedead2.customadvancements.util.ModHelper.TEXTURES;


@Mixin(SimpleTexture.class)
public abstract class MixinSimpleTexture {

    @Shadow protected abstract void doLoad(NativeImage pImage, boolean pBlur, boolean pClamp);
    @Shadow @Final protected ResourceLocation location;

    @Inject(at = @At("HEAD"), method = "load", cancellable = true)
    public void load(ResourceManager pResourceManager, CallbackInfo ci) {
        if(this.location.getNamespace().equals(ModHelper.MOD_ID) && TEXTURES.containsKey(this.location) && !ModHelper.ConfigManager.OPTIFINE_SHADER_COMPATIBILITY.get()){
            ci.cancel();

            boolean blurIn = false;
            boolean clampIn = false;

            NativeImage nativeimage = TEXTURES.get(this.location);

            if(nativeimage != null) {
                if (!RenderSystem.isOnRenderThreadOrInit()) {
                    RenderSystem.recordRenderCall(() -> this.doLoad(nativeimage, blurIn, clampIn));
                }
                else {
                    this.doLoad(nativeimage, blurIn, clampIn);
                }
            }
            else {
                LOGGER.error("Could not load texture for: " + this.location);
            }
        }
    }
}
