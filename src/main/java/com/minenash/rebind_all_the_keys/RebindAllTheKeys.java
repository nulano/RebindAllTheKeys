package com.minenash.rebind_all_the_keys;

import com.minenash.rebind_all_the_keys.mixin.GameOptionsAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Scroller;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class RebindAllTheKeys implements ClientModInitializer {
	public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	public static final Map<Integer,Boolean> IS_MOUSE_DOWN = new HashMap<>(6);

	public static final InputUtil.Key SCROLL_UP = InputUtil.Type.MOUSE.createFromCode(100);
	public static final InputUtil.Key SCROLL_DOWN = InputUtil.Type.MOUSE.createFromCode(101);
	public static final InputUtil.Key SCROLL_LEFT = InputUtil.Type.MOUSE.createFromCode(102);
	public static final InputUtil.Key SCROLL_RIGHT = InputUtil.Type.MOUSE.createFromCode(103);

	public static boolean isAmecsInstalled = false;
	public static final SimpleOption<Boolean> macCommandToControl = SimpleOption.ofBoolean("rebind_all_the_keys.controls.cmdToCtrl", false);
	public static final SimpleOption<Boolean> doubleTapSprint = SimpleOption.ofBoolean("rebind_all_the_keys.controls.doubleTapSprint", true);
	public static final SimpleOption<Boolean> doubleTapFly = SimpleOption.ofBoolean("rebind_all_the_keys.controls.doubleTapFly", true);

	public enum SneakMode implements StringIdentifiable {
		HOLD("options.key.hold"), PERSISTENT("rebind_all_the_keys.key.persistent"), TOGGLE("options.key.toggle"), GROUNDED("rebind_all_the_keys.key.grounded");
		public final Text text;
		SneakMode(String k) { text=Text.translatable(k); }
		@Override public String asString() { return name(); }
	}
	public enum SprintMode implements StringIdentifiable {
		HOLD("options.key.hold"), PERSISTENT("rebind_all_the_keys.key.persistent"), TOGGLE("options.key.toggle");
		public final Text text;
		SprintMode(String k) { text=Text.translatable(k); }
		@Override public String asString() { return name(); }
	}

	public static final SimpleOption<SneakMode> expandedSneak = new SimpleOption<>("key.sneak", SimpleOption.emptyTooltip(),
			(optionText, value) -> value.text,
			new SimpleOption.PotentialValuesBasedCallbacks<>(List.of(SneakMode.values()), StringIdentifiable.createCodec(SneakMode::values)),
			SneakMode.HOLD,
			(value) -> CLIENT.options.getSneakToggled().setValue( value != SneakMode.HOLD ));
	public static final SimpleOption<SprintMode> expandedSprint = new SimpleOption<>("key.sprint", SimpleOption.emptyTooltip(),
			(optionText, value) -> value.text,
			new SimpleOption.PotentialValuesBasedCallbacks<>(List.of(SprintMode.values()), StringIdentifiable.createCodec(SprintMode::values)),
			SprintMode.PERSISTENT,
			(value) -> CLIENT.options.getSprintToggled().setValue(value == SprintMode.TOGGLE));

	public static boolean dontDisableSprint = false;

	public static final Map<Integer, Integer> DEBUG_REBINDS = new HashMap<>();

	public static final KeyBinding DEBUG_KEY = debugKeybind("debug_key", GLFW.GLFW_KEY_F3);
	public static final KeyBinding RELOAD_CHUNKS = debugKeybind("reload_chunks", GLFW.GLFW_KEY_A);
	public static final KeyBinding SHOW_HITBOXES = debugKeybind("show_hitboxes", GLFW.GLFW_KEY_B);
	public static final KeyBinding COPY_LOCATION = debugKeybind("copy_location", GLFW.GLFW_KEY_C);
	public static final KeyBinding CLEAR_CHAT = debugKeybind("clear_chat", GLFW.GLFW_KEY_D);
	public static final KeyBinding CYCLE_RENDER_DISTANCE = debugKeybind("cycle_render_distance", GLFW.GLFW_KEY_F);
	public static final KeyBinding SHOW_CHUNK_BOUNDARIES = debugKeybind("show_chunk_boundaries", GLFW.GLFW_KEY_G);
	public static final KeyBinding ADVANCE_TOOLTIPS = debugKeybind("advance_tooltips", GLFW.GLFW_KEY_H);
	public static final KeyBinding COPY_DATA_TO_CLIPBOARD = debugKeybind("copy_data_to_clipboard", GLFW.GLFW_KEY_I);
	public static final KeyBinding START_STOP_PROFILING = debugKeybind("start_stop_profiling", GLFW.GLFW_KEY_L);
	public static final KeyBinding SWAP_GAMEMODE = debugKeybind("swap_gamemode", GLFW.GLFW_KEY_N);
	public static final KeyBinding PAUSE_ON_LOST_FOCUS = debugKeybind("pause_on_lost_focus", GLFW.GLFW_KEY_P);
	public static final KeyBinding SHOW_DEBUG_BINDINGS = debugKeybind("show_debug_bindings", GLFW.GLFW_KEY_Q);
	public static final KeyBinding RELOAD_RESOURCES = debugKeybind("reload_resources", GLFW.GLFW_KEY_T);
	public static final KeyBinding GAMEMODE_SWITCHER = debugKeybind("gamemode_switcher", GLFW.GLFW_KEY_F4);
	public static final KeyBinding INTENTIONAL_CRASH = debugKeybind("intentional_crash", GLFW.GLFW_KEY_C);

	public static final KeyBinding DYNAMIC_TEXTURE_DUMP = debugKeybind("dynamic_texture_dump", GLFW.GLFW_KEY_S);
	public static final KeyBinding CHART_PIE = debugKeybind("profiler", GLFW.GLFW_KEY_1);
	public static final KeyBinding CHART_FPS_TPS = debugKeybind("tps_fps", GLFW.GLFW_KEY_2);
	public static final KeyBinding CHART_BANDWIDTH_PING = debugKeybind("bandwidth_ping", GLFW.GLFW_KEY_3);

	public static final KeyBinding QUIT_ALIAS = keybind("quit", GLFW.GLFW_KEY_UNKNOWN, "key.categories.ui");
	public static final KeyBinding SCREEN_PRIMARY = mousebind("screen_primary", 0, "key.categories.ui");
	public static final KeyBinding SCREEN_SECONDARY = mousebind("screen_secondary", 1, "key.categories.ui");
	public static final KeyBinding REFRESH_SERVER_LIST = keybind("refresh_server_list", GLFW.GLFW_KEY_F5, "key.categories.ui");

	public static final KeyBinding TOGGLE_HUD = miscKeybind("toggle_hud", GLFW.GLFW_KEY_F1);
	public static final KeyBinding TOGGLE_NARRATOR_OVERRIDE = miscKeybind("toggle_narrator_override", GLFW.GLFW_KEY_UNKNOWN);
	public static final KeyBinding TOGGLE_AUTO_JUMP = miscKeybind("toggle_auto_jump", GLFW.GLFW_KEY_UNKNOWN);

	public static final KeyBinding HOTBAR_NEXT_OVERRIDE = keybind("hotbar_next_override", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.INVENTORY_CATEGORY);
	public static final KeyBinding HOTBAR_PREVIOUS_OVERRIDE = keybind("hotbar_previous_override", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.INVENTORY_CATEGORY);
	public static final KeyBinding DROP_STACK_MODIFIER = keybind("drop_stack_modifier", GLFW.GLFW_KEY_LEFT_CONTROL, KeyBinding.INVENTORY_CATEGORY);
	public static final KeyBinding QUICK_MOVE = keybind("quick_move", GLFW.GLFW_KEY_LEFT_SHIFT, KeyBinding.INVENTORY_CATEGORY);

	public static final KeyBinding FLY = keybind("fly", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.MOVEMENT_CATEGORY);
	public static final KeyBinding DISMOUNT = keybind("dismount", GLFW.GLFW_KEY_LEFT_SHIFT, KeyBinding.MISC_CATEGORY);




	public static Text gamemodeSwitcherSelectText = null;

	private static KeyBinding debugKeybind(String key, int defaultKey) {
		return keybind(key, defaultKey, "rebind_all_the_keys.keybind_group.debug");
	}

	private static KeyBinding miscKeybind(String key, int defaultKey) {
		return keybind(key, defaultKey, KeyBinding.MISC_CATEGORY);
	}

	private static KeyBinding keybind(String key, int defaultKey, String category) {
		KeyBinding binding = new KeyBinding("rebind_all_the_keys.keybind." + key, InputUtil.Type.KEYSYM, defaultKey, category);
		KeyBindingHelper.registerKeyBinding(binding);
		return binding;
	}
	private static KeyBinding mousebind(String key, int defaultKey, String category) {
		KeyBinding binding = new KeyBinding("rebind_all_the_keys.keybind." + key, InputUtil.Type.MOUSE, defaultKey, category);
		KeyBindingHelper.registerKeyBinding(binding);
		return binding;
	}

	@Override
	public void onInitializeClient() {
		isAmecsInstalled = FabricLoader.getInstance().isModLoaded("amecs");

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			KeyBinding.setKeyPressed(SCROLL_UP, false);
			KeyBinding.setKeyPressed(SCROLL_DOWN, false);
			KeyBinding.setKeyPressed(SCROLL_LEFT, false);
			KeyBinding.setKeyPressed(SCROLL_RIGHT, false);

			while (TOGGLE_AUTO_JUMP.wasPressed()) {
				boolean value = !client.options.getAutoJump().getValue();
				client.options.getAutoJump().setValue(value);
				client.player.sendMessage(Text.translatable("rebind_all_the_keys.keybind.toggle_auto_jump.msg." + value), true);
			}

			if (client.player != null && client.currentScreen == null) {
				PlayerInventory inventory = client.player.getInventory();
				int selectedSlot = inventory.getSelectedSlot();
				int hotbarSize = PlayerInventory.getHotbarSize();

				while (HOTBAR_NEXT_OVERRIDE.wasPressed())
					selectedSlot = Scroller.scrollCycling(-1, selectedSlot, hotbarSize);

				while (HOTBAR_PREVIOUS_OVERRIDE.wasPressed())
					selectedSlot = Scroller.scrollCycling(1, selectedSlot, hotbarSize);

				inventory.setSelectedSlot(selectedSlot);
			}

			if (isKeybindPressed(DEBUG_KEY) && isKeybindPressed(CYCLE_RENDER_DISTANCE)) {
				SimpleOption<Integer> option = ((GameOptionsAccessor) client.options).getViewDistance();
				int newValue = option.getValue() + (Screen.hasShiftDown() ? -1 : 1);
				int max = Runtime.getRuntime().maxMemory() >= 1_000_000_000L ? 32 : 16;
				if (newValue < 2) newValue = 2;
				if (newValue > max) newValue = max;

				client.player.sendMessage(Text.literal("§e§l[Debug]:§f Set Render Distance to " + newValue), false);
				option.setValue(newValue);
			}


		});

	}

	public static void updateDebugKeybinds() {
		DEBUG_REBINDS.clear();
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(RELOAD_CHUNKS          ).getCode(), GLFW.GLFW_KEY_A);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(SHOW_HITBOXES          ).getCode(), GLFW.GLFW_KEY_B);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(COPY_LOCATION          ).getCode(), GLFW.GLFW_KEY_C);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(CLEAR_CHAT             ).getCode(), GLFW.GLFW_KEY_D);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(CYCLE_RENDER_DISTANCE  ).getCode(), GLFW.GLFW_KEY_F);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(SHOW_CHUNK_BOUNDARIES  ).getCode(), GLFW.GLFW_KEY_G);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(ADVANCE_TOOLTIPS       ).getCode(), GLFW.GLFW_KEY_H);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(COPY_DATA_TO_CLIPBOARD ).getCode(), GLFW.GLFW_KEY_I);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(START_STOP_PROFILING   ).getCode(), GLFW.GLFW_KEY_L);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(SWAP_GAMEMODE          ).getCode(), GLFW.GLFW_KEY_N);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(PAUSE_ON_LOST_FOCUS    ).getCode(), GLFW.GLFW_KEY_P);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(SHOW_DEBUG_BINDINGS    ).getCode(), GLFW.GLFW_KEY_Q);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(RELOAD_RESOURCES       ).getCode(), GLFW.GLFW_KEY_T);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(GAMEMODE_SWITCHER      ).getCode(), GLFW.GLFW_KEY_F4);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(DYNAMIC_TEXTURE_DUMP   ).getCode(), GLFW.GLFW_KEY_S);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(CHART_PIE              ).getCode(), GLFW.GLFW_KEY_1);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(CHART_FPS_TPS          ).getCode(), GLFW.GLFW_KEY_2);
		DEBUG_REBINDS.put(KeyBindingHelper.getBoundKeyOf(CHART_BANDWIDTH_PING   ).getCode(), GLFW.GLFW_KEY_3);

		if (gamemodeSwitcherSelectText != null)
			updateGamemodeSwitcherSelectText();

	}

	public static void updateGamemodeSwitcherSelectText() {
		gamemodeSwitcherSelectText = Text.translatable("debug.gamemodes.select_next",
				Text.literal("[").formatted(Formatting.AQUA).append(RebindAllTheKeys.GAMEMODE_SWITCHER.getBoundKeyLocalizedText()).append("]"));
	}

	public static String getDebugKeybindString(KeyBinding key) {
		String debugString = DEBUG_KEY.getBoundKeyLocalizedText().getString();

		if (debugString.length() == 1)
			debugString = debugString.toUpperCase();

		if (key == null)
			return debugString;

		String keyString = key.getBoundKeyLocalizedText().getString();
		return debugString + " + " + (keyString.length() == 1 ? keyString.toUpperCase() : keyString);
	}

	public static int getKeyCode(KeyBinding key) {
		return KeyBindingHelper.getBoundKeyOf(key).getCode();
	}

	public static boolean isKeybindPressed(KeyBinding key) {
		if (key.boundKey.type == InputUtil.Type.MOUSE)
			return IS_MOUSE_DOWN.getOrDefault(getKeyCode(key), false);
		return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), getKeyCode(key));
	}
}
