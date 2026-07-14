package com.onefizz;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.OptionalDouble;

public final class EspRenderLayers {

    /** Lines that always show through walls (depth always passes). */
    public static final RenderLayer LINES_THROUGH_WALLS = RenderLayer.of(
        "onefizz_esp_lines",
        VertexFormats.LINES,
        VertexFormat.DrawMode.LINES,
        1536,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.LINES_PROGRAM)
            .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2.0)))
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .target(RenderPhase.ITEM_ENTITY_TARGET)
            .writeMaskState(RenderPhase.ALL_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
            .build(false));

    /** Translucent filled quads through walls. */
    public static final RenderLayer QUADS_THROUGH_WALLS = RenderLayer.of(
        "onefizz_esp_quads",
        VertexFormats.POSITION_COLOR,
        VertexFormat.DrawMode.QUADS,
        1536,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.COLOR_PROGRAM)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .target(RenderPhase.ITEM_ENTITY_TARGET)
            .writeMaskState(RenderPhase.COLOR_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
            .build(false));

    private EspRenderLayers() {}
}
