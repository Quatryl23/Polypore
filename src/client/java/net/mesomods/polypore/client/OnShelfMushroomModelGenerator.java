package net.mesomods.polypore.client;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.mesomods.polypore.OnShelfMushroomModels;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class OnShelfMushroomModelGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(ModelProvider::new);
    }

    static class ModelProvider extends FabricModelProvider {
        public ModelProvider(FabricPackOutput output) {
            super(output);
        }
        @Override
        public void generateBlockStateModels(@NonNull BlockModelGenerators ignored) {
        }
        @Override
        public void generateItemModels(@NonNull ItemModelGenerators generator) {
            for (Map.Entry<Identifier, Pair<Identifier, Transformation>> entry : OnShelfMushroomModels.SPECIAL_MODELS.entrySet()) {
                Identifier itemId = entry.getKey();
                Identifier modelId = entry.getValue().getFirst();
                Transformation transform = entry.getValue().getSecond();
                if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                    throw new IllegalStateException("Tried to generate special model on shelf mushrooms for item '" + itemId + "', which does not exist");
                }
                generator.itemModelOutput.accept(BuiltInRegistries.ITEM.getValue(itemId), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(BuiltInRegistries.BLOCK.getValue(modelId)), transform));
                ((OnShelfMushroomModels.ModelProviderItemInfoCollectorMixin) generator.itemModelOutput).polypore$addPolyporeGenerated(itemId);
            }
        }
    }
}
