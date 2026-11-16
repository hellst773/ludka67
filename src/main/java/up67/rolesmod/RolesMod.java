package up67.rolesmod;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import up67.rolesmod.registry.ModBlocks;
import up67.rolesmod.registry.ModItems;
import up67.rolesmod.registry.ModMenuTypes;

@Mod(RolesMod.MOD_ID)
public class RolesMod {

    public static final String MOD_ID = "up67";

    public RolesMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.REGISTRY.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
        ModMenuTypes.REGISTRY.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.UPGRADER_ITEM);
        }
    }
}
