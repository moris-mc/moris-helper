package org.NJ.hwamaihelper.mixin.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MouseHandler.class)
public abstract class MouseBannerMixin {
 @Inject(method="onButton",at=@At("HEAD"),cancellable=true)
 private void morisBannerClick(long window,MouseButtonInfo button,int action,CallbackInfo ci){if(action!=InputConstants.PRESS||button.button()!=InputConstants.MOUSE_BUTTON_LEFT)return;Minecraft mc=Minecraft.getInstance();if(org.NJ.hwamaihelper.client.DiscordBanner.click(mc.mouseHandler.getScaledXPos(mc.getWindow()),mc.mouseHandler.getScaledYPos(mc.getWindow())))ci.cancel();}
}
