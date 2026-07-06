package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public class ExampleMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void onHighFrequencyTick(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected, CallbackInfo ci) {
        // Run every single processing tick to prevent the 1-second internal cycle delay
        if (level.isClientSide()) {
            try {
                Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
                java.lang.reflect.Method getInstance = mcClass.getMethod("getInstance");
                Object clientInstance = getInstance.invoke(null);

                if (clientInstance != null) {
                    // Force the raw action click/miss cooldown timer to absolute 0
                    java.lang.reflect.Field missTimeField = mcClass.getDeclaredField("missTime");
                    missTimeField.setAccessible(true);
                    missTimeField.setInt(clientInstance, 0);

                    // Force sequential attack and breaker delay to absolute 0
                    java.lang.reflect.Field gameModeField = mcClass.getDeclaredField("gameMode");
                    gameModeField.setAccessible(true);
                    Object gameMode = gameModeField.get(clientInstance);
                    
                    if (gameMode != null) {
                        java.lang.reflect.Field breakDelayField = gameMode.getClass().getDeclaredField("blockBreakDelay");
                        breakDelayField.setAccessible(true);
                        breakDelayField.setInt(gameMode, 0);
                    }
                }
            } catch (Exception ignored) {
                // Safeguard for server threads
            }
        }
    }
}
