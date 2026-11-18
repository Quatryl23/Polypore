package net.mesomods.polypore;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class PolyporeItems {
    private static final List<Pair<Identifier, Supplier<Item>>> MODEL_DUMMY_ITEMS = new ArrayList<>();
    public static final Set<Item> POTTED_FLOWERS = new HashSet<>();

    public static void addModelDummyItem(Identifier id, Supplier<Item> itemSupplier) {
        MODEL_DUMMY_ITEMS.add(Pair.of(id, itemSupplier));
    }

    @SuppressWarnings("CodeBlock2Expr")
    public static void initialize() {
        OnShelfMushroomModels.initialize();
        MODEL_DUMMY_ITEMS.forEach(pair -> {
            POTTED_FLOWERS.add(Registry.register(BuiltInRegistries.ITEM, pair.getFirst(), pair.getSecond().get()));
        });
    }
}
