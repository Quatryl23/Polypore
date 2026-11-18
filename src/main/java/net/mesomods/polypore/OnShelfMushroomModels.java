package net.mesomods.polypore;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class OnShelfMushroomModels {
    private static final Transformation NO_TRANSFORM = new Transformation(null, null, null, null);
    private static final Transformation DOUBLE_SIZE = new Transformation(new Vector3f(-0.5f, -0.5f, -0.5f), null, new Vector3f(2, 2, 2), null);
    private static final Transformation HALF_SIZE = new Transformation(new Vector3f(0.25f, 0.25f, 0.25f), null, new Vector3f(0.5f, 0.5f, 0.5f), null);
    public static final Map<Identifier, Pair<Identifier, Transformation>> SPECIAL_MODELS = Util.make(new HashMap<>(), (map) -> {
        addDoubleSize(map, "minecraft:lantern");
        addDoubleSize(map, "minecraft:soul_lantern");
        addDoubleSize(map, "minecraft:copper_lantern");
        addDoubleSize(map, "minecraft:exposed_copper_lantern");
        addDoubleSize(map, "minecraft:weathered_copper_lantern");
        addDoubleSize(map, "minecraft:oxidized_copper_lantern");
        addByParentDoubleSize(map, "minecraft:waxed_copper_lantern", "minecraft:copper_lantern");
        addByParentDoubleSize(map, "minecraft:waxed_exposed_copper_lantern", "minecraft:exposed_copper_lantern");
        addByParentDoubleSize(map, "minecraft:waxed_weathered_copper_lantern", "minecraft:weathered_copper_lantern");
        addByParentDoubleSize(map, "minecraft:waxed_oxidized_copper_lantern", "minecraft:oxidized_copper_lantern");
        add(map, "minecraft:flower_pot");
        addHalfSize(map, "minecraft:cauldron");
        addWithItem(map, "minecraft:potted_dandelion");
        addWithItem(map, "minecraft:potted_poppy");
        addWithItem(map, "minecraft:potted_blue_orchid");
        addWithItem(map, "minecraft:potted_allium");
        addWithItem(map, "minecraft:potted_azure_bluet");
        addWithItem(map, "minecraft:potted_red_tulip");
        addWithItem(map, "minecraft:potted_orange_tulip");
        addWithItem(map, "minecraft:potted_white_tulip");
        addWithItem(map, "minecraft:potted_pink_tulip");
        addWithItem(map, "minecraft:potted_oxeye_daisy");
        addWithItem(map, "minecraft:potted_cornflower");
        addWithItem(map, "minecraft:potted_lily_of_the_valley");
        addWithItem(map, "minecraft:potted_wither_rose");
        addWithItem(map, "minecraft:potted_torchflower");
        addWithItem(map, "minecraft:potted_oak_sapling");
        addWithItem(map, "minecraft:potted_spruce_sapling");
        addWithItem(map, "minecraft:potted_birch_sapling");
        addWithItem(map, "minecraft:potted_jungle_sapling");
        addWithItem(map, "minecraft:potted_acacia_sapling");
        addWithItem(map, "minecraft:potted_dark_oak_sapling");
        addWithItem(map, "minecraft:potted_cherry_sapling");
        addWithItem(map, "minecraft:potted_pale_oak_sapling");
        addWithItem(map, "minecraft:potted_poplar_sapling");
        addWithItem(map, "minecraft:potted_red_mushroom");
        addWithItem(map, "minecraft:potted_brown_mushroom");
        addWithItem(map, "minecraft:potted_fern");
        addWithItem(map, "minecraft:potted_dead_bush");
        addWithItem(map, "minecraft:potted_cactus");
        addWithItem(map, "minecraft:potted_bamboo");
        addWithItem(map, "minecraft:potted_azalea_bush");
        addWithItem(map, "minecraft:potted_flowering_azalea_bush");
        addWithItem(map, "minecraft:potted_crimson_fungus");
        addWithItem(map, "minecraft:potted_warped_fungus");
        addWithItem(map, "minecraft:potted_crimson_roots");
        addWithItem(map, "minecraft:potted_warped_roots");
        addWithItem(map, "minecraft:potted_mangrove_propagule");
        addWithItem(map, "minecraft:potted_closed_eyeblossom");
        addWithItem(map, "minecraft:potted_open_eyeblossom");
        addWithItem(map, "minecraft:potted_golden_dandelion");
    });

    public static ItemStack withCorrectModel(ItemStack stack) {
        Identifier originalPath = stack.get(DataComponents.ITEM_MODEL);
        if (originalPath == null) return stack;
        if (SPECIAL_MODELS.containsKey(originalPath)) {
            ItemStack copy = stack.copy();
            copy.set(DataComponents.ITEM_MODEL, Polypore.id(originalPath.getPath()));
            return copy;
        }
        return stack;
    }

    public static void addDoubleSize(HashMap<Identifier, Pair<Identifier, Transformation>> map, String id) {
        Identifier parsedId = Identifier.parse(id);
        map.put(parsedId, Pair.of(parsedId, DOUBLE_SIZE));
    }

    public static void add(HashMap<Identifier, Pair<Identifier, Transformation>> map, String id) {
        Identifier parsedId = Identifier.parse(id);
        map.put(parsedId, Pair.of(parsedId, NO_TRANSFORM));
    }

    public static void addByParentDoubleSize(HashMap<Identifier, Pair<Identifier, Transformation>> map, String id, String parentId) {
        map.put(Identifier.parse(id), Pair.of(Identifier.parse(parentId), DOUBLE_SIZE));
    }

    public static void addHalfSize(HashMap<Identifier, Pair<Identifier, Transformation>> map, String id) {
        Identifier parsedId = Identifier.parse(id);
        map.put(parsedId, Pair.of(parsedId, HALF_SIZE));
    }

    public static void addWithItem(HashMap<Identifier, Pair<Identifier, Transformation>> map, String id) {
        add(map, id);
        Identifier parsedId = Identifier.parse(id);
        PolyporeItems.addModelDummyItem(parsedId, () -> new Item(new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), parsedId))));
    }

    public interface ModelProviderItemInfoCollectorMixin {
        void polypore$addPolyporeGenerated(Identifier id);
    }

    public static void initialize() {
    }
}
