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
import java.lang.reflect.Method;

@Mixin(ItemStack.class)
public class ExampleMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void onHighFrequencyTick(Level level, Entity entity, EquipmentSlot slot, CallbackInfo ci) {
        if (level.isClientSide()) {
            try {
                Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
                Method getInstance = mcClass.getMethod("getInstance");
                Object clientInstance = getInstance.invoke(null);

                if (clientInstance != null) {
                    // Work 1: Erase missTime (Mojang field controlling placement and manual click delays)
                    Field missTimeField = mcClass.getDeclaredField("missTime");
                    missTimeField.setAccessible(true);
                    missTimeField.setInt(clientInstance, 0);

                    // Work 2: Erase blockBreakDelay (Mojang field controlling sequential breaks when you choose to punch)
                    Field gameModeField = mcClass.getDeclaredField("gameMode");
                    gameModeField.setAccessible(true);
                    Object gameMode = gameModeField.get(clientInstance);
                    
                    if (gameMode != null) {
                        Field breakDelayField = gameMode.getClass().getDeclaredField("blockBreakDelay");
                        breakDelayField.setAccessible(true);
                        breakDelayField.setInt(gameMode, 0);
                    }

                    // Work 3: AUTO-HOLD PLACEMENT ONLY
                    Field optionsField = mcClass.getDeclaredField("options");
                    optionsField.setAccessible(true);
                    Object options = optionsField.get(clientInstance);

                    if (options != null) {
                        // Dynamically look up the Right-Click (Use Item) KeyMapping instance
                        Field keyUseField = options.getClass().getDeclaredField("keyUse");
                        keyUseField.setAccessible(true);
                        Object keyUse = keyUseField.get(options);
                        
                        if (keyUse != null) {
                            // Check if you are actively holding down the Right-Click button
                            Method isDownMethod = keyUse.getClass().getMethod("isDown");
                            boolean isRightClickHeld = (boolean) isDownMethod.invoke(keyUse);

                            if (isRightClickHeld) {
                                // Forces an instant right-click simulation frame-by-frame
                                Method startUseItemMethod = mcClass.getDeclaredMethod("startUseItem");
                                startUseItemMethod.setAccessible(true);
                                startUseItemMethod.invoke(clientInstance);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                // Safely catches isolated environments without crashing or printing log data
            }
        }
    }
}
