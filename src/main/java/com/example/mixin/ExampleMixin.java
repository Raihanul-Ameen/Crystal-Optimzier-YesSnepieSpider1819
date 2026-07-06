package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import java.lang.reflect.Field;

// Targets a network listener class natively shared by both environments
@Mixin(ServerGamePacketListenerImpl.class)
public class ExampleMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onSharedNetworkTick(CallbackInfo ci) {
        try {
            // Mojang class mapping path for MinecraftClient
            Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
            java.lang.reflect.Method getInstance = mcClass.getMethod("getInstance");
            Object clientInstance = getInstance.invoke(null);

            if (clientInstance != null) {
                // Work 1: Erase missTime (Mojang field that controls placement & attack delay clicks)
                Field missTimeField = mcClass.getDeclaredField("missTime");
                missTimeField.setAccessible(true);
                missTimeField.setInt(clientInstance, 0);

                // Work 2: Erase blockBreakDelay inside the gameMode variable
                Field gameModeField = mcClass.getDeclaredField("gameMode");
                gameModeField.setAccessible(true);
                Object gameMode = gameModeField.get(clientInstance);
                
                if (gameMode != null) {
                    // MultiPlayerGameMode is mapped to gameMode's underlying class reference
                    Field breakDelayField = gameMode.getClass().getDeclaredField("blockBreakDelay");
                    breakDelayField.setAccessible(true);
                    breakDelayField.setInt(gameMode, 0);
                }
            }
        } catch (Exception ignored) {
            // Isolates execution to run safely inside client blocks without server log crashes
        }
    }
}
