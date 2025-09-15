package com.minenash.rebind_all_the_keys.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.minenash.rebind_all_the_keys.RebindAllTheKeys.*;
import static com.minenash.rebind_all_the_keys.RebindAllTheKeys.SCROLL_RIGHT;

@Environment(EnvType.CLIENT)
@Mixin(KeybindsScreen.class)
public abstract class KeybindsScreenMixin extends GameOptionsScreen {

    @Shadow @Nullable public KeyBinding selectedKeyBinding;

    @Shadow private ControlsListWidget controlsList;

    public KeybindsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/option/KeyBinding;setBoundKey(Lnet/minecraft/client/util/InputUtil$Key;)V"))
    public void ScreenPrimaryCanNotBeUnbound(KeyBinding binding, InputUtil.Key key) {
        if (binding == SCREEN_PRIMARY) key = InputUtil.Type.MOUSE.createFromCode(0);
        if (binding == SCREEN_SECONDARY) key = InputUtil.Type.MOUSE.createFromCode(1);
        binding.setBoundKey(key);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
//        System.out.println("H: " + horizontalAmount + " V: " + verticalAmount);
        if (selectedKeyBinding != null) {
            InputUtil.Key key = Math.abs(verticalAmount) > Math.abs(horizontalAmount) ? verticalAmount > 0 ? SCROLL_UP : SCROLL_DOWN : horizontalAmount > 0 ? SCROLL_LEFT : SCROLL_RIGHT;
            selectedKeyBinding.setBoundKey(key);
            gameOptions.write();
            this.selectedKeyBinding = null;
            controlsList.update();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
