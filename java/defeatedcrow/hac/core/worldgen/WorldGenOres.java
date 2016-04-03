package defeatedcrow.hac.core.worldgen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import defeatedcrow.hac.core.DCInit;
import defeatedcrow.hac.core.DCLogger;
import defeatedcrow.hac.core.util.BlockSet;
import defeatedcrow.hac.core.util.DCUtil;

public class WorldGenOres implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGen,
			IChunkProvider chunkProv) {

		int genDim1 = world.provider.getDimensionId();

		int chunk2X = chunkX << 4;
		int chunk2Z = chunkZ << 4;
		int count = 4;

		if ((genDim1 != 1 && genDim1 != -1)) {
			int[] genY = {
					10,
					30,
					90,
					160 };
			for (int i = 0; i < count; i++) {
				/* 計4回のチャンス */
				int posX = chunk2X + random.nextInt(16);
				int posY = genY[i] + random.nextInt(10 + 10 * i);
				int posZ = chunk2Z + random.nextInt(16);
				BlockPos pos = new BlockPos(posX, posY, posZ);
				BiomeGenBase biome = world.getBiomeGenForCoords(pos);

				if (posY > 160) {
					if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.MOUNTAIN))
						generateSediment(world, random, pos);
				} else if (posY > 90 && random.nextInt(2) == 0) {
					if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.MOUNTAIN))
						generateSediment(world, random, pos);
				} else if (posY < 60 && posY > 30 && random.nextInt(3) == 0) {
					if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.MOUNTAIN)
							|| BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.OCEAN)) {
						generateKieslager(world, random, pos);
					} else if (random.nextInt(4) == 0) {
						generateQuartzVine(world, random, pos);
					}
				} else if (posY < 30 && posY > 13 && random.nextInt(5) == 0) {
					generateVugs(world, random, pos);
				} else if (posY <= 13 && random.nextInt(2) == 0) {
					generateUnderlava(world, random, pos);
				}
			}
		}

	}

	static boolean isPlaceable(Block block) {
		if (block == Blocks.stone)
			return true;
		if (block == Blocks.gravel)
			return true;
		if (block == Blocks.dirt)
			return true;
		if (block == Blocks.sandstone)
			return true;
		if (block == DCInit.ores)
			return true;

		return false;
	}

	/*
	 * 堆積岩。山岳の高高度に生成する。
	 * これを集めるだけでも生きてはいけるが、銅や亜鉛はここにはない。
	 */
	public void generateSediment(World world, Random rand, BlockPos pos) {
		int h = rand.nextInt(6) + 2; // 2-7
		int r = h + 1;
		BlockSet[] gen = new BlockSet[h];
		for (int i = 0; i < h; i++) {
			if (i == 0)
				gen[i] = new BlockSet(DCInit.ores, 0);
			else if (i == h - 1)
				gen[i] = rand.nextInt(2) == 0 ? new BlockSet(DCInit.ores, 1) : new BlockSet(Blocks.coal_ore, 0);
			else {
				if (i <= h / 2) {
					gen[i] = rand.nextInt(2) == 0 ? new BlockSet(Blocks.coal_ore, 0) : new BlockSet(DCInit.ores, 0);
				} else {
					gen[i] = rand.nextInt(2) == 0 ? new BlockSet(DCInit.ores, 1) : new BlockSet(Blocks.stone, 3);
				}
			}
		}

		BlockPos min = new BlockPos(pos.add(-r, -h + 2, -r));
		BlockPos max = new BlockPos(pos.add(r, 1, r));
		Iterable<BlockPos> itr = pos.getAllInBox(min, max);
		for (BlockPos p1 : itr) {
			Block block = world.getBlockState(p1).getBlock();
			if (p1.getY() > 1 && p1.getY() < world.getActualHeight() && isPlaceable(block)) {
				int height = max.getY() - p1.getY();
				BlockSet add = gen[height];
				if (add.block == Blocks.stone) {
					int j = rand.nextInt(50);
					if (j == 0) {
						world.setBlockState(p1, DCInit.ores.getStateFromMeta(3));
					} else if (j < 10) {
						world.setBlockState(p1, DCInit.ores.getStateFromMeta(2));
					} else {
						world.setBlockState(p1, add.getState());
					}
				} else {
					world.setBlockState(p1, add.getState());
				}
			}
		}

		DCLogger.debugLog("Ore gen! Sediment:" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ", size: " + h);
	}

	/*
	 * キースラガー。山岳と海底に生成。
	 * 本来は玄武岩など塩基性寄りのはずなんだが...
	 */
	public void generateKieslager(World world, Random rand, BlockPos pos) {
		int h = rand.nextInt(5) + 3; // 3-7
		int r = h + 1;
		BlockSet[] gen = new BlockSet[h];
		for (int i = 0; i < h; i++) {
			if (i == 0 || i == h - 1)
				gen[i] = new BlockSet(Blocks.stone, 5); // andesite
			else {
				// 亜鉛 - 銅 - 鉄
				if (i >= h / 2) {
					gen[i] = rand.nextInt(1) == 0 ? new BlockSet(DCInit.ores, 6) : new BlockSet(DCInit.ores, 4);
				} else {
					gen[i] = rand.nextInt(2) == 0 ? new BlockSet(DCInit.ores, 8) : new BlockSet(DCInit.ores, 4);
				}
			}
		}

		// 柱状に生成する
		BlockPos min = new BlockPos(pos.add(0, -h + 2, -r));
		BlockPos max = new BlockPos(pos.add(h / 2, 1, r));
		if (rand.nextInt(1) == 0) {
			min = new BlockPos(pos.add(-r, -h + 2, 0));
			max = new BlockPos(pos.add(r, 1, h / 2));
		}
		Iterable<BlockPos> itr = pos.getAllInBox(min, max);
		for (BlockPos p1 : itr) {
			Block block = world.getBlockState(p1).getBlock();
			if (p1.getY() > 1 && p1.getY() < world.getActualHeight() && isPlaceable(block)) {
				int height = max.getY() - p1.getY();
				BlockSet add = gen[height];
				int j = rand.nextInt(20);
				if (j == 0) {
					world.setBlockState(p1, DCInit.ores.getStateFromMeta(7));
				} else if (j > 12) {
					world.setBlockState(p1, Blocks.stone.getStateFromMeta(5));
				} else {
					world.setBlockState(p1, add.getState());
				}
			}
		}

		DCLogger.debugLog("Ore gen! Kieslager:" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ", size: " + h);
	}

	/*
	 * 石英脈。キースラガーが生成しないバイオームに出現。
	 * サンドイッチ状の層構造とライン状の鉱脈を伴う。
	 * 出現率は低く、生成も固有鉱石はない。山がないときの救済用。
	 */
	public void generateQuartzVine(World world, Random rand, BlockPos pos) {
		int h = rand.nextInt(5) + 5; // 5-9
		int r = h + 1;
		BlockSet[] gen = new BlockSet[h];
		for (int i = 0; i < h; i++) {
			if (i == 0 || i == h - 1) {
				gen[i] = new BlockSet(Blocks.stone, 1); // granite
			} else if (i == 1 || i == h - 2) {
				gen[i] = new BlockSet(DCInit.ores, 9);
			} else {
				// 優先度は 亜鉛>鉄>銀>金
				int j = rand.nextInt(8);
				switch (j) {
				case 0:
					gen[i] = new BlockSet(DCInit.ores, 10);
					break;
				case 1:
					gen[i] = new BlockSet(DCInit.ores, 11);
					break;
				case 2:
					gen[i] = new BlockSet(DCInit.ores, 12);
					break;
				case 3:
				case 4:
					gen[i] = new BlockSet(DCInit.ores, 8);
					break;
				default:
					gen[i] = new BlockSet(DCInit.ores, 15);
					break;
				}
			}
		}

		// 円柱状に生成
		BlockPos min = new BlockPos(pos.add(-r, -h + 2, -r));
		BlockPos max = new BlockPos(pos.add(r, 1, r));
		Iterable<BlockPos> itr = pos.getAllInBox(min, max);
		for (BlockPos p1 : itr) {
			double d1 = Math.cbrt(DCUtil.getCbrtDist(p1, pos));
			if (d1 > r)
				continue;
			Block block = world.getBlockState(p1).getBlock();
			if (p1.getY() > 1 && p1.getY() < world.getActualHeight() && isPlaceable(block)) {
				int height = max.getY() - p1.getY();
				BlockSet add = gen[height];
				world.setBlockState(p1, add.getState());
			}
		}

		// 直線状の鉱脈を伴う
		for (int k = 0; k < 2; k++) {
			BlockPos line = new BlockPos(pos.add(rand.nextInt(5) - 2, rand.nextInt(5) - 2, rand.nextInt(5) - 2));
			BlockPos min2 = new BlockPos(line.north(5));
			BlockPos max2 = new BlockPos(line.south(5));
			if (rand.nextInt(1) == 0) {
				min2 = new BlockPos(line.north(5));
				max2 = new BlockPos(line.south(5));
				Iterable<BlockPos> itr2 = pos.getAllInBox(min2, max2);
				for (BlockPos p2 : itr2) {
					Block block = world.getBlockState(p2).getBlock();
					if (p2.getY() > 1 && p2.getY() < world.getActualHeight() && isPlaceable(block)) {
						int m = k == 0 ? 8 : 5;
						world.setBlockState(p2, DCInit.ores.getStateFromMeta(m));
					}
				}
			}
		}

		DCLogger.debugLog("Ore gen! Vine:" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ", size: " + h);
	}

	/*
	 * マグマ底床。マグマ帯の真下に生成する。
	 * 非常に探しづらいが、ニッケルの入手手段のひとつになる。
	 */
	public void generateUnderlava(World world, Random rand, BlockPos pos) {
		int h = rand.nextInt(2) + 2; // 2-3
		int r = h + 1;

		// 円柱状に生成
		BlockPos min = new BlockPos(pos.add(-r, -h + 2, -r));
		BlockPos max = new BlockPos(pos.add(r, 1, r));
		Iterable<BlockPos> itr = pos.getAllInBox(min, max);
		for (BlockPos p1 : itr) {
			double d1 = Math.cbrt(DCUtil.getCbrtDist(p1, pos));
			if (d1 > r)
				continue;
			Block block = world.getBlockState(p1).getBlock();
			if (p1.getY() > 1 && p1.getY() < world.getActualHeight() && isPlaceable(block)) {
				int j = rand.nextInt(5);
				if (j == 0) {
					world.setBlockState(p1, DCInit.ores.getStateFromMeta(7));
				} else if (j < 3) {
					world.setBlockState(p1, DCInit.ores.getStateFromMeta(5));
				} else {
					world.setBlockState(p1, Blocks.stone.getStateFromMeta(5));
				}
			}
		}

		DCLogger.debugLog("Ore gen! Underlava:" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ", size: " + h);
	}

	/*
	 * 晶洞。マグマの上に生成する。
	 * 球状に生成し、中央に空洞を持つ。
	 * なかなか探しにくいが、引き当てるとバニラ宝石類が大量に手に入る。
	 */
	public void generateVugs(World world, Random rand, BlockPos pos) {
		int h = rand.nextInt(4) + 4; // 4-7
		// 球状に生成
		BlockPos min = new BlockPos(pos.add(-h, -h, -h));
		BlockPos max = new BlockPos(pos.add(h, h, h));
		Iterable<BlockPos> itr = pos.getAllInBox(min, max);
		for (BlockPos p1 : itr) {
			double d1 = Math.cbrt(DCUtil.getCbrtDist(p1, pos));
			int r = h - MathHelper.floor_double(d1);
			if (r < -0.0D)
				continue;
			Block block = world.getBlockState(p1).getBlock();
			if (p1.getY() > 1 && p1.getY() < world.getActualHeight() && isPlaceable(block)) {
				if (r < 2.0D) {
					world.setBlockState(p1, Blocks.stone.getStateFromMeta(1));
				} else if (r < 4.0D) {
					world.setBlockState(p1, DCInit.ores.getStateFromMeta(9));
				} else if (r < 5.0D) {
					int j = rand.nextInt(10);
					switch (j) {
					case 0:
						world.setBlockState(p1, DCInit.ores.getStateFromMeta(12));
						break;
					case 1:
						world.setBlockState(p1, DCInit.ores.getStateFromMeta(13));
						break;
					case 2:
						world.setBlockState(p1, DCInit.ores.getStateFromMeta(14));
						break;
					case 3:
					case 4:
						world.setBlockState(p1, DCInit.ores.getStateFromMeta(10));
						break;
					case 5:
					case 6:
						world.setBlockToAir(p1);
						break;
					default:
						world.setBlockState(p1, DCInit.ores.getStateFromMeta(9));
					}
				} else {
					world.setBlockToAir(p1);
				}
			}
		}

		DCLogger.debugLog("Ore gen! Vugs:" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ", size: " + h);
	}

}
