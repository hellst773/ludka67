package up67.rolesmod.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import up67.rolesmod.RolesMod;

public class ModItems {

    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, RolesMod.MOD_ID);

    public static final RegistryObject<Item> UPGRADER_ITEM = REGISTRY.register("upgrader",
            () -> new BlockItem(ModBlocks.UPGRADER.get(), new Item.Properties()));
}
