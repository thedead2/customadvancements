package de.thedead2.customadvancements.mixin;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static de.thedead2.customadvancements.util.ModHelper.FILE_HANDLER;
import static de.thedead2.customadvancements.util.ModHelper.MOD_ID;

@Mixin(TextureManager.class)
public abstract class MixinTextureManager {

    @Shadow protected abstract Texture func_230183_b_(ResourceLocation p_230183_1_, Texture p_230183_2_);

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;func_230183_b_(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/Texture;)Lnet/minecraft/client/renderer/texture/Texture;"), method = "loadTexture")
    public Texture loadTexture(TextureManager textureManager, ResourceLocation textureLocation, Texture texture){
        return null; //textureLocation.getNamespace().equals(MOD_ID) && textureLocation.getPath().contains("textures/") ? FILE_HANDLER.getBackgroundTexture(textureLocation) : this.func_230183_b_(textureLocation, texture);
    }
}
