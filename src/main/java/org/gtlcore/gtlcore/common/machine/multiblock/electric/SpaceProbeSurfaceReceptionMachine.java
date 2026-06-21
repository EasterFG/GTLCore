package org.gtlcore.gtlcore.common.machine.multiblock.electric;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;
import org.gtlcore.gtlcore.common.data.GTLMaterials;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author EasterFG on 2024/12/2
 */
public class SpaceProbeSurfaceReceptionMachine extends WorkableElectricMultiblockMachine {

    @Nullable
    private BlockPos cachedCheckPos;

    public SpaceProbeSurfaceReceptionMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        cachedCheckPos = findTopBlock();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        cachedCheckPos = null;
    }

    @Nullable
    protected BlockPos findTopBlock() {
        Level level = getLevel();
        if (level == null) return null;

        BlockPos pos = getPos();
        BlockPos[] coordinates = new BlockPos[] {
                pos.offset(4, 8, 0),
                pos.offset(-4, 8, 0),
                pos.offset(0, 8, 4),
                pos.offset(0, 8, -4)
        };

        for (BlockPos checkPos : coordinates) {
            if (level.getBlockState(checkPos).is(ChemicalHelper.getBlock(TagPrefix.frameGt, GTLMaterials.BlackTitanium))) {
                return checkPos.offset(0, 1, 0);
            }
        }
        return null;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (cachedCheckPos == null) return false;
        if (!Objects.requireNonNull(getLevel()).canSeeSky(cachedCheckPos)) {
            RecipeResult.of(this, RecipeResult.fail(Component.translatable("gtceu.recipe.fail.block")));
            return false;
        }
        return super.beforeWorking(recipe);
    }
}
