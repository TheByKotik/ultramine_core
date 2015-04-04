package org.ultramine.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeCache
{
	private final List<IRecipe> originList;
	private final Map<RecipeKey, IRecipe> cache = new HashMap<RecipeKey, IRecipe>();
	private final Set<RecipeKey> noRecipeSet = new HashSet<RecipeKey>();
	
	@SuppressWarnings("unchecked")
	public RecipeCache()
	{
		originList = CraftingManager.getInstance().getRecipeList();
	}
	
	public IRecipe findRecipe(InventoryCrafting inv, World world)
	{
		RecipeKey key = new RecipeKeyBuilder(inv).build();
		if(key.width == 0)
			return null;
		
		IRecipe rcp = cache.get(key);
		if (rcp != null && rcp.matches(inv, world))
		{
			return rcp;
		}
		else if(noRecipeSet.contains(key))
		{
			return null;
		}
		else
		{
			for(IRecipe recipe : originList)
			{
				if (recipe.matches(inv, world))
				{
					addToCache(key, recipe);
					return recipe;
				}
			}
		}
		
		if(noRecipeSet.size() >= 1048576)
			noRecipeSet.clear();
		noRecipeSet.add(key);
		return null;
	}
	
	private void addToCache(RecipeKey key, IRecipe recipe)
	{
		if(cache.size() >= 1048576)
			cache.clear();
		cache.put(key, recipe);
	}
	
	public void clearCache()
	{
		cache.clear();
		noRecipeSet.clear();
	}
	
	private static class RecipeKey implements Comparable<RecipeKey>
	{
		private final int[] contents;
		private final int width;
		
		public RecipeKey(int[] contents, int width)
		{
			this.contents = contents;
			this.width = width;
		}
		
		public boolean equals(Object o)
		{
			if(!(o instanceof RecipeKey))
				return false;
			RecipeKey rk = (RecipeKey)o;
			return width == rk.width && Arrays.equals(contents, rk.contents);
		}
		
		public int hashCode()
		{
			int hash = 0;
			for(int i = 0; i < contents.length; i++)
				hash ^= contents[i];
			return hash;
		}
		
		public int compareTo(RecipeKey rk)
		{
			int c1 = width - rk.width;
			if(c1 != 0)
				return c1;
			int c2 = contents.length - rk.contents.length;
			if(c2 != 0)
				return c2;
			for(int i = 0; i < contents.length; i++)
			{
				int c3 = Integer.compare(contents[i], rk.contents[i]);
				if(c3 != 0)
					return c3;
			}
			
			return 0;
		}
	}
	
	private static class RecipeKeyBuilder
	{
		private int[] contents;
		private int x;
		private int y;
		private int width;
		private int height;
		private int newWidth;
		private int newHeight;
		
		public RecipeKeyBuilder(InventoryCrafting inv)
		{
			contents = new int[inv.getSizeInventory()];
			for(int i = 0; i < contents.length; i++)
			{
				ItemStack is = inv.getStackInSlot(i);
				if(is != null)
					contents[i] = (Item.getIdFromItem(is.getItem()) << 16) | (is.getItemDamage() & 0xFFFF);
			}
			newWidth = width = inv.getWidth();
			newHeight = height = contents.length/width;
			
			while(trimHorisontal(false));
			if(y == height)
			{
				contents = new int[0];
				newWidth = 0;
			}
			else
			{
				while(trimHorisontal(true));
				while(trimVertical(false));
				while(trimVertical(true));
				if(width != newWidth || height != newHeight)
				{
					int[] newContents = new int[newWidth*newHeight];
					for(int i = 0; i < newWidth; i++)
						for(int j = 0; j < newHeight; j++)
							newContents[i + j*newWidth] = contents[(x+i) + (y+j)*width];
					contents = newContents;
				}
			}
		}
		
		private boolean trimHorisontal(boolean bottom)
		{
			boolean empty = true;
			for(int i = 0; i < width; i++)
			{
				if(contents[bottom ? (y+newHeight-1)*width + i : y*width + i] != 0)
				{
					empty = false;
					break;
				}
			}
			if(empty)
			{
				newHeight--;
				if(!bottom)
					y++;
				return newHeight != 0;
			}
			
			return false;
		}
		
		private boolean trimVertical(boolean right)
		{
			boolean empty = true;
			for(int i = 0; i < newHeight; i++)
			{
				if(contents[y*width + i*(width) + x + (right ? newWidth-1 : 0)] != 0)
				{
					empty = false;
					break;
				}
			}
			if(empty)
			{
				newWidth--;
				if(!right)
					x++;
				return true;
			}
			
			return false;
		}
		
		public RecipeKey build()
		{
			return new RecipeKey(contents, newWidth);
		}
	}
}
