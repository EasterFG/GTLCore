package org.gtlcore.gtlcore.mixin.ldlib;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Submap;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.model.custommodel.*;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.*;
import java.util.concurrent.*;

import javax.annotation.Nonnull;

/**
 * @author EasterFG on 2025/3/2
 */
@Mixin(CustomBakedModel.class)
public abstract class CustomBakedModelFixed implements BakedModel {

    @Mutable
    @Final
    @Shadow(remap = false)
    private final BakedModel parent;
    protected final ConcurrentMap<Direction, ConcurrentMap<Connections, List<BakedQuad>>> sideCache = new ConcurrentHashMap<>();
    protected final List<BakedQuad> noSideCache = new ObjectArrayList<>();

    protected CustomBakedModelFixed(BakedModel parent) {
        this.parent = parent;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    @Nonnull
    @Deprecated
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return parent.getQuads(state, side, rand);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    @Nonnull
    public List<BakedQuad> getCustomQuads(BlockAndTintGetter level, BlockPos pos, @Nonnull BlockState state, @Nullable Direction side, RandomSource rand) {
        var connections = Connections.checkConnections(level, pos, state, side);
        if (side == null) {
            if (noSideCache.isEmpty()) {
                synchronized (noSideCache) {
                    if (noSideCache.isEmpty()) {
                        noSideCache.addAll(buildCustomQuads(connections, parent.getQuads(state, null, rand), 0.0f));
                    }
                }
            }
            return noSideCache;
        }
        return sideCache
                .computeIfAbsent(side, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(connections, key -> buildCustomQuads(key, parent.getQuads(state, side, rand), 0.0f));
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public static List<BakedQuad> reBakeCustomQuads(List<BakedQuad> quads, BlockAndTintGetter level, BlockPos pos, @Nonnull BlockState state, @Nullable Direction side, float offset) {
        return buildCustomQuads(Connections.checkConnections(level, pos, state, side), quads, offset);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public static List<BakedQuad> buildCustomQuads(Connections connections, List<BakedQuad> base, float offset) {
        List<BakedQuad> result = new LinkedList<>();
        for (BakedQuad bakedQuad : base) {
            var section = LDLMetadataSection.getMetadata(bakedQuad.getSprite());
            TextureAtlasSprite connection = section.connection == null ? null : ModelFactory.getBlockSprite(section.connection);
            if (connection == null) {
                result.add(makeQuad(bakedQuad, section, offset).rebake());
                continue;
            }

            Quad quad = makeQuad(bakedQuad, section, offset).derotate();
            Quad[] quads = quad.subdivide(4);

            int[] ctm = connections.getSubmapIndices();

            for (int j = 0; j < quads.length; j++) {
                Quad q = quads[j];
                if (q != null) {
                    int ctmid = q.getUvs().normalize().getQuadrant();
                    quads[j] = q.grow().transformUVs(ctm[ctmid] > 15 ? bakedQuad.getSprite() : connection, Submap.uvs[ctm[ctmid]]);
                }
            }
            result.addAll(Arrays.stream(quads).filter(Objects::nonNull).map(Quad::rebake).toList());
        }
        return result;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    protected static Quad makeQuad(BakedQuad bq, LDLMetadataSection section, float offset) {
        Quad q = Quad.from(bq, offset);
        if (section.emissive) {
            q = q.setLight(15, 15);
        }
        return q;
    }
}
