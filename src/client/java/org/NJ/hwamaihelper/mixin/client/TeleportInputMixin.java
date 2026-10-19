package org.NJ.hwamaihelper.mixin.client;
import net.minecraft.client.player.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KeyboardInput.class)
public abstract class TeleportInputMixin extends ClientInput {
 @Inject(method="tick",at=@At("TAIL")) private void morisFreeze(CallbackInfo ci){if(org.NJ.hwamaihelper.client.TeleportVisualEffect.active()){keyPresses=net.minecraft.world.entity.player.Input.EMPTY;moveVector=net.minecraft.world.phys.Vec2.ZERO;}}
}
