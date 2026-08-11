package com.lowdragmc.lowdraglib2.gui.holder;

import com.lowdragmc.lowdraglib2.editor.settings.AppearanceSettings;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModularUIScreen extends Screen {
    @Getter
    public final ModularUI modularUI;
    /**
     * Starting X position for the Gui. Inconsistent use for Gui backgrounds.
     */
    @Getter
    protected int leftPos;
    /**
     * Starting Y position for the Gui. Inconsistent use for Gui backgrounds.
     */
    @Getter
    protected int topPos;
    private int fontStyleElementSignature = Integer.MIN_VALUE;

    public ModularUIScreen(ModularUI modularUI, Component title) {
        super(title);
        this.modularUI = modularUI;
    }

    @Override
    public void init() {
        this.modularUI.setScreenAndInit(this);
        applyFontSettingsIfNeeded();
        this.addRenderableWidget(modularUI.getWidget());
        this.leftPos = (int) ((this.width - modularUI.getWidth()) / 2);
        this.topPos = (int) ((this.height - modularUI.getHeight()) / 2);
        super.init();
        // initial focus
        setFocused(modularUI.getWidget());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (!modularUI.onFilesDrop(paths.stream().map(Path::toFile).toList())) {
            super.onFilesDrop(paths);
        }
    }

    public void renderBackground(GuiGraphics guiGraphics) {
        // Standalone modular UIs should leave the world visible behind the interface.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        applyFontSettingsIfNeeded();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void applyFontSettingsIfNeeded() {
        // 原实现每帧对全部元素算 identityHashCode 链签名（大 UI 树下实测占帧时间 6%）；
        // 元素注册/注销计数已覆盖同一判定（签名只感知集合增减）。
        if (modularUI.getStructureEpoch() != fontStyleElementSignature) {
            AppearanceSettings.applyActiveFontSettings(modularUI);
            fontStyleElementSignature = modularUI.getStructureEpoch();
        }
    }
}
