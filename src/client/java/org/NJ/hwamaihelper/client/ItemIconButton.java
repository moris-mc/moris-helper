package org.NJ.hwamaihelper.client;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
/** Vanilla button sprite with an item rendered through the active resource packs. */
public final class ItemIconButton extends MouseOnlyButton {
 private final ItemStack icon;
 public ItemIconButton(int x,int y,InterfaceCommands.Entry entry,Runnable action) {
  super(x,y,24,24,Component.literal(entry.label()),b->action.run(),DEFAULT_NARRATION);
  icon=new ItemStack(entry.icon());
  String model=switch(entry.command()){case "bank"->"minecraft:5coin";case "bin"->"minecraft:bin";default->null;};
  if(model!=null && net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(net.minecraft.resources.Identifier.parse(model.replace(":",":items/")+".json")).isPresent())icon.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,net.minecraft.resources.Identifier.parse(model));
  setTooltip(Tooltip.create(Component.literal(entry.label())));
 }
 protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta) {
  extractDefaultSprite(g);
  g.item(icon,getX()+4,getY()+4);
 }
}
