package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {
    @Accessor("managed")
    boolean ldlib2$isManaged();
}
