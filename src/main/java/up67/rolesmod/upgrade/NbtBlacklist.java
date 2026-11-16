package up67.rolesmod.upgrade;

import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;

/**
 * Предметы, чья "ценность" целиком сидит в NBT (зачарования, зелье, текст
 * книги и т.д.) - их нельзя ни апгрейдить, ни выбрать целью: выдавать такие
 * предметы "пустыми" не имеет смысла, а оценивать NBT мод не умеет.
 */
public class NbtBlacklist {

    public static boolean isBlacklisted(Item item) {
        if (item instanceof PotionItem
                || item instanceof SpawnEggItem
                || item instanceof EnchantedBookItem
                || item instanceof WrittenBookItem
                || item instanceof WritableBookItem
                || item instanceof SuspiciousStewItem
                || item instanceof MapItem
                || item instanceof BundleItem) {
            return true;
        }
        return item == Items.PLAYER_HEAD
                || item == Items.TIPPED_ARROW
                || item == Items.GOAT_HORN
                || item == Items.KNOWLEDGE_BOOK
                || item == Items.FIREWORK_ROCKET
                || item == Items.FIREWORK_STAR
                || item == Items.FILLED_MAP;
    }

    private NbtBlacklist() {
    }
}
