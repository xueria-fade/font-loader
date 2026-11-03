package dev.xueria.fontloader.mixins;

import dev.xueria.fontloader.UnnamedMod;
import net.minecraft.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontRenderer.class)
public class MixinFontRenderer {

    @Inject(method = "drawString(Ljava/lang/String;IIIZ)I", at = @At(value = "HEAD"), cancellable = true)
    private void drawString(String text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        UnnamedMod.INSTANCE.getRenderer().drawText(text, x, y, color, shadow);
        cir.cancel();
    }

}
