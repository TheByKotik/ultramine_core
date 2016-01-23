package org.ultramine.commands.basic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraftforge.common.util.FakePlayer;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.commands.OfflinePlayer;

public class OpenInvCommands
{
	@Command(
			name = "openinv",
			group = "basic",
			permissions = {"command.basic.openinv"},
			syntax = {"<player>"}
	)
	public static void openinv(final CommandContext ctx)
	{
		final EntityPlayerMP sender = ctx.getSenderAsPlayer();
		final OfflinePlayer offline = ctx.get("player").asOfflinePlayer();
		offline.loadPlayer(player -> {
			if(player.isDead)
				ctx.sendMessage("command.openinv.fail.dead");
			else
				sender.displayGUIChest(new PlayerWrappedInventory(ctx.getServer(), player.inventory, player instanceof FakePlayer ? offline : null));
		});
	}
	
	@Command(
			name = "openender",
			group = "basic",
			permissions = {"command.basic.openender"},
			syntax = {"<player>"}
	)
	public static void openender(final CommandContext ctx)
	{
		final EntityPlayerMP sender = ctx.getSenderAsPlayer();
		final OfflinePlayer offline = ctx.get("player").asOfflinePlayer();
		offline.loadPlayer(player -> {
			InventoryEnderChest inv = player.getInventoryEnderChest();
			WrappedTileEntityEnderChest tile = new WrappedTileEntityEnderChest(sender, player, player instanceof FakePlayer ? offline : null);
	        inv.func_146031_a(tile);
	        if(player instanceof FakePlayer)
	        	inv.func_110134_a(tile);
			
			sender.displayGUIChest(inv);
		});
	}

	private static class PlayerWrappedInventory implements IInventory
	{
		private final MinecraftServer server;
		
		private InventoryPlayer inv;
		private OfflinePlayer offline;
		
		public PlayerWrappedInventory(MinecraftServer server, InventoryPlayer player, OfflinePlayer offline)
		{
			this.server = server;
			this.inv = player;
			this.offline = offline;
		}

		@Override
		public int getSizeInventory()
		{
			return 45;
		}

		@Override
		public ItemStack getStackInSlot(int var1)
		{
			if(var1 < inv.getSizeInventory())
			{
				return inv.getStackInSlot(var1);
			}
			
			return null;
		}

		@Override
		public ItemStack decrStackSize(int var1, int var2)
		{
			if(var1 < inv.getSizeInventory())
			{
				return inv.decrStackSize(var1, var2);
			}
			
			return null;
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int var1)
		{
			if(var1 < inv.getSizeInventory())
			{
				return inv.getStackInSlotOnClosing(var1);
			}
			
			return null;
		}

		@Override
		public void setInventorySlotContents(int var1, ItemStack var2)
		{
			if(var1 < inv.getSizeInventory())
			{
				inv.setInventorySlotContents(var1, var2);
			}
		}

		@Override
		public String getInventoryName()
		{
			return inv.getInventoryName();
		}

		@Override
		public int getInventoryStackLimit()
		{
			return inv.getInventoryStackLimit();
		}
		
		@Override
		public void markDirty() 
		{
			inv.markDirty();
			if(offline != null)
			{
				EntityPlayerMP p = offline.getIfOnline();
				if(p != null)
				{
					inv = p.inventory;
					offline = null;
				}
			}
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer var1)
		{
			return offline != null || !inv.player.isDead;
		}

		@Override
		public void openInventory(){}

		@Override
		public void closeInventory()
		{
			if(offline != null && offline.getIfOnline() == null)
				offline.saveFakePlayer((EntityPlayerMP)inv.player);
		}

		@Override
		public boolean hasCustomInventoryName()
		{
			return inv.hasCustomInventoryName();
		}

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return inv.isItemValidForSlot(i, itemstack);
		}
	}
	
	private static class WrappedTileEntityEnderChest extends TileEntityEnderChest implements IInvBasic
	{
		private EntityPlayer opener;
		private EntityPlayer player;
		private OfflinePlayer offline;
		
		public WrappedTileEntityEnderChest(EntityPlayer opener, EntityPlayer player, OfflinePlayer offline)
		{
			this.opener = opener;
			this.player = player;
			this.offline = offline;
		}
		
		@Override public void updateEntity(){}
		@Override public boolean receiveClientEvent(int par1, int par2){return false;}
		@Override public void invalidate(){}
		@Override public void func_145969_a(){}
		@Override public boolean func_145971_a(EntityPlayer par1EntityPlayer){return true;}

		@Override
		public void onInventoryChanged(InventoryBasic var1)
		{
			if(offline != null)
			{
				EntityPlayer p = offline.getIfOnline();
				if(p != null)
					opener.closeScreen();
			}
		}
		
		@Override
		public void func_145970_b()
		{
			if(offline != null)
			{
				offline.saveFakePlayer((EntityPlayerMP)player);
			}
		}
	}
}
