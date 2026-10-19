package org.NJ.hwamaihelper.mixin.client;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import org.NJ.hwamaihelper.client.ChatImages;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(GuiMessage.class)
public abstract class GuiMessageMixin {
 @Redirect(method="splitLines",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/ComponentRenderUtils;wrapComponents(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/client/gui/Font;)Ljava/util/List;"))
 private java.util.List<net.minecraft.util.FormattedCharSequence> morisMediaLayout(FormattedText text,int width,Font font){return net.minecraft.client.gui.components.ComponentRenderUtils.wrapComponents(text instanceof Component component?ChatImages.decorate(component,width,(int)(9*(Minecraft.getInstance().options.chatLineSpacing().get()+1))):text,width,font);}
}
