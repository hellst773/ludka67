package up67.rolesmod.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.sounds.SoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import up67.rolesmod.RolesMod;
import up67.rolesmod.block.UpgraderBlock;

public class ModBlocks {

    public static final DeferredRegister<Block> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RolesMod.MOD_ID);

    public static final RegistryObject<Block> UPGRADER = REGISTRY.register("upgrader",
            () -> new UpgraderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 1200.0F)
                    .sound(SoundType.ANVIL)
                    .noOcclusion()));
}
