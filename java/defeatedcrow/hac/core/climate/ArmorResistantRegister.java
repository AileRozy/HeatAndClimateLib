package defeatedcrow.hac.core.climate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import defeatedcrow.hac.api.climate.ItemSet;
import defeatedcrow.hac.api.damage.IArmorItemRegister;
import defeatedcrow.hac.core.DCLogger;
import defeatedcrow.hac.core.util.DCUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class ArmorResistantRegister implements IArmorItemRegister {

	public static final HashMap<ItemSet, Float> heatMap = new HashMap<ItemSet, Float>();

	public static final HashMap<ItemSet, Float> coldMap = new HashMap<ItemSet, Float>();

	private ArmorResistantRegister() {}

	public static final ArmorResistantRegister INSTANCE = new ArmorResistantRegister();

	@Override
	public HashMap<ItemSet, Float> getHeatMap() {
		return heatMap;
	}

	@Override
	public HashMap<ItemSet, Float> getColdMap() {
		return coldMap;
	}

	@Override
	public void registerMaterial(ItemStack item, float heat, float cold) {
		if (DCUtil.isEmpty(item))
			return;
		ItemSet set = new ItemSet(item.getItem(), item.getItemDamage());
		if (!heatMap.containsKey(set) && !coldMap.containsKey(set)) {
			heatMap.put(set, heat);
			coldMap.put(set, cold);
			DCLogger.debugLog("register armor material: " + item.getDisplayName() + " heat " + heat + "/cold " + cold);
			String mapName = item.getItem().getRegistryName().toString() + ":" + item.getItemDamage();
			Map<String, Float> map = Maps.newHashMap();
			map.put("heat", heat);
			map.put("cold", cold);
			floatMap.put(mapName, map);
		}
	}

	@Override
	public float getHeatPreventAmount(ItemStack item) {
		if (DCUtil.isEmpty(item))
			return 0;
		ItemSet set = new ItemSet(item.getItem(), item.getItemDamage());
		ItemSet wildcard = new ItemSet(item.getItem(), OreDictionary.WILDCARD_VALUE);
		if (heatMap.containsKey(set)) {
			float ret = heatMap.get(set);
			return ret;
		} else if (heatMap.containsKey(wildcard)) {
			float ret = heatMap.get(wildcard);
			return ret;
		}
		return 0F;
	}

	@Override
	public float getColdPreventAmount(ItemStack item) {
		if (DCUtil.isEmpty(item))
			return 0;
		ItemSet set = new ItemSet(item.getItem(), item.getItemDamage());
		ItemSet wildcard = new ItemSet(item.getItem(), OreDictionary.WILDCARD_VALUE);
		if (coldMap.containsKey(set)) {
			float ret = coldMap.get(set);
			return ret;
		} else if (coldMap.containsKey(wildcard)) {
			float ret = coldMap.get(wildcard);
			return ret;
		}
		return 0F;
	}

	public void registerArmorResistant(String name, float heat, float cold) {
		if (name != null) {
			String itemName = name;
			String modid = "minecraft";
			int meta = 32767;
			if (name.contains(":")) {
				String[] n2 = name.split(":");
				if (n2 != null && n2.length > 0) {
					if (n2.length == 1) {
						itemName = n2[0];
					} else {
						modid = n2[0];
						itemName = n2[1];
						if (n2.length > 2) {
							Integer m = Integer.parseInt(n2[2]);
							if (m != null && m >= 0) {
								meta = m;
							}
						}
					}

				} else {
					DCLogger.debugLog("fail to register target item from json: " + name);
					return;
				}
			}

			Item item = Item.REGISTRY.getObject(new ResourceLocation(modid, itemName));
			if (item != null) {
				DCLogger.debugLog("register target item from json: " + modid + ":" + itemName + ", " + meta);
				ItemSet set = new ItemSet(item, meta);
				INSTANCE.registerMaterial(set.getSingleStack(), heat, cold);
			}
		}
	}

	/* json */
	private static Map<String, Object> jsonMap = new HashMap<String, Object>();
	protected static Map<String, Map<String, Float>> floatMap = new HashMap<String, Map<String, Float>>();

	private static File dir = null;

	public static void startMap() {
		if (!jsonMap.isEmpty()) {
			for (Entry<String, Object> ent : jsonMap.entrySet()) {
				if (ent != null) {
					String name = ent.getKey();
					Object value = ent.getValue();
					float heat = 0;
					float cold = 0;
					if (value instanceof Map) {
						String h = ((Map) value).get("heat").toString();
						heat = Float.parseFloat(h);
						String c = ((Map) value).get("cold").toString();
						cold = Float.parseFloat(c);
					}
					INSTANCE.registerArmorResistant(name, heat, cold);
				}
			}
		} else {
			DCLogger.debugLog("no item resistant json data.");
		}
	}

	public static void pre() {
		if (dir != null) {
			try {
				if (!dir.exists() && !dir.createNewFile()) {
					return;
				}

				if (dir.canRead()) {
					FileInputStream fis = new FileInputStream(dir.getPath());
					InputStreamReader isr = new InputStreamReader(fis);
					JsonReader jsr = new JsonReader(isr);
					Gson gson = new Gson();
					Map get = gson.fromJson(jsr, Map.class);

					isr.close();
					fis.close();
					jsr.close();

					if (get != null && !get.isEmpty()) {
						jsonMap.putAll(get);

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		startMap();
	}

	// 生成は初回のみ
	public static void post() {

		if (dir != null) {
			try {
				if (!dir.exists() && !dir.createNewFile()) {
					return;
				} else if (!jsonMap.isEmpty()) {
					DCLogger.infoLog("item resistant data json is already exists.");
					return;
				}

				if (dir.canWrite()) {
					FileOutputStream fos = new FileOutputStream(dir.getPath());
					OutputStreamWriter osw = new OutputStreamWriter(fos);
					JsonWriter jsw = new JsonWriter(osw);
					jsw.setIndent(" ");
					Gson gson = new Gson();
					gson.toJson(floatMap, Map.class, jsw);

					osw.close();
					fos.close();
					jsw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class FloatSet {
		final float resistant;

		private FloatSet(float f1) {
			resistant = f1;
		}
	}

	public static void setDir(File file) {
		dir = new File(file, "defeatedcrow/climate/armor_item_resistant.json");
		if (dir.getParentFile() != null) {
			dir.getParentFile().mkdirs();
		}
	}
}
