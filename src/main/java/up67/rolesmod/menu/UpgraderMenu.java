package up67.rolesmod.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import up67.rolesmod.registry.ModMenuTypes;
import up67.rolesmod.upgrade.JokeMessages;
import up67.rolesmod.upgrade.NbtBlacklist;
import up67.rolesmod.upgrade.ValueCalculator;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Апгрейдер бесплатный: в первый слот кладётся предмет, который реально
 * тратится и улучшается. Во второй слот кладётся ОДИН экземпляр желаемого
 * предмета как "образец цели" - он НЕ тратится, просто показывает моду,
 * что ты хочешь получить (точный аналог выбора предмета из каталога, но без
 * отдельного экрана-каталога).
 *
 * Шанс успеха = ценность входного предмета / ценность целевого предмета,
 * ограниченный снизу и сверху, чтобы даунгрейд не был гарантированной победой,
 * а апгрейд никогда не падал до 0%.
 */
public class UpgraderMenu extends AbstractContainerMenu {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_TARGET = 1;

    public static final int BUTTON_UPGRADE_X1 = 0;
    public static final int BUTTON_UPGRADE_X2 = 1;
    public static final int BUTTON_UPGRADE_X4 = 2;
    public static final int BUTTON_UPGRADE_X8 = 3;

    private static final double MIN_CHANCE = 0.02;
    private static final double MAX_CHANCE = 0.95;

    private final Container container = new SimpleContainer(2) {
        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    };

    public UpgraderMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.UPGRADER_MENU.get(), containerId);

        // слот предмета, который реально улучшаем (тратится)
        this.addSlot(new Slot(container, SLOT_INPUT, 26, 40));
        // слот-образец желаемого предмета (не тратится)
        this.addSlot(new Slot(container, SLOT_TARGET, 132, 40));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 112 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 170));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int amount = switch (id) {
            case BUTTON_UPGRADE_X1 -> 1;
            case BUTTON_UPGRADE_X2 -> 2;
            case BUTTON_UPGRADE_X4 -> 4;
            case BUTTON_UPGRADE_X8 -> 8;
            default -> -1;
        };
        if (amount > 0) {
            tryUpgrade(player, amount);
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    private void tryUpgrade(Player player, int requestedAmount) {
        ItemStack input = container.getItem(SLOT_INPUT);
        ItemStack target = container.getItem(SLOT_TARGET);

        if (input.isEmpty() || target.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("Положи предмет для апгрейда и образец того, что хочешь получить."), true);
            return;
        }
        if (input.getItem() == target.getItem()) {
            player.displayClientMessage(
                    Component.literal("Это же один и тот же предмет, гений."), true);
            return;
        }
        if (NbtBlacklist.isBlacklisted(input.getItem()) || NbtBlacklist.isBlacklisted(target.getItem())) {
            player.displayClientMessage(
                    Component.literal("Этот предмет нельзя апгрейдить или выбрать целью (зелья/книги/головы и т.п. зависят от NBT)."),
                    true);
            return;
        }

        Level level = player.level();
        double inputValue = ValueCalculator.getValue(input.getItem(), level.getRecipeManager(), level.registryAccess());
        double targetValue = ValueCalculator.getValue(target.getItem(), level.getRecipeManager(), level.registryAccess());
        double chance = Math.min(MAX_CHANCE, Math.max(MIN_CHANCE, inputValue / targetValue));

        int amount = Math.min(requestedAmount, input.getCount());
        if (amount <= 0) {
            return;
        }

        int successes = 0;
        for (int i = 0; i < amount; i++) {
            input.shrink(1);
            boolean success = ThreadLocalRandom.current().nextDouble() < chance;
            if (success) {
                successes++;
                ItemStack result = new ItemStack(target.getItem());
                if (!player.getInventory().add(result)) {
                    player.drop(result, false);
                }
            }
        }

        if (amount == 1) {
            if (successes == 1) {
                player.displayClientMessage(JokeMessages.randomSuccess(chance), false);
                playSuccessSound(player);
            } else {
                player.displayClientMessage(JokeMessages.randomFail(), false);
                playFailSound(player);
            }
        } else {
            player.displayClientMessage(JokeMessages.batchSummary(successes, amount, chance), false);
            if (successes > 0) {
                playSuccessSound(player);
            } else {
                playFailSound(player);
            }
        }

        this.broadcastChanges();
    }

    private void playSuccessSound(Player player) {
        player.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void playFailSound(Player player) {
        player.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.ANVIL_BREAK,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
    }

    /** Используется клиентским экраном, чтобы прочитать текущие предметы в слотах и показать подсказку. */
    public Container getContainer() {
        return container;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (index < 2) {
                if (!this.moveItemStackTo(originalStack, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(originalStack, SLOT_INPUT, SLOT_TARGET + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            this.clearContainer(player, container);
        }
    }
}
