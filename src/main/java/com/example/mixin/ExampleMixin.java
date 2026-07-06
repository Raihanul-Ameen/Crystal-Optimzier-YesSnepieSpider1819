package com.example.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ExampleMixin {

    @Shadow private int missTime; // Tracks crystal placement and attack click delays

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // Sets placement and attack delay to absolute 0 on every game tick
        this.missTime = 0;

        // Safely resets multi-interaction/breaking delay if the player is actively in a world
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.gameMode != null) {
            client.gameMode.blockBreakDelay = 0;
        }
    }
}
