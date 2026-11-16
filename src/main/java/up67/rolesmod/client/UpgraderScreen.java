package up67.rolesmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import up67.rolesmod.menu.UpgraderMenu;
import up67.rolesmod.upgrade.ValueCalculator;

import java.util.Locale;

public class UpgraderScreen extends AbstractContainerScreen<UpgraderMenu> {

    public UpgraderScreen(UpgraderMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 210;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int bx = this.leftPos;
        int by = this.topPos + 75;

        this.addRenderableWidget(Button.builder(Component.literal("ПРОКАЧАТЬ x1"), b ->
                        click(UpgraderMenu.BUTTON_UPGRADE_X1))
                .bounds(bx + 8, by, 78, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("x2"), b ->
                        click(UpgraderMenu.BUTTON_UPGRADE_X2))
                .bounds(bx + 90, by, 26, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("x4"), b ->
                        click(UpgraderMenu.BUTTON_UPGRADE_X4))
                .bounds(bx + 118, by, 26, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("x8"), b ->
                        click(UpgraderMenu.BUTTON_UPGRADE_X8))
                .bounds(bx + 146, by, 22, 20).build());
    }

    private void click(int buttonId) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        g.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF8B8B8B);
        drawSlotFrame(g, leftPos + 26, topPos + 40);
        drawSlotFrame(g, leftPos + 132, topPos + 40);

        // стрелка между слотами
        g.drawCenteredString(this.font, "->", leftPos + imageWidth / 2, topPos + 44, 0xFFFFFF);

        renderValuesAndChance(g);
    }

    private void renderValuesAndChance(GuiGraphics g) {
        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }
        ItemStack input = this.menu.getContainer().getItem(UpgraderMenu.SLOT_INPUT);
        ItemStack target = this.menu.getContainer().getItem(UpgraderMenu.SLOT_TARGET);

        var level = this.minecraft.level;
        var recipeManager = level.getRecipeManager();
        var registryAccess = level.registryAccess();

        if (!input.isEmpty()) {
            double v = ValueCalculator.getValue(input.getItem(), recipeManager, registryAccess);
            g.drawCenteredString(this.font, formatValue(v), leftPos + 34, topPos + 22, 0x404040);
        }
        if (!target.isEmpty()) {
            double v = ValueCalculator.getValue(target.getItem(), recipeManager, registryAccess);
            g.drawCenteredString(this.font, formatValue(v), leftPos + 140, topPos + 22, 0x404040);
        }
        if (!input.isEmpty() && !target.isEmpty() && input.getItem() != target.getItem()) {
            double inputValue = ValueCalculator.getValue(input.getItem(), recipeManager, registryAccess);
            double targetValue = ValueCalculator.getValue(target.getItem(), recipeManager, registryAccess);
            double chance = Math.min(0.95, Math.max(0.02, inputValue / targetValue));
            String text = String.format(Locale.ROOT, "шанс: %.1f%%", chance * 100);
            g.drawCenteredString(this.font, text, leftPos + imageWidth / 2, topPos + 60, 0xE0A000);
        }
    }

    private String formatValue(double v) {
        return "Ценность: " + Math.round(v);
    }

    private void drawSlotFrame(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        g.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, 8, 6, 0x404040, false);
        g.drawString(this.font, Component.literal("Положи предмет"), 10, 58, 0x404040, false);
        g.drawString(this.font, Component.literal("Образец цели"), 118, 58, 0x404040, false);
    }
}
