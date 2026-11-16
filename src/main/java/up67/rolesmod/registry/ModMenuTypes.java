package up67.rolesmod.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import up67.rolesmod.RolesMod;
import up67.rolesmod.menu.UpgraderMenu;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RolesMod.MOD_ID);

    public static final RegistryObject<MenuType<UpgraderMenu>> UPGRADER_MENU = REGISTRY.register("upgrader",
            () -> IForgeMenuType.create((windowId, inv, data) -> new UpgraderMenu(windowId, inv)));
}
