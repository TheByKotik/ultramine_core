package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenLayerHills extends GenLayer
{
	private static final Logger logger = LogManager.getLogger();
	private GenLayer field_151628_d;
	private static final String __OBFID = "CL_00000563";

	public GenLayerHills(long p_i45479_1_, GenLayer p_i45479_3_, GenLayer p_i45479_4_)
	{
		super(p_i45479_1_);
		this.parent = p_i45479_3_;
		this.field_151628_d = p_i45479_4_;
	}

	public int[] getInts(int p_75904_1_, int p_75904_2_, int p_75904_3_, int p_75904_4_)
	{
		int[] aint = this.parent.getInts(p_75904_1_ - 1, p_75904_2_ - 1, p_75904_3_ + 2, p_75904_4_ + 2);
		int[] aint1 = this.field_151628_d.getInts(p_75904_1_ - 1, p_75904_2_ - 1, p_75904_3_ + 2, p_75904_4_ + 2);
		int[] aint2 = IntCache.getIntCache(p_75904_3_ * p_75904_4_);

		for (int i1 = 0; i1 < p_75904_4_; ++i1)
		{
			for (int j1 = 0; j1 < p_75904_3_; ++j1)
			{
				this.initChunkSeed((long)(j1 + p_75904_1_), (long)(i1 + p_75904_2_));
				int k1 = aint[j1 + 1 + (i1 + 1) * (p_75904_3_ + 2)];
				int l1 = aint1[j1 + 1 + (i1 + 1) * (p_75904_3_ + 2)];
				boolean flag = (l1 - 2) % 29 == 0;

				if (k1 > 255)
				{
					logger.debug("old! " + k1);
				}

				if (k1 != 0 && l1 >= 2 && (l1 - 2) % 29 == 1 && k1 < 128)
				{
					if (BiomeGenBase.getBiome(k1 + 128) != null)
					{
						aint2[j1 + i1 * p_75904_3_] = k1 + 128;
					}
					else
					{
						aint2[j1 + i1 * p_75904_3_] = k1;
					}
				}
				else if (this.nextInt(3) != 0 && !flag)
				{
					aint2[j1 + i1 * p_75904_3_] = k1;
				}
				else
				{
					int i2 = k1;
					int j2;

					if (k1 == BiomeGenBase.desert.biomeID)
					{
						i2 = BiomeGenBase.desertHills.biomeID;
					}
					else if (k1 == BiomeGenBase.forest.biomeID)
					{
						i2 = BiomeGenBase.forestHills.biomeID;
					}
					else if (k1 == BiomeGenBase.birchForest.biomeID)
					{
						i2 = BiomeGenBase.birchForestHills.biomeID;
					}
					else if (k1 == BiomeGenBase.roofedForest.biomeID)
					{
						i2 = BiomeGenBase.plains.biomeID;
					}
					else if (k1 == BiomeGenBase.taiga.biomeID)
					{
						i2 = BiomeGenBase.taigaHills.biomeID;
					}
					else if (k1 == BiomeGenBase.megaTaiga.biomeID)
					{
						i2 = BiomeGenBase.megaTaigaHills.biomeID;
					}
					else if (k1 == BiomeGenBase.coldTaiga.biomeID)
					{
						i2 = BiomeGenBase.coldTaigaHills.biomeID;
					}
					else if (k1 == BiomeGenBase.plains.biomeID)
					{
						if (this.nextInt(3) == 0)
						{
							i2 = BiomeGenBase.forestHills.biomeID;
						}
						else
						{
							i2 = BiomeGenBase.forest.biomeID;
						}
					}
					else if (k1 == BiomeGenBase.icePlains.biomeID)
					{
						i2 = BiomeGenBase.iceMountains.biomeID;
					}
					else if (k1 == BiomeGenBase.jungle.biomeID)
					{
						i2 = BiomeGenBase.jungleHills.biomeID;
					}
					else if (k1 == BiomeGenBase.ocean.biomeID)
					{
						i2 = BiomeGenBase.deepOcean.biomeID;
					}
					else if (k1 == BiomeGenBase.extremeHills.biomeID)
					{
						i2 = BiomeGenBase.extremeHillsPlus.biomeID;
					}
					else if (k1 == BiomeGenBase.savanna.biomeID)
					{
						i2 = BiomeGenBase.savannaPlateau.biomeID;
					}
					else if (compareBiomesById(k1, BiomeGenBase.mesaPlateau_F.biomeID))
					{
						i2 = BiomeGenBase.mesa.biomeID;
					}
					else if (k1 == BiomeGenBase.deepOcean.biomeID && this.nextInt(3) == 0)
					{
						j2 = this.nextInt(2);

						if (j2 == 0)
						{
							i2 = BiomeGenBase.plains.biomeID;
						}
						else
						{
							i2 = BiomeGenBase.forest.biomeID;
						}
					}

					if (flag && i2 != k1)
					{
						if (BiomeGenBase.getBiome(i2 + 128) != null)
						{
							i2 += 128;
						}
						else
						{
							i2 = k1;
						}
					}

					if (i2 == k1)
					{
						aint2[j1 + i1 * p_75904_3_] = k1;
					}
					else
					{
						j2 = aint[j1 + 1 + (i1 + 1 - 1) * (p_75904_3_ + 2)];
						int k2 = aint[j1 + 1 + 1 + (i1 + 1) * (p_75904_3_ + 2)];
						int l2 = aint[j1 + 1 - 1 + (i1 + 1) * (p_75904_3_ + 2)];
						int i3 = aint[j1 + 1 + (i1 + 1 + 1) * (p_75904_3_ + 2)];
						int j3 = 0;

						if (compareBiomesById(j2, k1))
						{
							++j3;
						}

						if (compareBiomesById(k2, k1))
						{
							++j3;
						}

						if (compareBiomesById(l2, k1))
						{
							++j3;
						}

						if (compareBiomesById(i3, k1))
						{
							++j3;
						}

						if (j3 >= 3)
						{
							aint2[j1 + i1 * p_75904_3_] = i2;
						}
						else
						{
							aint2[j1 + i1 * p_75904_3_] = k1;
						}
					}
				}
			}
		}

		return aint2;
	}
}