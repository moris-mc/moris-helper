package org.NJ.hwamaihelper.mixin.client;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MouseHandler.class)
public abstract class TeleportMouseMixin {
 @Inject(method="turnPlayer",at=@At("HEAD"),cancellable=true) private void morisCamera(double dt,CallbackInfo ci){if(org.NJ.hwamaihelper.client.TeleportVisualEffect.active())ci.cancel();}
}
