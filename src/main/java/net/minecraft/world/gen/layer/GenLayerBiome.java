package net.minecraft.world.gen.layer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.WeightedRandom;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;

public class GenLayerBiome extends GenLayer
{
	private List<BiomeEntry> desertBiomes = new ArrayList<BiomeEntry>();
	private List<BiomeEntry> warmBiomes = new ArrayList<BiomeEntry>();
	private List<BiomeEntry> coolBiomes = new ArrayList<BiomeEntry>();
	private List<BiomeEntry> icyBiomes = new ArrayList<BiomeEntry>();
	
	private static final String __OBFID = "CL_00000555";

	public GenLayerBiome(long par1, GenLayer par3GenLayer, WorldType par4WorldType)
	{
		super(par1);
		
		this.parent = par3GenLayer;
		
		this.desertBiomes.addAll(BiomeManager.desertBiomes);
		this.warmBiomes.addAll(BiomeManager.warmBiomes);
		this.coolBiomes.addAll(BiomeManager.coolBiomes);
		this.icyBiomes.addAll(BiomeManager.icyBiomes);
		
		if (par4WorldType == WorldType.DEFAULT_1_1)
		{
			desertBiomes.add(new BiomeEntry(BiomeGenBase.desert, 10));
			desertBiomes.add(new BiomeEntry(BiomeGenBase.forest, 10));
			desertBiomes.add(new BiomeEntry(BiomeGenBase.extremeHills, 10));
			desertBiomes.add(new BiomeEntry(BiomeGenBase.swampland, 10));
			desertBiomes.add(new BiomeEntry(BiomeGenBase.plains, 10));
			desertBiomes.add(new BiomeEntry(BiomeGenBase.taiga, 10));
		}
		else
		{
			desertBiomes.add(new BiomeEntry(BiomeGenBase.desert, 30));
			desertBiomes.add(new BiomeEntry(BiomeGenBase.savanna, 20));
			desertBiomes.add(new BiomeEntry(BiomeGenBase.plains, 10));
		}
	}

	public int[] getInts(int par1, int par2, int par3, int par4)
	{
		int[] aint = this.parent.getInts(par1, par2, par3, par4);
		int[] aint1 = IntCache.getIntCache(par3 * par4);

		for (int i1 = 0; i1 < par4; ++i1)
		{
			for (int j1 = 0; j1 < par3; ++j1)
			{
				this.initChunkSeed((long)(j1 + par1), (long)(i1 + par2));
				int k1 = aint[j1 + i1 * par3];
				int l1 = (k1 & 3840) >> 8;
				k1 &= -3841;

				if (isBiomeOceanic(k1))
				{
					aint1[j1 + i1 * par3] = k1;
				}
				else if (k1 == BiomeGenBase.mushroomIsland.biomeID)
				{
					aint1[j1 + i1 * par3] = k1;
				}
				else if (k1 == 1)
				{
					if (l1 > 0)
					{
						if (this.nextInt(3) == 0)
						{
							aint1[j1 + i1 * par3] = BiomeGenBase.mesaPlateau.biomeID;
						}
						else
						{
							aint1[j1 + i1 * par3] = BiomeGenBase.mesaPlateau_F.biomeID;
						}
					}
					else
					{
						aint1[j1 + i1 * par3] = ((BiomeEntry)WeightedRandom.getItem(this.desertBiomes, (int)(this.nextLong(WeightedRandom.getTotalWeight(this.desertBiomes) / 10) * 10))).biome.biomeID;
					}
				}
				else if (k1 == 2)
				{
					if (l1 > 0)
					{
						aint1[j1 + i1 * par3] = BiomeGenBase.jungle.biomeID;
					}
					else
					{
						aint1[j1 + i1 * par3] = ((BiomeEntry)WeightedRandom.getItem(this.warmBiomes, (int)(this.nextLong(WeightedRandom.getTotalWeight(this.warmBiomes) / 10) * 10))).biome.biomeID;
					}
				}
				else if (k1 == 3)
				{
					if (l1 > 0)
					{
						aint1[j1 + i1 * par3] = BiomeGenBase.megaTaiga.biomeID;
					}
					else
					{
						aint1[j1 + i1 * par3] = ((BiomeEntry)WeightedRandom.getItem(this.coolBiomes, (int)(this.nextLong(WeightedRandom.getTotalWeight(this.coolBiomes) / 10) * 10))).biome.biomeID;
					}
				}
				else if (k1 == 4)
				{
					aint1[j1 + i1 * par3] = ((BiomeEntry)WeightedRandom.getItem(this.icyBiomes, (int)(this.nextLong(WeightedRandom.getTotalWeight(this.icyBiomes) / 10) * 10))).biome.biomeID;
				}
				else
				{
					aint1[j1 + i1 * par3] = BiomeGenBase.mushroomIsland.biomeID;
				}
			}
		}

		return aint1;
	}
}