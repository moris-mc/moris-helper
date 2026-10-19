package org.NJ.hwamaihelper.mixin.client;
import net.minecraft.client.*;
import net.minecraft.world.phys.Vec3;
import org.NJ.hwamaihelper.client.PlayerInspectScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Camera.class)
public abstract class PlayerInspectCameraMixin {
 @Shadow protected abstract void setPosition(Vec3 position);
 @Shadow protected abstract void setRotation(float yaw,float pitch);
 @Inject(method="alignWithEntity",at=@At("TAIL"))
 private void morisInspect(float partial,CallbackInfo ci){var mc=Minecraft.getInstance();if(org.NJ.hwamaihelper.client.TeleportVisualEffect.active()&&((Camera)(Object)this).entity()==mc.player){var frame=org.NJ.hwamaihelper.client.TeleportVisualEffect.frame(partial);if(frame!=null){setPosition(frame.position());setRotation(frame.yaw(),frame.pitch());}return;}if(mc.gui.screen() instanceof PlayerInspectScreen screen&&((Camera)(Object)this).entity()==mc.player){var pose=screen.camera(partial);if(pose!=null){setPosition(pose.position());setRotation(pose.yaw(),pose.pitch());}}}
}
