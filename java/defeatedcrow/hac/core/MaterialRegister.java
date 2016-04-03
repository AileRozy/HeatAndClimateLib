package defeatedcrow.hac.core;

import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.registry.GameRegistry;
import defeatedcrow.hac.core.base.DCItemBlock;
import defeatedcrow.hac.core.block.smelting.BlockOres;
import defeatedcrow.hac.core.item.ItemClimateChecker;
import defeatedcrow.hac.machine.common.BlockStoveFuel;

public class MaterialRegister {
	private MaterialRegister() {
	}

	public static void load() {
		registerBlock();
		registerItem();
		registerHarvestLevel();
	}

	static void registerBlock() {
		DCInit.stove_fuel = new BlockStoveFuel(Material.clay, ClimateCore.PACKAGE_BASE + "_gen_stovefuel", 0);
		GameRegistry.registerBlock(DCInit.stove_fuel, DCItemBlock.class, ClimateCore.PACKAGE_BASE + "_gen_stovefuel");

		DCInit.ores = new BlockOres(Material.iron, ClimateCore.PACKAGE_BASE + "_ore_stone", 15);
		GameRegistry.registerBlock(DCInit.ores, DCItemBlock.class, ClimateCore.PACKAGE_BASE + "_ore_stone");
	}

	static void registerItem() {
		DCInit.climate_checker = new ItemClimateChecker().setCreativeTab(ClimateCore.climate).setUnlocalizedName(
				ClimateCore.PACKAGE_BASE + "_checker");
		GameRegistry.registerItem(DCInit.climate_checker, ClimateCore.PACKAGE_BASE + "_checker");
	}

	private static void registerHarvestLevel() {
		DCInit.ores.setHarvestLevel("pickaxe", 0);
	}
}