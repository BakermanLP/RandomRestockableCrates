package zairus.randomrestockablecrates.tileentity;

import java.util.Random;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

import zairus.randomrestockablecrates.RRCConfig;
import zairus.randomrestockablecrates.RandomRestockableCrates;
import zairus.randomrestockablecrates.inventory.ContainerCrate;
import zairus.randomrestockablecrates.sound.RRCSoundEvents;

public class TileEntityCrate extends TileEntityLockable implements ITickable, IInventory
{
	public static final int SLOT_COUNT = 27;
	
	public int playersUsing;

	private NonNullList<ItemStack> chestContents = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	private String customName;
	private long lastOpened;
	private boolean open = false;
	private boolean firstTime = true;
	
	private int tier = -1;

    private String lootTable;
    protected ResourceLocation lootTableLocation;
    protected long lootTableSeed;
	
	public TileEntityCrate()
	{
		this.tier = -1;
	}
	
	public TileEntityCrate(int crateTier)
	{
		this.tier = crateTier;
	}
	
	public String getDefaultName()
	{
		return "Crate";
	}
	
	@Override
	public String getName()
	{
		return this.hasCustomName()? this.customName : "container.crate";
	}
	
	@Override
	public boolean hasCustomName()
	{
		return customName != null;
	}
	
	public void setCustomName(String name)
	{
		this.customName = name;
	}
	
	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player)
	{
//        RandomRestockableCrates.logger.info("Restock: createContainer");
		return new ContainerCrate(this, player);
	}
	
	@Override
	public String getGuiID()
	{
		return "randomrestockablecrates:crate";
	}
	
	@Override
	public int getSizeInventory()
	{
		return SLOT_COUNT;
	}
	
	@Override
	public ItemStack getStackInSlot(int index)
	{
		return this.chestContents.get(index);
	}
	
	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		return ItemStackHelper.getAndSplit(chestContents, index, count);
	}
	
	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		return ItemStackHelper.getAndRemove(chestContents, index);
	}
	
	@Override
	public boolean isEmpty()
	{
		for (ItemStack itemstack : chestContents)
		{
			if (!itemstack.isEmpty())
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		ItemStack itemstack = this.chestContents.get(index);
		this.chestContents.set(index, stack);
		
		if (stack.getCount() > this.getInventoryStackLimit())
		{
			stack.setCount(this.getInventoryStackLimit());
		}
		this.markDirty();
	}
	
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	
	@Override
	public boolean isUsableByPlayer(EntityPlayer player)
	{
		return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
	}
	
	@Override
	public void openInventory(EntityPlayer player)
	{

//        RandomRestockableCrates.logger.info("Restock: openInventory top worldObj: " + (worldObj != null));
//        RandomRestockableCrates.logger.info("Restock: openInventory top this_worldObj: " + (world != null));

        if (world.isRemote)
        {
//            RandomRestockableCrates.logger.info("Restock: openInventory This is remote: " + (world.isRemote));
            return;
        }
        double x = this.pos.getX() + 0.5D;
        double y = this.pos.getY() + 0.5D;
        double z = this.pos.getZ() + 0.5D;

//        RandomRestockableCrates.logger.info("Restock: openInventory at (" + x + ", " + y + ", " + z + ")");
		if (!player.isSpectator())
		{
			if (this.playersUsing < 0)
			{
				this.playersUsing = 0;
			}
			
			++this.playersUsing;

            if (this.tier > -1)
            {
                long ticksEllapsed = getTime() - this.lastOpened;

                long restockTime = 0;

                switch(this.tier)
                {
                case 1:
                    restockTime = RRCConfig.tier2RestockTime;
                    break;
                case 2:
                    restockTime = RRCConfig.tier3RestockTime;
                    break;
                case 3:
                    restockTime = RRCConfig.tier4RestockTime;
                    break;
                default:
                    restockTime = RRCConfig.tier1RestockTime;
                    break;
                }

                if (ticksEllapsed >= restockTime || this.firstTime)
                {
//                    RandomRestockableCrates.logger.info("Restock: openInventory firstTime: " + this.firstTime);
//                    RandomRestockableCrates.logger.info("Restock: openInventory ticksEllapsed: " + ticksEllapsed);
//                    RandomRestockableCrates.logger.info("Restock: openInventory restockTime: " + restockTime);
                    this.firstTime = false;
	
					world.playSound((EntityPlayer)null, pos, RRCSoundEvents.CRATE_OPEN, SoundCategory.BLOCKS, 1.0F, 1.2F / (world.rand.nextFloat() * 0.2f + 0.9f));
     
//                    RandomRestockableCrates.logger.info("Restock: openInventory restock");
                    restock(world.rand);
                }

//                updateMe();
            }

			world.addBlockEvent(this.pos, this.getBlockType(), 1, this.playersUsing);
			world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
			world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType(), false);
		}
		
		if (!this.open)
			this.lastOpened = getTime();
		
		this.open = true;
		
		updateMe();
	}
	
	public int getTier()
	{
		return this.tier;
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
//        RandomRestockableCrates.logger.info("Restock: shouldRefresh");
		return super.shouldRefresh(world, pos, oldState, newState);
	}
	
	@Override
	public void update()
	{
        if (world != null && !world.isRemote)
        {
            if (this.tier > -1)
            {
                long ticksEllapsed = getTime() - this.lastOpened;
                
                long restockTime = 0;
                
                switch(this.tier)
                {
                case 1:
                    restockTime = RRCConfig.tier2RestockTime;
                    break;
                case 2:
                    restockTime = RRCConfig.tier3RestockTime;
                    break;
                case 3:
                    restockTime = RRCConfig.tier4RestockTime;
                    break;
                default:
                    restockTime = RRCConfig.tier1RestockTime;
                    break;
                }
                
                if (ticksEllapsed >= restockTime && this.lastOpened > 0)
                {
//                    RandomRestockableCrates.logger.info("Restock: update firstTime: " + this.firstTime);
//                    RandomRestockableCrates.logger.info("Restock: update ticksEllapsed: " + ticksEllapsed);
//                    RandomRestockableCrates.logger.info("Restock: update restockTime: " + restockTime);
//                    RandomRestockableCrates.logger.info("Restock: update lastOpened: " + this.lastOpened);
//                    RandomRestockableCrates.logger.info("Restock: update open: " + this.open);
                    // this.firstTime = false;
                    this.lastOpened = 0L;
                    this.open = false;
                    clear();
//                    RandomRestockableCrates.logger.info("Restock: update");
                    
 				world.playSound((EntityPlayer)null, pos, RRCSoundEvents.CRATE_OPEN, SoundCategory.BLOCKS, 1.0F, 1.2F / (world.rand.nextFloat() * 0.2f + 0.9f));
                    
                    updateMe();

                    // restock(world.rand);
                }
                
            }
        }
	}
	
	private void restock(Random rand)
	{
		if (this.tier < 0)
			return;
		
		boolean addedItem = false;
		
//		this.open = false;
//		updateMe();
		
        // Clear chest content
//        RandomRestockableCrates.logger.info("Restock: Clear chest content");

        clear();

//        RandomRestockableCrates.logger.info("Restock: in Restock");

        if (this.lootTable == null)
        {
            this.lootTable = "minecraft:chests/simple_dungeon";
        }

        if (this.lootTable != null)
        {
//            RandomRestockableCrates.logger.info("Restock: in LootTable " + this.lootTable);
            this.lootTableLocation = new ResourceLocation(this.lootTable);
            if (world.getLootTableManager() == null)
            {
//                RandomRestockableCrates.logger.info("Could not get loot manager.");
                return;
            }
            
            LootTable table = world.getLootTableManager().getLootTableFromLocation(this.lootTableLocation);
//            this.lootTable = null;
            Random random;
            
            if (this.lootTableSeed == 0L)
            {
                random = new Random();
            }
            else
            {
                random = new Random(this.lootTableSeed);
            }
            
            LootContext.Builder lootBuilder = new LootContext.Builder((WorldServer)world);

            double x = this.pos.getX() + 0.5D;
            double y = this.pos.getY() + 0.5D;
            double z = this.pos.getZ() + 0.5D;
            
            table.fillInventory(this, random, lootBuilder.build());
            RandomRestockableCrates.logger.info("Restock: refill chest at (" + x + ", " + y + ", " + z + ")");
        }

		this.markDirty();
	}
    
	public void syncValues(int ticks, long lastOpened, boolean open)
	{
//        RandomRestockableCrates.logger.info("Restock: syncValues");
		this.lastOpened = lastOpened;
		this.open = open;
	}
	
	public boolean getIsOpen()
	{
		return this.open;
	}
	
	private ItemStack getStackFromPool(NBTTagList list, Random rand)
	{
		ItemStack stack = ItemStack.EMPTY;
		
		NBTTagCompound curElement = list.getCompoundTagAt(rand.nextInt(list.tagCount()));
		
		if (curElement != null)
		{
			int amount = curElement.getInteger("max") - curElement.getInteger("min");
			amount = rand.nextInt(amount + 1) + curElement.getInteger("min");
			if (amount == 0)
				amount = 1;
			
			stack = new ItemStack(Item.getByNameOrId(curElement.getString("itemId")), amount, curElement.getInteger("meta"));
			
			NBTTagCompound tag = null;
			
			if (curElement.hasKey("NBTData") && curElement.getString("NBTData") != null && curElement.getString("NBTData").length() > 0)
			{
				try {
					tag = JsonToNBT.getTagFromJson(curElement.getString("NBTData"));
				} catch (NBTException e) {
				}
				
				if (tag != null)
					stack.setTagCompound(tag);
			}
		}
		
		return stack;
	}
	
	@Override
	public void closeInventory(EntityPlayer player)
	{
        if (world == null)
        {
            return;
        }

//        RandomRestockableCrates.logger.info("Restock: closeInventory");
		if (!player.isSpectator() && this.getBlockType() instanceof BlockChest)
		{
			--this.playersUsing;
			world.addBlockEvent(this.pos, this.getBlockType(), 1, this.playersUsing);
			world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
			world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType(), false);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
//        RandomRestockableCrates.logger.warn("Restock: ReadFromNBT");
		
        NBTTagList nbttaglist = compound.getTagList("Items", 10);

		this.chestContents.clear();
		
		if (compound.hasKey("CustomName", 8))
		{
			this.customName = compound.getString("CustomName");
		}
        if (compound.hasKey("LootTable", 8))
        {
            this.lootTable = compound.getString("LootTable");
        } else {
            this.lootTable = "minecraft:chests/simple_dungeon";
        }
        if (compound.hasKey("LootTableSeed", 8))
        {
            this.lootTableSeed = compound.getLong("LootTableSeed");
        }
		ItemStackHelper.loadAllItems(compound, chestContents);
		
		this.firstTime = compound.getBoolean("first");
		this.lastOpened = compound.getLong("lastOpened");
		this.open = compound.getBoolean("open");
		this.tier = compound.getInteger("tier");

//        RandomRestockableCrates.logger.warn("Restock: ReadFromNBT: " + this.lootTable);

	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
//        RandomRestockableCrates.logger.info("Restock: writeToNBT");
		super.writeToNBT(compound);
		NBTTagList nbttaglist = new NBTTagList();
		
		ItemStackHelper.saveAllItems(compound, chestContents);
		
		compound.setTag("Items", nbttaglist);
		
		if (this.hasCustomName())
		{
			compound.setString("CustomName", this.customName);
		}
		
//        RandomRestockableCrates.logger.info("Restock: NBTTagCompund LootTable: " + this.lootTable);
        if (this.lootTable == null)
        {
			switch(this.tier)
			{
			case 1:
                this.lootTable = RRCConfig.tier2DefaultTable;
				break;
			case 2:
                this.lootTable = RRCConfig.tier3DefaultTable;
				break;
			case 3:
                this.lootTable = RRCConfig.tier4DefaultTable;
				break;
			default:
                this.lootTable = RRCConfig.tier1DefaultTable;
				break;
			}
        }

//        RandomRestockableCrates.logger.info("Restock: NBTTagCompund LootTable 2: " + this.lootTable);

    	compound.setString("LootTable", this.lootTable);
		compound.setBoolean("first", this.firstTime);
		compound.setInteger("tier", this.tier);
		compound.setLong("lastOpened", this.lastOpened);
		compound.setBoolean("open", this.open);
		
		return compound;
	}
	
	@Override
	public void updateContainingBlockInfo()
	{
//        RandomRestockableCrates.logger.info("Restock: updateContainingBlockInfo");
		super.updateContainingBlockInfo();
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return true;
	}
	
	@Override
	public int getField(int id)
	{
		return 0;
	}
	
	@Override
	public void setField(int id, int value)
	{
//        RandomRestockableCrates.logger.info("Restock: setField");
	}
	
	@Override
	public int getFieldCount()
	{
//        RandomRestockableCrates.logger.info("Restock: getFieldCount");
		return 0;
	}
	
	@Override
	public void clear()
	{
//        RandomRestockableCrates.logger.info("Restock: clear");
		chestContents.clear();
	}
	
	@Override
	public boolean receiveClientEvent(int id, int type)
	{
//        RandomRestockableCrates.logger.info("Restock: receiveClientEvent");
		if (id == 1)
		{
			this.playersUsing = type;
			return true;
		}
		else
		{
			return super.receiveClientEvent(id, type);
		}
	}
	
	@Override
	public void invalidate()
	{
//        RandomRestockableCrates.logger.info("Restock: invalidate");
		super.invalidate();
		this.updateContainingBlockInfo();
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
//        RandomRestockableCrates.logger.info("Restock: SPacketUpdateTileEntity");
        NBTTagCompound compound = new NBTTagCompound();
		
		ItemStackHelper.saveAllItems(compound, chestContents);
		
		if (this.hasCustomName())
		{
			compound.setString("CustomName", this.customName);
		}
		
//        RandomRestockableCrates.logger.info("Restock: SPacketUpdateTileEntity LootTable: " + this.lootTable);
        if (this.lootTable == null)
        {
			switch(this.tier)
			{
			case 1:
                this.lootTable = RRCConfig.tier2DefaultTable;
				break;
			case 2:
                this.lootTable = RRCConfig.tier3DefaultTable;
				break;
			case 3:
                this.lootTable = RRCConfig.tier4DefaultTable;
				break;
			default:
                this.lootTable = RRCConfig.tier1DefaultTable;
				break;
			}
        }

//        RandomRestockableCrates.logger.info("Restock: SPacketUpdateTileEntity LootTable 2: " + this.lootTable);

    	compound.setString("LootTable", this.lootTable);
		compound.setBoolean("first", this.firstTime);
		compound.setInteger("tier", this.tier);
		compound.setLong("lastOpened", this.lastOpened);
		compound.setBoolean("open", this.open);
		
		return new SPacketUpdateTileEntity(this.pos, 0, compound);
	}
	
	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt)
	{
        if (pkt.getTileEntityType() == 0)
        {
//            RandomRestockableCrates.logger.warn("Restock: onDataPacket START");

            NBTTagCompound compound = pkt.getNbtCompound();

            NBTTagList nbttaglist = compound.getTagList("Items", 10);

            this.chestContents.clear();

            if (compound.hasKey("CustomName", 8))
            {
                this.customName = compound.getString("CustomName");
            }
            if (compound.hasKey("LootTable", 8))
            {
                this.lootTable = compound.getString("LootTable");
            } else {
                this.lootTable = "minecraft:chests/simple_dungeon";
            }
            if (compound.hasKey("LootTableSeed", 8))
            {
                this.lootTableSeed = compound.getLong("LootTableSeed");
            }
	
			ItemStackHelper.loadAllItems(compound, chestContents);

            this.firstTime = compound.getBoolean("first");
            this.lastOpened = compound.getLong("lastOpened");
            this.open = compound.getBoolean("open");
            this.tier = compound.getInteger("tier");

//            RandomRestockableCrates.logger.warn("Restock: onDataPacket END: " + this.lootTable);

        }
	}
	
	protected void writeSyncableDataToNBT(NBTTagCompound syncData)
	{
//        RandomRestockableCrates.logger.info("Restock: writeSyncableDataToNBT");
        syncData.setString("LootTable", this.lootTable);
		syncData.setLong("lastOpened", this.lastOpened);
		syncData.setBoolean("open", this.open);
		syncData.setInteger("tier", this.tier);
	}
	
	protected void readSyncableDataFromNBT(NBTTagCompound syncData)
	{
//        RandomRestockableCrates.logger.info("Restock: readSyncableDataFromNBT");
        this.lootTable = syncData.getString("LootTable");
		this.lastOpened = syncData.getLong("lastOpened");
		this.open = syncData.getBoolean("open");
		this.tier = syncData.getInteger("tier");
	}

    public long getTime() {
        return world.getWorldInfo().getWorldTotalTime();
    }
    
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }
	
	private void updateMe()
	{
//        RandomRestockableCrates.logger.info("Restock: updateMe");
		this.markDirty();
		world.markBlockRangeForRenderUpdate(getPos().add(-1, -1, -1), getPos().add(1, 1, 1));
		IBlockState state = world.getBlockState(getPos());
		world.notifyBlockUpdate(getPos(), state, state, 0);
	}

}
