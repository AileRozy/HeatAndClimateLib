package defeatedcrow.hac.core.plugin;

import java.util.ArrayList;
import java.util.List;

import defeatedcrow.hac.api.climate.DCAirflow;
import defeatedcrow.hac.api.climate.DCHeatTier;
import defeatedcrow.hac.api.climate.DCHumidity;
import defeatedcrow.hac.api.cultivate.IClimateCrop;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class ClimateCropWrapper implements IRecipeWrapper {

	private final List<ItemStack> input;
	private final List<ItemStack> output;
	private final IClimateCrop rec;
	private final Block block;

	@SuppressWarnings("unchecked")
	public ClimateCropWrapper(IClimateCrop recipe) {
		rec = recipe;
		input = new ArrayList<ItemStack>();
		output = new ArrayList<ItemStack>();
		if (recipe instanceof Block && recipe.getGrownState() != null) {
			block = (Block) recipe;
			IBlockState grown = recipe.getGrownState();
			output.addAll(recipe.getCropItems(grown, 0));
			input.add(new ItemStack(block, 1, block.getMetaFromState(grown)));
			input.add(rec.getSeedItem(grown));
		} else {
			block = null;
		}
	}

	@Override
	public void getIngredients(IIngredients ing) {
		ing.setInputs(ItemStack.class, input);
		ing.setOutputs(ItemStack.class, output);
	}

	@Override
	public List getInputs() {
		return input;
	}

	@Override
	public List getOutputs() {
		return output;
	}

	@Override
	public List<FluidStack> getFluidInputs() {
		return null;
	}

	@Override
	public List<FluidStack> getFluidOutputs() {
		return null;
	}

	@Override
	public void drawInfo(Minecraft mc, int wid, int hei, int mouseX, int mouseY) {
		List<DCHeatTier> heats = rec.getSuitableTemp(block.getDefaultState());
		List<DCHumidity> hums = rec.getSuitableHum(block.getDefaultState());
		List<DCAirflow> airs = rec.getSuitableAir(block.getDefaultState());
		DCHeatTier minT = DCHeatTier.INFERNO;
		DCHumidity maxH = DCHumidity.DRY;
		DCAirflow maxA = DCAirflow.TIGHT;
		int baseY = 75;

		ResourceLocation res = new ResourceLocation("dcs_climate", "textures/gui/c_crops_gui_jei.png");
		mc.getTextureManager().bindTexture(res);
		if (heats.isEmpty()) {
			mc.currentScreen.drawTexturedModalRect(44, baseY, 0, 170, 72, 3);
			minT = DCHeatTier.NORMAL;
		} else {
			for (DCHeatTier h : heats) {
				mc.currentScreen.drawTexturedModalRect(44 + h.getID() * 6, baseY, h.getID() * 6, 170, 6, 3);
				if (h.getID() < minT.getID())
					minT = h;
			}
		}
		if (hums.isEmpty()) {
			mc.currentScreen.drawTexturedModalRect(44, baseY + 10, 0, 174, 72, 3);
			maxH = DCHumidity.NORMAL;
		} else {
			for (DCHumidity h : hums) {
				mc.currentScreen.drawTexturedModalRect(44 + h.getID() * 18, baseY + 10, h.getID() * 18, 174, 18, 3);
				if (maxH.getID() < h.getID())
					maxH = h;
			}
		}
		if (airs.isEmpty()) {
			mc.currentScreen.drawTexturedModalRect(44, baseY + 20, 0, 178, 72, 3);
			maxA = DCAirflow.NORMAL;
		} else {
			for (DCAirflow a : airs) {
				mc.currentScreen.drawTexturedModalRect(44 + a.getID() * 18, baseY + 20, a.getID() * 18, 178, 18, 3);
				if (maxA.getID() < a.getID())
					maxA = a;
			}
		}

		String name = "PLANT BLOCK";
		String crop = "CROPS";
		String seed = "SEEDS";
		mc.fontRendererObj.drawString(name, 26, 20, 0x0099FF, true);
		mc.fontRendererObj.drawString(crop, 80, 14, 0x0099FF, true);
		mc.fontRendererObj.drawString(seed, 80, 42, 0x0099FF, true);
	}

	@Override
	public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight) {

	}

	@Override
	public List<String> getTooltipStrings(int x, int y) {
		int baseY = 72;
		List<String> s = new ArrayList<String>();
		if (y > baseY && y < baseY + 8) {
			if (x > 44 && x < 116) {
				int i = (x - 44) / 6;
				s.add(DCHeatTier.getTypeByID(i).name() + " " + DCHeatTier.getTypeByID(i).getTemp());
			}
		}
		if (y > baseY + 10 && y < baseY + 18) {
			if (x > 44 && x < 116) {
				int i = (x - 44) / 18;
				s.add(DCHumidity.getTypeByID(i).name());
			}
		}
		if (y > baseY + 20 && y < baseY + 28) {
			if (x > 44 && x < 116) {
				int i = (x - 44) / 18;
				s.add(DCAirflow.getTypeByID(i).name());
			}
		}
		// if (s.isEmpty())
		// s.add(x + ", " + y);

		return s.isEmpty() ? null : s;
	}

	@Override
	public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		return false;
	}

}
