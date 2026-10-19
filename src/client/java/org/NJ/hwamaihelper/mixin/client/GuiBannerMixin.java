package org.NJ.hwamaihelper.mixin.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Gui.class)
public abstract class GuiBannerMixin {
 @Shadow @Final private GuiRenderState guiRenderState;
 @Inject(method="extractRenderState",at=@At("TAIL"))
 private void morisBanner(DeltaTracker delta,boolean renderLevel,boolean renderHud,CallbackInfo ci){Minecraft mc=Minecraft.getInstance();var graphics=new GuiGraphicsExtractor(mc,guiRenderState,(int)mc.mouseHandler.getScaledXPos(mc.getWindow()),(int)mc.mouseHandler.getScaledYPos(mc.getWindow()));org.NJ.hwamaihelper.client.DiscordBanner.render(graphics);if(renderHud)org.NJ.hwamaihelper.client.ContainerPreview.render(graphics);org.NJ.hwamaihelper.client.TeleportVisualEffect.render(graphics);}
}
