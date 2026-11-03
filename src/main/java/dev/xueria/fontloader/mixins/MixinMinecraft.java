package dev.xueria.fontloader.mixins;

import dev.xueria.fontloader.UnnamedMod;
import net.minecraft.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "startGame", at = @At(value = "RETURN"))
    private void i(CallbackInfo ci) {
        UnnamedMod.INSTANCE.init();
    }

}
