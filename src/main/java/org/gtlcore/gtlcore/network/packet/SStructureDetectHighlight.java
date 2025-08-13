package org.gtlcore.gtlcore.network.packet;

import org.gtlcore.gtlcore.client.renderer.BlockHighlightHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.glodblock.github.glodium.network.packet.IMessage;

public class SStructureDetectHighlight implements IMessage<SStructureDetectHighlight> {

    private BlockPos pos;

    private ResourceKey<Level> dim;

    private long time;

    public SStructureDetectHighlight() {}

    public SStructureDetectHighlight(BlockPos pos, ResourceKey<Level> dim, long time) {
        this.pos = pos;
        this.dim = dim;
        this.time = time;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarLong(this.pos.asLong());
        buf.writeResourceKey(this.dim);
        buf.writeVarLong(this.time);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf) {
        this.pos = BlockPos.of(buf.readVarLong());
        this.dim = buf.readResourceKey(Registries.DIMENSION);
        this.time = buf.readVarLong();
    }

    @Override
    public void onMessage(Player player) {
        BlockHighlightHandler.highlight(this.pos, this.dim, this.time);
    }

    @Override
    public Class<SStructureDetectHighlight> getPacketClass() {
        return SStructureDetectHighlight.class;
    }

    @Override
    public boolean isClient() {
        return true;
    }
}
