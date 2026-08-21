package de.thedead2.customadvancements.network;


import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static de.thedead2.customadvancements.util.core.ModHelper.MOD_ID;

public record SyncBackgroundDataPayload(Map<ResourceLocation, JsonElement> backgroundData) implements CustomPacketPayload {

    public static final Type<SyncBackgroundDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sync_backgrounds"));

    public static final StreamCodec<ByteBuf, JsonElement> JSON_ELEMENT_CODEC = ByteBufCodecs.fromCodec(ExtraCodecs.JSON);

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, JsonElement>> BACKGROUND_DATA_CODEC =
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, JSON_ELEMENT_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBackgroundDataPayload> CODEC =
            StreamCodec.composite(
                    BACKGROUND_DATA_CODEC, SyncBackgroundDataPayload::backgroundData,
                    SyncBackgroundDataPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}