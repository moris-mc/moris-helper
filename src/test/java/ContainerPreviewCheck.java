import org.NJ.hwamaihelper.client.PreviewItemDecoder;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import com.google.gson.JsonObject;
public class ContainerPreviewCheck {
 public static void main(String[] args)throws Exception{
  // Check the installed MaLiLib name map, avoiding an assumed legacy key name.
  var field=fi.dy.masa.malilib.util.input.ScanCodes.class.getDeclaredField("MAP_NAME_TO_SCAN_CODE");field.setAccessible(true);var names=(it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<?>)field.get(null);if(names.getInt("RIGHT_ALT")!=com.mojang.blaze3d.platform.InputConstants.KEY_RALT)throw new AssertionError("Wrong default Alt key");
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var registries=RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);var ops=RegistryOps.create(NbtOps.INSTANCE,registries);
  // Standalone fixture: data-pack loading normally binds item component defaults in game.
  Items.DIAMOND.builtInRegistryHolder().bindComponents(net.minecraft.core.component.DataComponentMap.builder().set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE,64).build());
  ItemStack original=new ItemStack(Items.DIAMOND,12);Identifier model=Identifier.parse("minecraft:5coin");original.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,model);original.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);
  CompoundTag tag=(CompoundTag)ItemStack.CODEC.encodeStart(ops,original).getOrThrow();tag.putInt("DataVersion",99999);var output=new java.io.ByteArrayOutputStream();NbtIo.writeCompressed(tag,output);
  JsonObject slot=new JsonObject();slot.addProperty("id","minecraft:diamond");slot.addProperty("nbt",java.util.Base64.getEncoder().encodeToString(output.toByteArray()));var decoded=PreviewItemDecoder.decode(slot,registries);
  if(decoded.getCount()!=12||!model.equals(decoded.get(net.minecraft.core.component.DataComponents.ITEM_MODEL))||!Boolean.TRUE.equals(decoded.get(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE)))throw new AssertionError("Appearance data lost");
  slot.addProperty("nbt","invalid");if(PreviewItemDecoder.decode(slot,registries).getItem()!=Items.DIAMOND)throw new AssertionError("Malformed icon fallback");
  slot.addProperty("nbt","a".repeat(5000));if(PreviewItemDecoder.decode(slot,registries).getItem()!=Items.DIAMOND)throw new AssertionError("Oversize icon fallback");
  System.out.println("PASS: installed MaLiLib RIGHT_ALT mapping, native compressed item NBT with DataVersion, model/glint/count, malformed and oversized icon fallback.");
 }
}
