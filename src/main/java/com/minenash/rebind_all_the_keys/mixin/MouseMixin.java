package com.minenash.rebind_all_the_keys.mixin;

import com.minenash.rebind_all_the_keys.RebindAllTheKeys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Scroller;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.minenash.rebind_all_the_keys.RebindAllTheKeys.*;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow private int activeButton;

    @Unique
    private static boolean block(int button) {
        return switch (button) {
            case 0 -> !SCREEN_PRIMARY.matchesMouse(0) && !SCREEN_SECONDARY.matchesMouse(0);
            case 1 -> !SCREEN_PRIMARY.matchesMouse(1) && !SCREEN_SECONDARY.matchesMouse(1);
            default -> false;
        };
    }

    @Unique
    private static int remap(int button) {
        if (SCREEN_PRIMARY.matchesMouse(button))
            return 0;
        if (SCREEN_SECONDARY.matchesMouse(button))
            return 1;
        return button;
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void setIsMouseKeyDown(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS)
            RebindAllTheKeys.IS_MOUSE_DOWN.put(button, true);
        else if (action == GLFW.GLFW_RELEASE)
            RebindAllTheKeys.IS_MOUSE_DOWN.put(button, false);
    }

    @Redirect(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"))
    public boolean onMouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        return !block(button) && screen.mouseClicked(mouseX, mouseY, remap(button));
    }

    @Redirect(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(DDI)Z"))
    public boolean onMouseReleased(Screen screen, double mouseX, double mouseY, int button) {
        return !block(button) && screen.mouseReleased(mouseX, mouseY, remap(button));
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseDragged(DDIDD)Z"))
    public boolean onMouseDragged(Screen screen, double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (block(button))
            return false;

        if (button == -10)
            button = 0;
        else if (button == -9)
            button = 1;
        else
            button = remap(button);

        return screen.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Scroller;scrollCycling(DII)I"))
    public int blockHotbarScroll(double amount, int selectedIndex, int total) {
        if (amount < 0 && HOTBAR_NEXT_OVERRIDE.isUnbound()
           || amount > 0 && HOTBAR_PREVIOUS_OVERRIDE.isUnbound())
            return Scroller.scrollCycling(amount, selectedIndex, total);
        else
            return selectedIndex;
    }

    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SpectatorHud;cycleSlot(I)V"))
    public void blockSpectatorHotbarScroll(SpectatorHud instance, int offset) {
        if (offset > 0 && HOTBAR_NEXT_OVERRIDE.isUnbound()
                || offset < 0 && HOTBAR_PREVIOUS_OVERRIDE.isUnbound())
            MinecraftClient.getInstance().inGameHud.getSpectatorHud().cycleSlot(-offset);
    }

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getOverlay()Lnet/minecraft/client/gui/screen/Overlay;"))
    public void activateScrollKeybinds(long window, double horizontal, double vertical, CallbackInfo ci) {
        InputUtil.Key key = Math.abs(vertical) > Math.abs(horizontal) ? vertical > 0 ? SCROLL_UP : SCROLL_DOWN : horizontal > 0 ? SCROLL_LEFT : SCROLL_RIGHT;
        KeyBinding.setKeyPressed(key, true);
        KeyBinding.onKeyPressed(key);
    }
}
