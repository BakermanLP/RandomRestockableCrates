package zairus.randomrestockablecrates.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;

import zairus.randomrestockablecrates.RandomRestockableCrates;
import zairus.randomrestockablecrates.tileentity.TileEntityCrate;

public class BlockCrate extends BlockContainer implements ITileEntityProvider {
	public static final AxisAlignedBB CRATE_BOUNDING_BOX = new AxisAlignedBB(0.08F, 0.0F, 0.08F, 0.93F, 0.93F, 0.93F);
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool OPEN = PropertyBool.create("open");
	
	private String modName;
	
	private final int crateTier;
	
	public BlockCrate(Material material, int tier) {
		super(material);
		this.crateTier = tier;
		this.setCreativeTab(RandomRestockableCrates.tabCrates);
		this.setResistance(6000000.0F);
		this.setHardness(1.9F);
		this.setHarvestLevel("axe", 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, false));
		
		this.setBlockUnbreakable();
		tier += 1;
		String name = "block_crate_" + tier;
		SoundType type = SoundType.WOOD;
		if (tier > 2) {
			type = SoundType.METAL;
		}
		this.setSoundType(type);
		this.setRegistryName(name);
		this.setUnlocalizedName(name);
		this.modName = name;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return CRATE_BOUNDING_BOX;
	}
	
	protected Block setSoundType(SoundType sound) {
		super.setSoundType(sound);
		return this;
	}
	
	public int getTier() {
		return this.crateTier;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		// world.playSound(player, pos, RRCSoundEvents.CRATE_OPEN, SoundCategory.BLOCKS, 1.0F, 1.2F / (world.rand.nextFloat() * 0.2f + 0.9f));
		
		if (world.isRemote) {
			return true;
		} else {
			ILockableContainer container = this.getLockableContainer(world, pos);
			
			if (container != null) {
				player.openGui(RandomRestockableCrates.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				
				return true;
			}
		}
		
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCrate(crateTier);
	}
	
	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
		return Container.calcRedstoneFromInventory(this.getLockableContainer(world, pos));
	}
	
	public ILockableContainer getLockableContainer(World world, BlockPos pos) {
		TileEntity tileentity = world.getTileEntity(pos);
		
		ILockableContainer ilockablecontainer = (TileEntityCrate) tileentity;
		
		return ilockablecontainer;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, OPEN);
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		boolean o = state.getValue(OPEN);
		int f = state.getValue(FACING).getIndex();
		
		if (o) {
			f += 5;
		}
		
		return f;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		int m = meta;
		boolean o = false;
		
		if (m > 5) {
			m -= 5;
			o = true;
		}
		
		EnumFacing enumfacing = EnumFacing.getFront(m);
		
		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}
		
		IBlockState state = getDefaultState().withProperty(FACING, enumfacing).withProperty(OPEN, o);
		
		return state;
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityCrate crate = (TileEntityCrate) world.getTileEntity(pos);
		
		boolean isOpen = crate.getIsOpen();
		
		return state.withProperty(OPEN, isOpen);
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing enumfacing = placer.getHorizontalFacing().getOpposite();
		state = state.withProperty(FACING, enumfacing).withProperty(OPEN, false);
		world.setBlockState(pos, state, 2);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand)
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite())
				.withProperty(OPEN, false);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	public String getModName() {
		return this.modName;
	}
	
	public enum EnumTier implements IStringSerializable {
		TIER1(0, "tier1"),
		TIER2(1, "tier2"),
		TIER3(2, "tier3"),
		TIER4(3, "tier4");
		
		private final String name;
		private final int meta;
		
		EnumTier(int meta, String name) {
			this.meta = meta;
			this.name = name;
		}
		
		public static EnumTier fromMeta(int meta) {
			switch (meta) {
				case 1:
					return TIER2;
				case 2:
					return TIER3;
				case 3:
					return TIER4;
				default:
					return TIER1;
			}
		}
		
		@Override
		public String getName() {
			return this.name;
		}
		
		public int getMetadata() {
			return this.meta;
		}
	}
}
