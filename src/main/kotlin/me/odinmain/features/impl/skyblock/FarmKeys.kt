package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard

object FarmKeys: Module(
    name = "Farm Keys",
    description = "Temporarily changes your keybinds for farming in Skyblock.",
    category = Category.SKYBLOCK
) {
    private val blockBreakKey by KeybindSetting("Block breaking", Keyboard.KEY_NONE, "Changes the keybind for breaking blocks.")
    private val jumpKey by KeybindSetting("Jump", Keyboard.KEY_NONE, "Changes the keybind for jumping.")
    private val previousSensitivity by NumberSetting("Previous Sensitivity", 100f, 0f, 200f, description = "The sensitivity before enabling the module.")

    override fun onEnable() {
        updateKeyBindings(blockBreakKey.key, jumpKey.key, -1 / 3f)
        super.onEnable()
    }

    override fun onDisable() {
        updateKeyBindings(-100, 57, previousSensitivity / 200)
        super.onDisable()
    }

    private fun updateKeyBindings(breakKeyCode: Int, jumpKeyCode: Int, sensitivity: Float) {
        setKeyBindingState(mc.gameSettings.keyBindAttack, breakKeyCode)
        setKeyBindingState(mc.gameSettings.keyBindJump, jumpKeyCode)
        mc.gameSettings.mouseSensitivity = sensitivity
        mc.gameSettings.saveOptions()
        mc.gameSettings.loadOptions()
    }

    private fun setKeyBindingState(keyBinding: KeyBinding, keyCode: Int) {
        KeyBinding.setKeyBindState(keyBinding.keyCode, false)
        keyBinding.keyCode = keyCode
    }
}