package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import java.lang.reflect.Field;

@Mixin(ItemStack.class)
public class ExampleMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void onHighFrequencyTick(Level level, Entity entity, EquipmentSlot slot, CallbackInfo ci) {
        // Triggers continuously on the client thread layer to stop the 1-second pauses
        if (level.isClientSide()) {
            try {
                Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
                java.lang.reflect.Method getInstance = mcClass.getMethod("getInstance");
                Object clientInstance = getInstance.invoke(null);

                if (clientInstance != null) {
                    // Work 1: Force placement and weapon attack delay clicks to 0
                    Field missTimeField = mcClass.getDeclaredField("missTime");
                    missTimeField.setAccessible(true);
                    missTimeField.setInt(clientInstance, 0);

                    // Work 2: Force multi-target breaking delay buffer to 0
                    Field gameModeField = mcClass.getDeclaredField("gameMode");
                    gameModeField.setAccessible(true);
                    Object gameMode = gameModeField.get(clientInstance);
                    
                    if (gameMode != null) {
                        Field breakDelayField = gameMode.getClass().getDeclaredField("blockBreakDelay");
                        breakDelayField.setAccessible(true);
                        breakDelayField.setInt(gameMode, 0);
                    }
                }
            } catch (Exception ignored) {
                // Safely handles server execution steps without tracking logs
            }
        }
    }
}
