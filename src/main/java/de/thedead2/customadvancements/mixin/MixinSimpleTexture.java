package de.thedead2.customadvancements.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import de.thedead2.customadvancements.CustomAdvancements;
import de.thedead2.customadvancements.util.ModHelper;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.thedead2.customadvancements.util.ModHelper.TEXTURES;


@Mixin(SimpleTexture.class)
public abstract class MixinSimpleTexture {

    @Shadow @Final protected ResourceLocation textureLocation;
    @Shadow protected abstract void loadImage(NativeImage imageIn, boolean blurIn, boolean clampIn);


    @Inject(at = @At("HEAD"), method = "loadTexture", cancellable = true)
    public void loadTexture(IResourceManager resourceManager, CallbackInfo ci) {
        if(this.textureLocation.getNamespace().equals(ModHelper.MOD_ID) && TEXTURES.containsKey(this.textureLocation) && !ModHelper.ConfigManager.OPTIFINE_SHADER_COMPATIBILITY.get()){
            ci.cancel();

            boolean blurIn = false;
            boolean clampIn = false;

            NativeImage nativeimage = TEXTURES.get(this.textureLocation);

            if(nativeimage != null) {
                if (!RenderSystem.isOnRenderThreadOrInit()) {
                    RenderSystem.recordRenderCall(() -> this.loadImage(nativeimage, blurIn, clampIn));
                }
                else {
                    this.loadImage(nativeimage, blurIn, clampIn);
                }
            }
            else {
                CustomAdvancements.LOGGER.error("Could not load texture for: " + this.textureLocation);
            }
        }
        else if (this.textureLocation.getNamespace().equals(ModHelper.MOD_ID) && !TEXTURES.containsKey(this.textureLocation)){
            CustomAdvancements.LOGGER.debug("Couldn't find texture location {} in custom advancements textures map! Using normal method to load texture!", this.textureLocation);
        }
    }
}
