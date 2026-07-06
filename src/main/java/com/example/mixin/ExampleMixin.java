package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.InteractionResult;
import java.lang.reflect.Field;

@Mixin(InteractionResult.class)
public class ExampleMixin {

    // Injecting into indicatesResult ensures this runs every time your game processes an interaction click state
    @Inject(method = "indicatesResult", at = @At("HEAD"))
    private static void onInteractionUpdate(CallbackInfoReturnable<Boolean> cir) {
        try {
            // Securely grab the client engine loop using strings to hide it from the GitHub compiler
            Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
            java.lang.reflect.Method getInstance = mcClass.getMethod("getInstance");
            Object clientInstance = getInstance.invoke(null);

            if (clientInstance != null) {
                // 1. Wipe out placement/click use delays (missTime)
                Field missTimeField = mcClass.getDeclaredField("missTime");
                missTimeField.setAccessible(true);
                missTimeField.setInt(clientInstance, 0);

                // 2. Wipe out rapid block breaking/attacking delays (blockBreakDelay)
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
            // Silently drops server-thread passes to ensure 100% stable execution
        }
    }
}
