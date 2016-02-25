package lumien.randomthings.block;

import lumien.randomthings.handler.ModDimensions;
import lumien.randomthings.handler.spectre.SpectreCube;
import lumien.randomthings.handler.spectre.SpectreHandler;
import lumien.randomthings.item.ItemIngredient;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.IExplosionImmune;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSpectreCore extends BlockBase implements IExplosionImmune
{
	enum ORIENTATION implements IStringSerializable
	{
		NW("nw"), NE("ne"), ES("es"), SW("sw");

		String value;

		private ORIENTATION(String value)
		{
			this.value = value;
		}

		@Override
		public String getName()
		{
			return value;
		}
	}

	public static PropertyEnum<BlockSpectreCore.ORIENTATION> orientation = PropertyEnum.<BlockSpectreCore.ORIENTATION> create("orientation", BlockSpectreCore.ORIENTATION.class);

	protected BlockSpectreCore()
	{
		super("spectreCore", Material.rock);

		this.setBlockUnbreakable().setStepSound(soundTypeGlass);
		this.setResistance(6000000.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(orientation, ORIENTATION.NW));
		this.setCreativeTab(null);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		ORIENTATION blockPosition;

		if (worldIn.getBlockState(pos.offset(EnumFacing.NORTH)).getBlock() != this && worldIn.getBlockState(pos.offset(EnumFacing.WEST)).getBlock() != this)
		{
			blockPosition = ORIENTATION.NW;
		}
		else if (worldIn.getBlockState(pos.offset(EnumFacing.NORTH)).getBlock() != this && worldIn.getBlockState(pos.offset(EnumFacing.EAST)).getBlock() != this)
		{
			blockPosition = ORIENTATION.NE;
		}
		else if (worldIn.getBlockState(pos.offset(EnumFacing.EAST)).getBlock() != this && worldIn.getBlockState(pos.offset(EnumFacing.SOUTH)).getBlock() != this)
		{
			blockPosition = ORIENTATION.ES;
		}
		else
		{
			blockPosition = ORIENTATION.SW;
		}

		return state.withProperty(orientation, blockPosition);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return 0;
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, new IProperty[] { orientation });
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity)
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.TRANSLUCENT;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
	{
		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();

		if (block == this || iblockstate.getBlock() == ModBlocks.spectreBlock)
		{
			return false;
		}

		if (worldIn.getBlockState(pos.offset(side.getOpposite())) != iblockstate)
		{
			return true;
		}

		return block == this ? false : super.shouldSideBeRendered(worldIn, pos, side);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (worldIn.provider.getDimensionId() == ModDimensions.SPECTRE_ID)
		{
			ItemStack holding;
			if ((holding = playerIn.getCurrentEquippedItem()) != null)
			{
				if (holding.getItem() == ModItems.ingredients && holding.getItemDamage() == ItemIngredient.INGREDIENT.ECTO_PLASM.id)
				{
					if (!worldIn.isRemote)
					{
						SpectreCube cube = SpectreHandler.getInstance().getSpectreCubeFromPos(worldIn, pos.up());

						if (cube != null)
						{
							holding.stackSize -= cube.increaseHeight(holding.stackSize);
						}
					}
					return true;
				}
			}
		}

		return super.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
	}
}