package zairus.randomrestockablecrates.tileentity;

import java.util.Random;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import zairus.randomrestockablecrates.RandomRestockableCrates;
import zairus.randomrestockablecrates.RRCConfig;
import zairus.randomrestockablecrates.inventory.ContainerCrate;
import zairus.randomrestockablecrates.sound.RRCSoundEvents;

public class TileEntityCrate extends TileEntityLockable implements ITickable, IInventory
{
	public int playersUsing;

	private ItemStack[] chestContents = new ItemStack[27];
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
		return new ContainerCrate(playerInventory, this, player);
	}
	
	@Override
	public String getGuiID()
	{
		return "randomrestockablecrates:crate";
	}
	
	@Override
	public int getSizeInventory()
	{
		return 27;
	}
	
	@Override
	public ItemStack getStackInSlot(int index)
	{
		return this.chestContents[index];
	}
	
	@Override
	public ItemStack decrStackSize(int index, int count)
	{
        // RandomRestockableCrates.logger.info("Restock: decrStackSize");
		if (this.chestContents[index] != null)
		{
			if (this.chestContents[index].stackSize <= count)
			{
				ItemStack itemstack1 = this.chestContents[index];
                this.chestContents[index] = null;
                this.markDirty();
                return itemstack1;
			}
			else
			{
				ItemStack itemstack = this.chestContents[index].splitStack(count);
				
                if (this.chestContents[index].stackSize == 0)
                {
                    this.chestContents[index] = null;
                }
                
                this.markDirty();
                return itemstack;
			}
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public ItemStack removeStackFromSlot(int index)
	{
        // RandomRestockableCrates.logger.info("Restock: removeStackFromSlot");
		if (this.chestContents[index] != null)
        {
            ItemStack itemstack = this.chestContents[index];
            this.chestContents[index] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
	}
	
	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
        // RandomRestockableCrates.logger.info("Restock: setInventorySlotContents");
		this.chestContents[index] = stack;
		
        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }
        
        this.markDirty();
	}
	
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return true;
	}
	
	@Override
	public void openInventory(EntityPlayer player)
	{

//        RandomRestockableCrates.logger.info("Restock: openInventory top worldObj: " + (worldObj != null));
//        RandomRestockableCrates.logger.info("Restock: openInventory top this_worldObj: " + (this.worldObj != null));

        if (this.worldObj.isRemote)
        {
//            RandomRestockableCrates.logger.info("Restock: openInventory This is remote: " + (this.worldObj.isRemote));
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

//                    RandomRestockableCrates.logger.info("Restock: openInventory restock");
                    restock(this.worldObj.rand);
                }

//                updateMe();
            }

			this.worldObj.addBlockEvent(this.pos, this.getBlockType(), 1, this.playersUsing);
			this.worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
			this.worldObj.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType());
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
        if (worldObj != null && !this.worldObj.isRemote)
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
                    
//  				this.worldObj.playSound((EntityPlayer)null, pos, RRCSoundEvents.CRATE_OPEN, SoundCategory.BLOCKS, 1.0F, 1.2F / (this.worldObj.rand.nextFloat() * 0.2f + 0.9f));
                    
                    updateMe();

                    // restock(this.worldObj.rand);
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
            if (this.worldObj.getLootTableManager() == null)
            {
//                RandomRestockableCrates.logger.info("Could not get loot manager.");
                return;
            }
            
            LootTable table = this.worldObj.getLootTableManager().getLootTableFromLocation(this.lootTableLocation);
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
            
            LootContext.Builder lootBuilder = new LootContext.Builder((WorldServer)this.worldObj);

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
		ItemStack stack = null;
		
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
        if (this.worldObj == null)
        {
            return;
        }

//        RandomRestockableCrates.logger.info("Restock: closeInventory");
		if (!player.isSpectator() && this.getBlockType() instanceof BlockChest)
		{
			--this.playersUsing;
			this.worldObj.addBlockEvent(this.pos, this.getBlockType(), 1, this.playersUsing);
			this.worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
			this.worldObj.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType());
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
//        RandomRestockableCrates.logger.warn("Restock: ReadFromNBT");
		
        NBTTagList nbttaglist = compound.getTagList("Items", 10);

		this.chestContents = new ItemStack[this.getSizeInventory()];
		
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
		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;
			
			if (j >= 0 && j < this.chestContents.length)
			{
				this.chestContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}
		
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
		
		for (int i = 0; i < this.chestContents.length; ++i)
		{
			if (this.chestContents[i] != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte)i);
				this.chestContents[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}
		
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
		for (int i = 0; i < this.chestContents.length; ++i)
		{
			this.chestContents[i] = null;
		}
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

		NBTTagList nbttaglist = new NBTTagList();
		
		for (int i = 0; i < this.chestContents.length; ++i)
		{
			if (this.chestContents[i] != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte)i);
				this.chestContents[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}
		
		compound.setTag("Items", nbttaglist);
		
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

            this.chestContents = new ItemStack[this.getSizeInventory()];

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
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;

                if (j >= 0 && j < this.chestContents.length)
                {
                    this.chestContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
                }
            }

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
        return this.worldObj.getWorldInfo().getWorldTotalTime();
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
		this.worldObj.markBlockRangeForRenderUpdate(getPos().add(-1, -1, -1), getPos().add(1, 1, 1));
		IBlockState state = this.worldObj.getBlockState(getPos());
		this.worldObj.notifyBlockUpdate(getPos(), state, state, 0);
	}

}
