package up67.rolesmod.upgrade;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Считает "ценность" предмета, разворачивая его настоящие рецепты (крафт, печь,
 * доменная печь, коптильня, костёр, камнерез) до сырья. Если у предмета рецепта
 * нет - используется базовое "сырьевое" значение или, если и этого нет,
 * оценка по редкости (Rarity).
 *
 * Расчёт кэшируется, но кэш общий на сессию (на перезапуск игры/датапака
 * нужно вызвать {@link #clearCache()}).
 */
public class ValueCalculator {

    /** Базовые "сырьевые" значения для вещей, которые не крафтятся (добываются/падают с мобов). */
    private static final Map<Item, Double> BASE_VALUES = new HashMap<>();

    private static final Map<Item, Double> CACHE = new HashMap<>();
    private static final Set<Item> IN_PROGRESS = new HashSet<>();

    static {
        base(Items.DIRT, 1); base(Items.COBBLESTONE, 1); base(Items.STONE, 1);
        base(Items.SAND, 1); base(Items.GRAVEL, 1); base(Items.NETHERRACK, 1);
        base(Items.STICK, 1); base(Items.STRING, 2); base(Items.FEATHER, 2);
        base(Items.LEATHER, 3); base(Items.BONE, 2); base(Items.GUNPOWDER, 3);
        base(Items.SLIME_BALL, 3); base(Items.FLINT, 2); base(Items.WHEAT, 2);
        base(Items.EGG, 3); base(Items.COAL, 4); base(Items.REDSTONE, 3);
        base(Items.LAPIS_LAZULI, 3); base(Items.QUARTZ, 4); base(Items.GLOWSTONE_DUST, 3);
        base(Items.RAW_IRON, 8); base(Items.RAW_COPPER, 5); base(Items.RAW_GOLD, 10);
        base(Items.IRON_INGOT, 8); base(Items.GOLD_INGOT, 10); base(Items.COPPER_INGOT, 5);
        base(Items.DIAMOND, 120); base(Items.EMERALD, 60); base(Items.AMETHYST_SHARD, 10);
        base(Items.NETHERITE_SCRAP, 400); base(Items.ANCIENT_DEBRIS, 400);
        base(Items.ENDER_PEARL, 20); base(Items.BLAZE_ROD, 25); base(Items.GHAST_TEAR, 40);
        base(Items.NETHER_STAR, 2000); base(Items.DRAGON_EGG, 5000);
        base(Items.ELYTRA, 3000); base(Items.TOTEM_OF_UNDYING, 1500);
        base(Items.OBSIDIAN, 15); base(Items.END_STONE, 3);
        base(Items.OAK_LOG, 2); base(Items.SPRUCE_LOG, 2); base(Items.BIRCH_LOG, 2);
        base(Items.JUNGLE_LOG, 2); base(Items.ACACIA_LOG, 2); base(Items.DARK_OAK_LOG, 2);
        base(Items.MANGROVE_LOG, 2); base(Items.CHERRY_LOG, 2);
        base(Items.WHITE_WOOL, 3); base(Items.CLAY_BALL, 2); base(Items.SUGAR_CANE, 1);
        base(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 50);

        // кузнечный стол (netherite upgrade) - проще прописать руками,
        // чем разбирать SmithingTransformRecipe по компонентам
        manualNetherite(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
        manualNetherite(Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        manualNetherite(Items.DIAMOND_AXE, Items.NETHERITE_AXE);
        manualNetherite(Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
        manualNetherite(Items.DIAMOND_HOE, Items.NETHERITE_HOE);
        manualNetherite(Items.DIAMOND_HELMET, Items.NETHERITE_HELMET);
        manualNetherite(Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        manualNetherite(Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
        manualNetherite(Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);
    }

    private static void base(Item item, double value) {
        BASE_VALUES.put(item, value);
    }

    private static void manualNetherite(Item diamondVariant, Item netheriteVariant) {
        // значение считается лениво при первом запросе (нужен RecipeManager), см. NETHERITE_SOURCE ниже
        NETHERITE_SOURCE.put(netheriteVariant, diamondVariant);
    }

    private static final Map<Item, Item> NETHERITE_SOURCE = new HashMap<>();

    public static void clearCache() {
        CACHE.clear();
        IN_PROGRESS.clear();
    }

    public static double getValue(Item item, RecipeManager recipeManager, RegistryAccess registryAccess) {
        if (item == Items.AIR) {
            return 0;
        }
        Double base = BASE_VALUES.get(item);
        if (base != null) {
            return base;
        }
        Double cached = CACHE.get(item);
        if (cached != null) {
            return cached;
        }
        if (NETHERITE_SOURCE.containsKey(item)) {
            Item diamondVariant = NETHERITE_SOURCE.get(item);
            double value = getValue(diamondVariant, recipeManager, registryAccess)
                    + getValue(Items.NETHERITE_INGOT, recipeManager, registryAccess)
                    + BASE_VALUES.getOrDefault(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 50.0);
            CACHE.put(item, value);
            return value;
        }

        if (IN_PROGRESS.contains(item)) {
            // цикл в рецептах (или рецепт ссылается сам на себя) - не уходим в рекурсию навсегда
            return rarityValue(item);
        }

        IN_PROGRESS.add(item);
        try {
            double best = Double.MAX_VALUE;
            boolean found = false;

            for (Recipe<?> recipe : allRelevantRecipes(recipeManager)) {
                ItemStack result;
                try {
                    result = recipe.getResultItem(registryAccess);
                } catch (Exception ignored) {
                    continue;
                }
                if (result.isEmpty() || result.getItem() != item) {
                    continue;
                }
                List<Ingredient> ingredients = ingredientsOf(recipe);
                if (ingredients.isEmpty()) {
                    continue;
                }
                double cost = 0;
                boolean valid = true;
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.isEmpty()) {
                        continue;
                    }
                    ItemStack[] options = ingredient.getItems();
                    if (options.length == 0) {
                        valid = false;
                        break;
                    }
                    double cheapest = Double.MAX_VALUE;
                    for (ItemStack option : options) {
                        if (option.getItem() == item) {
                            // ингредиент ссылается сам на себя - пропускаем вариант
                            continue;
                        }
                        double v = getValue(option.getItem(), recipeManager, registryAccess) * option.getCount();
                        if (v < cheapest) {
                            cheapest = v;
                        }
                    }
                    if (cheapest == Double.MAX_VALUE) {
                        valid = false;
                        break;
                    }
                    cost += cheapest;
                }
                if (!valid) {
                    continue;
                }
                int outputCount = Math.max(1, result.getCount());
                double perItem = cost / outputCount;
                if (perItem < best) {
                    best = perItem;
                    found = true;
                }
            }

            double value = found ? best : rarityValue(item);
            CACHE.put(item, value);
            return value;
        } finally {
            IN_PROGRESS.remove(item);
        }
    }

    private static double rarityValue(Item item) {
        Rarity rarity = new ItemStack(item).getRarity();
        return switch (rarity) {
            case COMMON -> 5;
            case UNCOMMON -> 25;
            case RARE -> 100;
            case EPIC -> 400;
        };
    }

    private static List<Recipe<?>> allRelevantRecipes(RecipeManager recipeManager) {
        List<Recipe<?>> list = new java.util.ArrayList<>();
        list.addAll(recipeManager.getAllRecipesFor(RecipeType.CRAFTING));
        list.addAll(recipeManager.getAllRecipesFor(RecipeType.SMELTING));
        list.addAll(recipeManager.getAllRecipesFor(RecipeType.BLASTING));
        list.addAll(recipeManager.getAllRecipesFor(RecipeType.SMOKING));
        list.addAll(recipeManager.getAllRecipesFor(RecipeType.CAMPFIRE_COOKING));
        list.addAll(recipeManager.getAllRecipesFor(RecipeType.STONECUTTING));
        return list;
    }

    private static List<Ingredient> ingredientsOf(Recipe<?> recipe) {
        // Recipe<C> уже объявляет getIngredients() с реализацией по умолчанию
        // (пустой список), так что безопасно вызывать его на любом рецепте.
        try {
            return recipe.getIngredients();
        } catch (Exception e) {
            return List.of();
        }
    }

    private ValueCalculator() {
    }
}
