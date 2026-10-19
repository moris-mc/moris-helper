package org.NJ.hwamaihelper.client;
import java.util.List;
import net.minecraft.world.item.*;
/** Server commands fixed by the server owner; never loaded from user config. */
public final class InterfaceCommands {
 public record Entry(String label,String command,Item icon) {}
 public static final List<Entry> ENTRIES=List.of(
  new Entry("合成台","chmc 開啟介面 工作臺",Items.CRAFTING_TABLE),
  new Entry("切石機","chmc 開啟介面 切石機",Items.STONECUTTER),
  new Entry("製圖台","chmc 開啟介面 製圖台",Items.CARTOGRAPHY_TABLE),
  new Entry("織布機","chmc 開啟介面 織布機",Items.LOOM),
  new Entry("鍛造台","chmc 開啟介面 鍛造台",Items.SMITHING_TABLE),
  new Entry("砂輪","chmc 開啟介面 砂輪",Items.GRINDSTONE),
  new Entry("鐵砧","chmc 開啟介面 鐵砧",Items.ANVIL),
  new Entry("垃圾桶","bin",Items.CACTUS),
  new Entry("終界箱","ec",Items.ENDER_CHEST),
  new Entry("隨身背包","bp",Items.BUNDLE),
  new Entry("銀行","bank",Items.GOLD_INGOT),
  new Entry("附魔卸除","附魔卸除",Items.ENCHANTED_BOOK),
  new Entry("金錢排行榜","baltop",Items.EMERALD)
 );
 private InterfaceCommands() {}
}