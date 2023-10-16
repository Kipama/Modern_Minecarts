package net.lordkipama.modernminecarts.block.Custom;

import net.lordkipama.modernminecarts.RailSpeeds;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

public class CustomRailBlock extends RailBlock {


    public CustomRailBlock(Properties p_55395_) {
        super(p_55395_);
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        if (getRailDirection(state, level, pos, null).isAscending()) {
            return RailSpeeds.max_ascending_speed;
        } else {
            return RailSpeeds.fastest_speed;
        }
    }



    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        boolean gotReplaced = false;

        if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.ASCENDING_NORTH) {
            level.setBlock(pos, ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state), 1);
            gotReplaced = true;

        } else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.ASCENDING_SOUTH) {
            level.setBlock(pos, ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state), 1);
            gotReplaced = true;

        } else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.ASCENDING_EAST) {
            level.setBlock(pos, ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state), 1);
            gotReplaced = true;

        } else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.ASCENDING_WEST) {
            level.setBlock(pos, ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state), 1);
            gotReplaced = true;
        }


        //EXPERIMENTAL!!
        else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.NORTH_SOUTH) {
            BlockState northernBlockState = level.getBlockState(new BlockPos(pos.getX(),pos.getY(),pos.getZ()-1));
            BlockState northernBelowBlockState = level.getBlockState(new BlockPos(pos.getX(),pos.getY()-1,pos.getZ()-1));
            BlockState southernBlockState = level.getBlockState(new BlockPos(pos.getX(),pos.getY(),pos.getZ()+1));
            BlockState southernBelowBlockState = level.getBlockState(new BlockPos(pos.getX(),pos.getY()-1,pos.getZ()+1));

            if((southernBlockState.is(BlockTags.RAILS) || southernBelowBlockState.is(BlockTags.RAILS)) && !(northernBlockState.is(BlockTags.RAILS) || northernBelowBlockState.is(BlockTags.RAILS))){
                level.setBlock(pos, ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(SHAPE,RailShape.ASCENDING_NORTH)), 1);
                gotReplaced = true;
            }
            else if(!(southernBlockState.is(BlockTags.RAILS) || southernBelowBlockState.is(BlockTags.RAILS)) && (northernBlockState.is(BlockTags.RAILS) || northernBelowBlockState.is(BlockTags.RAILS))){
                level.setBlock(pos, ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(SHAPE,RailShape.ASCENDING_SOUTH)), 1);
                gotReplaced = true;
            }
        }

        else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.EAST_WEST) {
            BlockState westernBlockState = level.getBlockState(new BlockPos(pos.getX()-1,pos.getY(),pos.getZ()));
            BlockState westernBelowBlockState = level.getBlockState(new BlockPos(pos.getX()-1,pos.getY()-1,pos.getZ()));
            BlockState easternBlockState = level.getBlockState(new BlockPos(pos.getX()+1,pos.getY(),pos.getZ()));
            BlockState easternBelowBlockState = level.getBlockState(new BlockPos(pos.getX()+1,pos.getY()-1,pos.getZ()));

            if((easternBlockState.is(BlockTags.RAILS) || easternBelowBlockState.is(BlockTags.RAILS)) && !(westernBlockState.is(BlockTags.RAILS) || westernBelowBlockState.is(BlockTags.RAILS))){
                level.setBlock(pos, ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(SHAPE,RailShape.ASCENDING_WEST)), 1);
                gotReplaced = true;
            }
            else if(!(easternBlockState.is(BlockTags.RAILS) || easternBelowBlockState.is(BlockTags.RAILS)) && (westernBlockState.is(BlockTags.RAILS) || westernBelowBlockState.is(BlockTags.RAILS))){
                level.setBlock(pos, ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(SHAPE,RailShape.ASCENDING_EAST)), 1);
                gotReplaced = true;
            }
        }
            //Unchanged Shapes:
        //else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.NORTH_EAST) {} else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.NORTH_WEST) {} else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.SOUTH_EAST) {} else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.SOUTH_WEST) {}

        if(gotReplaced == true){
            if (!player.isCreative() && !player.isSpectator()) {
                itemstack.shrink(1);
            }
            player.swing(interactionHand);
        }

        return super.use(state, level, pos, player, interactionHand, blockHitResult);
    }
}
