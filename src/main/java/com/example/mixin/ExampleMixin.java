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
    private void onVanillaZeroDelayTick(Level level, Entity entity, EquipmentSlot slot, CallbackInfo ci) {
        // Only modify values on your local client screen thread
        if (level.isClientSide()) {
            try {
                // Fetch the running client engine directly via text strings
                Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
                java.lang.reflect.Method getInstance = mcClass.getMethod("getInstance");
                Object clientInstance = getInstance.invoke(null);

                if (clientInstance != null) {
                    // 1. Wipe out the action/use click cooldown timer entirely
                    Field missTimeField = mcClass.getDeclaredField("missTime");
                    missTimeField.setAccessible(true);
                    missTimeField.setInt(clientInstance, 0);

                    // 2. Wipe out the sequential multi-interaction break delay buffer
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
                // Completely safe, runs silently without causing server thread exceptions
            }
        }
    }
}
