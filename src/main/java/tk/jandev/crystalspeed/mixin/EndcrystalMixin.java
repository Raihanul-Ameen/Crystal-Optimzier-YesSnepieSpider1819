package tk.jandev.crystalspeed.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EndCrystalEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalEntity.class)
public abstract class EndcrystalMixin {

    @Inject(at = @At("HEAD"), method = "damage")
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
            StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);
            
            if (weakness != null && strength != null) {
                if (strength.getAmplifier() > weakness.getAmplifier()) {
                    // Call discard() directly instead of a shadow kill method
                    ((EndCrystalEntity)(Object)this).discard();
                }
            }
        }
    }
}
