package net.mesomods.polypore.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import net.mesomods.polypore.Polypore;
import net.mesomods.polypore.OnShelfMushroomModels;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


@Mixin(targets = "net/minecraft/client/data/models/ModelProvider$ItemInfoCollector")
public abstract class ModelProviderItemInfoCollectorMixin implements OnShelfMushroomModels.ModelProviderItemInfoCollectorMixin {
    @Unique
    private final Set<Identifier> polyporeGenerated = new HashSet<>();

    @Unique
    @Override
    public void polypore$addPolyporeGenerated(Identifier id) {
        polyporeGenerated.add(id);
    }

    @WrapOperation(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/DataProvider;saveAll(Lnet/minecraft/data/CachedOutput;Lcom/mojang/serialization/Codec;Ljava/util/function/Function;Ljava/util/Map;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<?> save(CachedOutput cache, Codec<ClientItem> codec, Function<Item, Path> pathGetter, Map<Item, ClientItem> contents, Operation<CompletableFuture<?>> original, @Local(argsOnly = true, name = "pathProvider") PackOutput.PathProvider pathProvider) {
        Function<Item, Path> newPathGetter = (item) -> {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (polyporeGenerated.contains(id)) {
                return pathProvider.json(Polypore.id(id.getPath()));
            }
            return pathProvider.json(id);
        };
        return original.call(cache, codec, newPathGetter, contents);
    }
}
