package up67.rolesmod.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import up67.rolesmod.RolesMod;
import up67.rolesmod.registry.ModMenuTypes;

@Mod.EventBusSubscriber(modid = RolesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(ModMenuTypes.UPGRADER_MENU.get(), UpgraderScreen::new));
    }
}
