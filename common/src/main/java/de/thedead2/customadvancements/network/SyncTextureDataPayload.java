package de.thedead2.customadvancements.network;

import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SyncTextureDataPayload(ResourceLocation textureId, int chunkIndex, int totalChunks, byte[] chunkData) implements CustomPacketPayload {

    public static final Type<SyncTextureDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModHelper.MOD_ID, "sync_texture"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTextureDataPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SyncTextureDataPayload::textureId,
            ByteBufCodecs.VAR_INT, SyncTextureDataPayload::chunkIndex,
            ByteBufCodecs.VAR_INT, SyncTextureDataPayload::totalChunks,
            ByteBufCodecs.BYTE_ARRAY, SyncTextureDataPayload::chunkData,
            SyncTextureDataPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
