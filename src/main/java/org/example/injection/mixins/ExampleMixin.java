package org.example.injection.mixins;

import net.minecraft.Minecraft;
import org.example.ExampleMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ExampleMixin {

    @Inject(method = "startGame", at = @At(value = "RETURN"))
    private void injectStartGame(CallbackInfo ci) {
        ExampleMod.logger.info("Mixin inject Minecraft#startGame");
    }

}
