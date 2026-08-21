package de.thedead2.customadvancements.network;

import com.google.gson.JsonElement;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static de.thedead2.customadvancements.network.SyncBackgroundDataPayload.JSON_ELEMENT_CODEC;
import static de.thedead2.customadvancements.util.core.ModHelper.MOD_ID;

public record SyncLangDataPayload(Map<String, JsonElement> langData) implements CustomPacketPayload {

    public static final Type<SyncLangDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sync_lang_data"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<String, JsonElement>> TRANSLATIONS_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, JSON_ELEMENT_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncLangDataPayload> STREAM_CODEC = StreamCodec.composite(
            TRANSLATIONS_CODEC, SyncLangDataPayload::langData,
            SyncLangDataPayload::new
    );


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
