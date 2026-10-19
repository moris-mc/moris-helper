package org.NJ.hwamaihelper.client;
import com.google.gson.JsonObject;
import java.util.Base64;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
public final class PreviewItemDecoder {
 public static ItemStack decode(JsonObject slot,HolderLookup.Provider registries){try{String encoded=slot.get("nbt").getAsString();if(encoded.length()>4096)throw new IllegalArgumentException();byte[] bytes=Base64.getDecoder().decode(encoded);var tag=net.minecraft.nbt.NbtIo.readCompressed(new java.io.ByteArrayInputStream(bytes),net.minecraft.nbt.NbtAccounter.create(131072));return ItemStack.CODEC.parse(net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE,registries),tag).result().orElseThrow();}catch(Exception e){var item=BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(slot.get("id").getAsString()));return item==null?ItemStack.EMPTY:new ItemStack(item);}}
 private PreviewItemDecoder(){}
}
