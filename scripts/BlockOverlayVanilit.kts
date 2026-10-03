name("BlockOverlay")
description("Renders an outline, fill or animated shader over the targeted block's actual shape")
requireApi(2)

val overlayMode = selectable(
    "Mode",
    "Outline",
    "Fill",
    "Outline + Fill",
    "Shader",
    selected = "Outline"
).id("block_overlay_mode")

val blocksOnly = checkBox("Blocks only", true)
    .id("block_overlay_blocks_only")

val outline3D = checkBox("Outline 3D", false)
    .id("block_overlay_outline_3d")
    .visibleWhen { overlayMode.value().name() == "Outline" }

val fill3D = checkBox("Fill 3D", false)
    .id("block_overlay_fill_3d")
    .visibleWhen { overlayMode.value().name() == "Fill" }

val outlineFill3D = checkBox("Outline + Fill 3D", false)
    .id("block_overlay_outline_fill_3d")
    .visibleWhen { overlayMode.value().name() == "Outline + Fill" }

val shader3D = checkBox("Shader 3D", false)
    .id("block_overlay_shader_3d")
    .visibleWhen { overlayMode.value().name() == "Shader" }

fun renderIn3D(): Boolean = when (overlayMode.value().name()) {
    "Outline" -> outline3D.value()
    "Fill" -> fill3D.value()
    "Outline + Fill" -> outlineFill3D.value()
    "Shader" -> shader3D.value()
    else -> false
}

val outlineColor = colorPicker("Outline Color", Colors.rgba(255, 255, 255, 255))
    .id("block_overlay_outline_color")
    .visibleWhen {
        val mode = overlayMode.value().name()
        mode == "Outline" || mode == "Outline + Fill"
    }

val outlineThickness = slider("Outline Thickness", 2, 1, 10)
    .id("block_overlay_outline_thickness")
    .visibleWhen {
        val mode = overlayMode.value().name()
        mode == "Outline" || mode == "Outline + Fill"
    }

val fillColor = colorPicker("Fill Color", Colors.rgba(70, 150, 255, 70))
    .id("block_overlay_fill_color")
    .visibleWhen {
        val mode = overlayMode.value().name()
        mode == "Fill" || mode == "Outline + Fill"
    }

val modeGlow = checkBox("Glow", false)
    .id("block_overlay_mode_glow")
    .visibleWhen { overlayMode.value().name() != "Shader" }

val shader = selectable(
    "Shader",
    "Dots",
    "Ink Smoke",
    "Plasma",
    "Pixel",
    selected = "Dots"
).id("block_overlay_shader")
    .visibleWhen { overlayMode.value().name() == "Shader" }

val translucency = slider("translucency", 1.0f, 0.0f, 1.0f, 0.05f)
    .id("block_overlay_translucency")
    .visibleWhen { overlayMode.value().name() == "Shader" }

fun shaderSelected(name: String): Boolean =
    overlayMode.value().name() == "Shader" && shader.value().name() == name

val dotColor = colorPicker("Dot Color", Colors.rgba(255, 255, 255, 255))
    .id("block_overlay_dot_color")
    .visibleWhen { shaderSelected("Dots") }

val backgroundColor = colorPicker("Background", Colors.rgba(166, 0, 255, 51))
    .id("block_overlay_background_color")
    .visibleWhen { shaderSelected("Dots") }

val shaderSpeed = slider("Dot Speed", 1.15f, 0.0f, 3.0f, 0.05f)
    .id("block_overlay_shader_speed")
    .visibleWhen { shaderSelected("Dots") }

val brightness = slider("Brightness", 3.0f, 0.0f, 3.0f, 0.05f)
    .id("block_overlay_brightness")
    .visibleWhen { shaderSelected("Dots") }

val dotSize = slider("Dot Size", 2.50f, 0.40f, 2.50f, 0.05f)
    .id("block_overlay_dot_size")
    .visibleWhen { shaderSelected("Dots") }

val gridDensity = slider("Grid Density", 2.50f, 0.40f, 6.0f, 0.05f)
    .id("block_overlay_grid_density")
    .visibleWhen { shaderSelected("Dots") }

val patternScale = slider("Pattern Scale", 1.65f, 0.35f, 4.0f, 0.05f)
    .id("block_overlay_pattern_scale")
    .visibleWhen { shaderSelected("Dots") }

val inkSpeed = slider("Speed", 1.15f, 0.0f, 3.0f, 0.05f)
    .id("block_overlay_ink_speed")
    .visibleWhen { shaderSelected("Ink Smoke") }

val inkScale = slider("Ink Scale", 3.45f, 0.3f, 6.0f, 0.05f)
    .id("block_overlay_ink_scale")
    .visibleWhen { shaderSelected("Ink Smoke") }

val inkWarp = slider("Warp", 4.0f, 0.0f, 8.0f, 0.1f)
    .id("block_overlay_ink_warp")
    .visibleWhen { shaderSelected("Ink Smoke") }

val inkHighlight = slider("Highlight", 1.0f, 0.0f, 3.0f, 0.05f)
    .id("block_overlay_ink_highlight")
    .visibleWhen { shaderSelected("Ink Smoke") }

val inkDownscale = selectable(
    "Ink Downscale",
    "1x",
    "x2",
    "x4",
    "x8",
    selected = "1x"
).id("block_overlay_ink_downscale")
    .visibleWhen { shaderSelected("Ink Smoke") }

val inkColor1 = colorPicker("Ink 1", Colors.rgba(0, 0, 0, 255))
    .id("block_overlay_ink_color_1")
    .visibleWhen { shaderSelected("Ink Smoke") }

val inkColor2 = colorPicker("Ink 2", Colors.rgba(255, 255, 255, 255))
    .id("block_overlay_ink_color_2")
    .visibleWhen { shaderSelected("Ink Smoke") }

val inkColor3 = colorPicker("Ink 3", Colors.rgba(31, 59, 27, 255))
    .id("block_overlay_ink_color_3")
    .visibleWhen { shaderSelected("Ink Smoke") }

val inkColor4 = colorPicker("Ink 4", Colors.rgba(0, 77, 102, 255))
    .id("block_overlay_ink_color_4")
    .visibleWhen { shaderSelected("Ink Smoke") }

val inkGlow = colorPicker("Ink Glow", Colors.rgba(51, 81, 102, 255))
    .id("block_overlay_ink_glow")
    .visibleWhen { shaderSelected("Ink Smoke") }

val plasmaScale = slider("Scale", 6.0f, 1.0f, 6.0f, 0.05f)
    .id("block_overlay_plasma_scale")
    .visibleWhen { shaderSelected("Plasma") }

val plasmaIntensity = slider("Intensity", 1.0f, 0.5f, 2.5f, 0.05f)
    .id("block_overlay_plasma_intensity")
    .visibleWhen { shaderSelected("Plasma") }

val plasmaDistortion = slider("Distortion", 4.0f, 0.0f, 4.0f, 0.05f)
    .id("block_overlay_plasma_distortion")
    .visibleWhen { shaderSelected("Plasma") }

val plasmaDownscale1x = entry("1x", true)
val plasmaDownscale2x = entry("x2")
val plasmaDownscale4x = entry("x4")
val plasmaDownscale8x = entry("x8")
val plasmaDownscale = selectable(
    "Downscale",
    plasmaDownscale1x,
    plasmaDownscale2x,
    plasmaDownscale4x,
    plasmaDownscale8x
).visibleWhen { shaderSelected("Plasma") }

val plasmaColor1 = colorPicker("Color 1", Colors.rgba(26, 5, 0, 255))
    .id("block_overlay_plasma_color_1")
    .visibleWhen { shaderSelected("Plasma") }

val plasmaColor2 = colorPicker("Color 2", Colors.rgba(90, 18, 8, 255))
    .id("block_overlay_plasma_color_2")
    .visibleWhen { shaderSelected("Plasma") }

val plasmaColor3 = colorPicker("Color 3", Colors.rgba(196, 74, 32, 255))
    .id("block_overlay_plasma_color_3")
    .visibleWhen { shaderSelected("Plasma") }

val plasmaColor4 = colorPicker("Color 4", Colors.rgba(240, 138, 58, 255))
    .id("block_overlay_plasma_color_4")
    .visibleWhen { shaderSelected("Plasma") }

val plasmaColor5 = colorPicker("Color 5", Colors.rgba(255, 197, 122, 255))
    .id("block_overlay_plasma_color_5")
    .visibleWhen { shaderSelected("Plasma") }

val pixelStyle = selectable(
    "Style",
    "Wave",
    "Noise",
    "Warp",
    selected = "Wave"
).id("block_overlay_pixel_style")
    .visibleWhen { shaderSelected("Pixel") }

val pixelWaveSpeed = slider("Pixel Speed", 0.6f, 0.0f, 2.0f, 0.05f)
    .id("block_overlay_pixel_wave_speed")
    .visibleWhen { shaderSelected("Pixel") }

val pixelWaveSize = slider("Pixel Size", 3, 1, 8)
    .id("block_overlay_pixel_wave_size")
    .visibleWhen { shaderSelected("Pixel") }

val pixelWavePatternScale = slider("Pixel Pattern Scale", 2.0f, 1.0f, 4.0f, 0.25f)
    .id("block_overlay_pixel_wave_pattern_scale")
    .visibleWhen { shaderSelected("Pixel") }

val pixelWaveBackColor = colorPicker("Pixel Background", Colors.rgba(0, 17, 34, 255))
    .id("block_overlay_pixel_wave_back_color")
    .visibleWhen { shaderSelected("Pixel") }

val pixelWaveFrontColor = colorPicker("Pixel Pattern", Colors.rgba(255, 0, 136, 255))
    .id("block_overlay_pixel_wave_front_color")
    .visibleWhen { shaderSelected("Pixel") }

val fadeTicks = slider("In/Out Animation", 0, 0, 20)
    .id("block_overlay_fade_ticks")

val linkMultiblocks = checkBox("Link Multiblocks", false)
    .id("block_overlay_link_multiblocks")

val blockSwapAnimation = checkBox("Block Swap Animation", false)
    .id("block_overlay_block_swap")

val swapDuration = slider("Swap Duration", 8, 0, 20)
    .id("block_overlay_swap_duration")
    .visibleWhen { blockSwapAnimation.value() }

val swapInterpolation = selectable(
    "Swap Interpolation",
    "Linear",
    "Slowdown",
    "Speed-up",
    selected = "Linear"
).id("block_overlay_swap_interpolation")
    .visibleWhen { blockSwapAnimation.value() }

val BLOCK_REACH = 4.5
val OVERLAY_EXPAND = 0.002

val PLANT_BLOCK_TAGS = setOf(
    "flowers",
    "small_flowers",
    "tall_flowers",
    "crops",
    "saplings",
    "leaves",
    "replaceable_by_trees",
    "bee_growables",
    "wart_blocks"
)

val PLANT_BLOCK_IDS = setOf(
    "short_grass", "tall_grass", "short_dry_grass", "tall_dry_grass",
    "fern", "large_fern", "dead_bush", "bush", "firefly_bush",
    "azalea", "flowering_azalea", "mangrove_propagule",
    "pink_petals", "wildflowers", "leaf_litter", "spore_blossom",
    "hanging_roots", "moss_block", "moss_carpet",
    "pale_moss_block", "pale_moss_carpet", "pale_hanging_moss",
    "vine", "glow_lichen", "weeping_vines", "weeping_vines_plant",
    "twisting_vines", "twisting_vines_plant", "cave_vines", "cave_vines_plant",
    "seagrass", "tall_seagrass", "kelp", "kelp_plant", "lily_pad", "sea_pickle",
    "sugar_cane", "cactus", "cactus_flower", "bamboo", "bamboo_sapling",
    "small_dripleaf", "big_dripleaf", "big_dripleaf_stem",
    "chorus_plant", "chorus_flower",
    "brown_mushroom", "red_mushroom", "mushroom_stem",
    "crimson_fungus", "warped_fungus",
    "crimson_roots", "warped_roots", "nether_sprouts", "nether_wart",
    "wheat", "carrots", "potatoes", "beetroots", "cocoa",
    "melon_stem", "attached_melon_stem", "pumpkin_stem", "attached_pumpkin_stem",
    "sweet_berry_bush", "torchflower_crop", "pitcher_crop", "pitcher_plant",
    "open_eyeblossom", "closed_eyeblossom"
)

fun isPlantBlock(block: Block): Boolean {
    val id = block.id().substringAfter(':')
    if (id in PLANT_BLOCK_IDS || id.startsWith("potted_")) return true
    if (id.startsWith("chorus_") || id.contains("_coral")) return true
    if (id.endsWith("_sapling") || id.endsWith("_leaves")) return true
    if (id.endsWith("_flower") || id.endsWith("_tulip") || id.endsWith("_orchid")) return true
    if (id.contains("mushroom") || id.endsWith("_fungus") || id.endsWith("_roots")) return true
    if (id.endsWith("_vines") || id.endsWith("_vine") || id.endsWith("_seagrass")) return true

    return block.tags().any { tag ->
        tag.removePrefix("#").substringAfter(':') in PLANT_BLOCK_TAGS
    }
}

data class OverlayPosition(val x: Int, val y: Int, val z: Int)

data class FadingOverlay(
    var previousOpacity: Float = 0f,
    var opacity: Float = 0f,
    var cachedBoxes: List<Box> = emptyList(),
    var cachedPattern: Box? = null
)

val fadingOverlays = mutableMapOf<OverlayPosition, FadingOverlay>()

var lastPrimaryTarget: OverlayPosition? = null
var swapActive = false
var swapFromX = 0.0
var swapFromY = 0.0
var swapFromZ = 0.0
var swapToX = 0.0
var swapToY = 0.0
var swapToZ = 0.0
var swapProgress = 0f
var swapPreviousProgress = 0f

fun interpolateSwap(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return when (swapInterpolation.value().name()) {
        "Slowdown" -> 1f - (1f - x) * (1f - x)
        "Speed-up" -> x * x * 0.55f + x * 0.45f
        else -> x
    }
}

fun currentSwapOffset(partialTick: Float): Triple<Double, Double, Double> {
    if (!swapActive || !blockSwapAnimation.value() || swapDuration.intValue() <= 0) {
        return Triple(0.0, 0.0, 0.0)
    }
    val raw = (swapPreviousProgress + (swapProgress - swapPreviousProgress) * partialTick).coerceIn(0f, 1f)
    val t = interpolateSwap(raw)
    val ox = (swapFromX - swapToX) * (1.0 - t.toDouble())
    val oy = (swapFromY - swapToY) * (1.0 - t.toDouble())
    val oz = (swapFromZ - swapToZ) * (1.0 - t.toDouble())
    return Triple(ox, oy, oz)
}

fun offsetBoxes(boxes: List<Box>, ox: Double, oy: Double, oz: Double): List<Box> {
    if (ox == 0.0 && oy == 0.0 && oz == 0.0) return boxes
    return boxes.map { it.offset(ox, oy, oz) }
}

fun refreshOverlayCache(position: OverlayPosition, state: FadingOverlay) {
    val boxes = overlayBoxes(position)
    if (boxes.isNotEmpty()) {
        state.cachedBoxes = boxes
        state.cachedPattern = structurePatternBox(position)
    }
}

fun colorOpacity(color: Int, opacity: Float): Int = Colors.rgba(
    (color ushr 16) and 255,
    (color ushr 8) and 255,
    color and 255,
    (((color ushr 24) and 255) * opacity.coerceIn(0f, 1f)).toInt()
)

fun drawThickOutline(
    render: Render3D,
    box: Box,
    color: Int,
    thickness: Int,
    throughWalls: Boolean
) {
    val amount = thickness.coerceIn(1, 10)
    val halfWidth = 0.0025 + (amount - 1) * (0.0100 / 9.0)
    val minX = box.minX()
    val minY = box.minY()
    val minZ = box.minZ()
    val maxX = box.maxX()
    val maxY = box.maxY()
    val maxZ = box.maxZ()

    fun solidEdge(
        edgeMinX: Double,
        edgeMinY: Double,
        edgeMinZ: Double,
        edgeMaxX: Double,
        edgeMaxY: Double,
        edgeMaxZ: Double
    ) {
        render.filledBox(
            Box(edgeMinX, edgeMinY, edgeMinZ, edgeMaxX, edgeMaxY, edgeMaxZ),
            color,
            throughWalls
        )
    }

    solidEdge(minX - halfWidth, minY - halfWidth, minZ - halfWidth, maxX + halfWidth, minY + halfWidth, minZ + halfWidth)
    solidEdge(minX - halfWidth, minY - halfWidth, maxZ - halfWidth, maxX + halfWidth, minY + halfWidth, maxZ + halfWidth)
    solidEdge(minX - halfWidth, maxY - halfWidth, minZ - halfWidth, maxX + halfWidth, maxY + halfWidth, minZ + halfWidth)
    solidEdge(minX - halfWidth, maxY - halfWidth, maxZ - halfWidth, maxX + halfWidth, maxY + halfWidth, maxZ + halfWidth)

    solidEdge(minX - halfWidth, minY - halfWidth, minZ - halfWidth, minX + halfWidth, maxY + halfWidth, minZ + halfWidth)
    solidEdge(minX - halfWidth, minY - halfWidth, maxZ - halfWidth, minX + halfWidth, maxY + halfWidth, maxZ + halfWidth)
    solidEdge(maxX - halfWidth, minY - halfWidth, minZ - halfWidth, maxX + halfWidth, maxY + halfWidth, minZ + halfWidth)
    solidEdge(maxX - halfWidth, minY - halfWidth, maxZ - halfWidth, maxX + halfWidth, maxY + halfWidth, maxZ + halfWidth)

    solidEdge(minX - halfWidth, minY - halfWidth, minZ - halfWidth, minX + halfWidth, minY + halfWidth, maxZ + halfWidth)
    solidEdge(minX - halfWidth, maxY - halfWidth, minZ - halfWidth, minX + halfWidth, maxY + halfWidth, maxZ + halfWidth)
    solidEdge(maxX - halfWidth, minY - halfWidth, minZ - halfWidth, maxX + halfWidth, minY + halfWidth, maxZ + halfWidth)
    solidEdge(maxX - halfWidth, maxY - halfWidth, minZ - halfWidth, maxX + halfWidth, maxY + halfWidth, maxZ + halfWidth)
}

val blockShaderVertexSource = """
    #version 330
    layout(location=0) in vec3 pos;
    layout(location=1) in vec3 local_pos;
    out vec3 block_pos;
    uniform mat4 u_view;
    uniform mat4 u_projection;
    void main() {
        block_pos = local_pos;
        gl_Position = u_projection * u_view * vec4(pos, 1.0);
    }
"""

val blockGlowGpuShader = shader(
    "block-overlay-smooth-glow-3d",
    """
        #version 330
        in vec3 block_pos;
        out vec4 out_color;
        uniform vec4 u_color;
        uniform float u_radius;
        uniform float u_opacity;

        float rectangleDistance(vec2 p) {
            vec2 q = abs(p - 0.5) - 0.5;
            return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0);
        }

        void main() {
            vec3 face_normal = abs(normalize(cross(dFdx(block_pos), dFdy(block_pos))));
            vec2 uv;
            if (face_normal.x >= face_normal.y && face_normal.x >= face_normal.z) uv = block_pos.zy;
            else if (face_normal.y >= face_normal.z) uv = block_pos.xz;
            else uv = block_pos.xy;

            float edge_distance = abs(rectangleDistance(uv));
            float radius = max(u_radius, 0.0001);
            float glow = 1.0 - smoothstep(0.0, radius, edge_distance);
            glow = glow * glow * (3.0 - 2.0 * glow);
            if (glow <= 0.001) discard;
            out_color = vec4(u_color.rgb, u_color.a * u_opacity * glow);
        }
    """,
    blockShaderVertexSource
)

val inkSmokeGpuShader = shader(
    "block-overlay-ink-smoke-3d",
    """
        #version 330
        in vec3 block_pos;
        out vec4 out_color;
        uniform float u_time;
        uniform float u_speed;
        uniform float u_scale;
        uniform float u_warp;
        uniform float u_highlight;
        uniform float u_opacity;
        uniform float u_downscale;
        uniform vec3 u_ink1;
        uniform vec3 u_ink2;
        uniform vec3 u_ink3;
        uniform vec3 u_ink4;
        uniform vec3 u_glow;
        float hash31(vec3 p) {
            p = fract(p * 0.1031);
            p += dot(p, p.yzx + 33.33);
            return fract((p.x + p.y) * p.z);
        }
        float vnoise(vec3 p) {
            vec3 i = floor(p);
            vec3 f = fract(p);
            vec3 u = f * f * (3.0 - 2.0 * f);
            float n000 = hash31(i + vec3(0.0, 0.0, 0.0));
            float n100 = hash31(i + vec3(1.0, 0.0, 0.0));
            float n010 = hash31(i + vec3(0.0, 1.0, 0.0));
            float n110 = hash31(i + vec3(1.0, 1.0, 0.0));
            float n001 = hash31(i + vec3(0.0, 0.0, 1.0));
            float n101 = hash31(i + vec3(1.0, 0.0, 1.0));
            float n011 = hash31(i + vec3(0.0, 1.0, 1.0));
            float n111 = hash31(i + vec3(1.0, 1.0, 1.0));
            float z0 = mix(mix(n000, n100, u.x), mix(n010, n110, u.x), u.y);
            float z1 = mix(mix(n001, n101, u.x), mix(n011, n111, u.x), u.y);
            return mix(z0, z1, u.z);
        }
        float fbm(vec3 p) {
            float value = 0.0;
            float amplitude = 0.5;
            int octaves = 5;
            if (u_downscale >= 7.5) octaves = 2;
            else if (u_downscale >= 3.5) octaves = 3;
            else if (u_downscale >= 1.5) octaves = 4;
            for (int i = 0; i < 5; i++) {
                if (i >= octaves) break;
                value += amplitude * vnoise(p);
                p *= 2.0;
                amplitude *= 0.5;
            }
            return value;
        }
        void main() {
            vec3 sample_pos = block_pos;
            if (u_downscale > 1.0) {
                vec2 sample_pixel = floor(gl_FragCoord.xy / u_downscale) * u_downscale + u_downscale * 0.5;
                vec2 pixel_offset = sample_pixel - gl_FragCoord.xy;
                sample_pos += dFdx(block_pos) * pixel_offset.x + dFdy(block_pos) * pixel_offset.y;
            }
            vec3 p = (sample_pos * 2.0 - 1.0) * max(u_scale, 0.0001);
            float t = u_time * u_speed * 0.2;
            vec3 q = vec3(
                fbm(p + vec3(t * 0.4, t * 0.3, t * 0.2)),
                fbm(p + vec3(t * 0.2, t * 0.4, -t * 0.3)),
                fbm(p + vec3(-t * 0.3, t * 0.2, t * 0.4))
            );
            vec3 r = vec3(
                fbm(p + q * u_warp + vec3(1.7, 9.2, 4.6) + t * 0.15),
                fbm(p + q * u_warp + vec3(8.3, 2.8, 6.1) - t * 0.1),
                fbm(p + q * u_warp + vec3(3.5, 7.4, 1.9) + t * 0.12)
            );
            float f = fbm(p + r * 2.0);
            vec3 color = mix(u_ink1, u_ink2, clamp(f * 2.0, 0.0, 1.0));
            color = mix(color, u_ink3, clamp(q.x * 1.5, 0.0, 1.0));
            color = mix(color, u_ink4, clamp(r.y * 0.8, 0.0, 1.0));
            float wisp = pow(clamp(f * 1.5, 0.0, 1.0), 3.0);
            color += u_glow * wisp * u_highlight;
            out_color = vec4(color, u_opacity);
        }
    """,
    blockShaderVertexSource
)

val dotsGpuShader = shader(
    "block-overlay-dots-3d",
    """
        #version 330
        in vec3 block_pos;
        out vec4 out_color;
        uniform float u_time;
        uniform float u_speed;
        uniform float u_brightness;
        uniform float u_dot_size;
        uniform float u_grid_density;
        uniform float u_pattern_scale;
        uniform float u_opacity;
        uniform vec4 u_dot_color;
        uniform vec4 u_background;
        void main() {
            vec3 sample_pos = block_pos;
            vec3 centred = sample_pos - 0.5;
            vec3 face_normal = abs(normalize(cross(dFdx(block_pos), dFdy(block_pos))));
            vec2 uv;
            if (face_normal.x >= face_normal.y && face_normal.x >= face_normal.z) uv = sample_pos.zy;
            else if (face_normal.y >= face_normal.z) uv = sample_pos.xz;
            else uv = sample_pos.xy;
            float cells = floor(clamp(8.0 + u_grid_density * 8.0, 11.0, 56.0));
            vec2 cell = fract(uv * cells) - 0.5;
            float radius = 0.075 * u_dot_size;
            float distance_to_dot = length(cell);
            float antialias = max(fwidth(distance_to_dot), 0.002);
            float dot_mask = 1.0 - smoothstep(radius - antialias, radius + antialias, distance_to_dot);
            float scale = u_pattern_scale;
            float time = u_time * u_speed;
            float noise = (
                sin(centred.x * 3.0 * scale + time * 0.4) * cos(centred.y * 3.0 * scale - time * 0.35) +
                sin(centred.y * 3.0 * scale + time * 0.4) * cos(centred.z * 3.0 * scale - time * 0.35) +
                sin(centred.z * 3.0 * scale + time * 0.4) * cos(centred.x * 3.0 * scale - time * 0.35)
            ) / 3.0;
            noise += 0.5 * (
                sin(centred.x * 7.0 * scale - time * 0.6) * sin(centred.y * 7.0 * scale + time * 0.55) +
                sin(centred.y * 7.0 * scale - time * 0.6) * sin(centred.z * 7.0 * scale + time * 0.55) +
                sin(centred.z * 7.0 * scale - time * 0.6) * sin(centred.x * 7.0 * scale + time * 0.55)
            ) / 3.0;
            float fronts = sin(noise * 6.0 + length(centred) * 8.0 * scale - time * 1.8);
            float intensity = clamp(0.1 + pow(max(fronts, 0.0), 1.8), 0.1, 1.0);
            vec3 foreground = clamp(u_dot_color.rgb * u_brightness, 0.0, 1.0);
            vec3 dot_rgb = mix(u_background.rgb, foreground, intensity);
            vec3 color = mix(u_background.rgb, dot_rgb, dot_mask);
            float alpha = mix(u_background.a, u_dot_color.a, dot_mask) * u_opacity;
            out_color = vec4(color, alpha);
        }
    """,
    blockShaderVertexSource
)

val plasmaGpuShader = shader(
    "block-overlay-plasma-3d",
    """
        #version 330
        in vec3 block_pos;
        out vec4 out_color;
        uniform float u_time;
        uniform float u_scale;
        uniform float u_intensity;
        uniform float u_distortion;
        uniform float u_downscale;
        uniform float u_opacity;
        uniform vec3 u_color1;
        uniform vec3 u_color2;
        uniform vec3 u_color3;
        uniform vec3 u_color4;
        uniform vec3 u_color5;

        float hash31(vec3 p) {
            p = fract(p * vec3(0.1031, 0.1030, 0.0973));
            p += dot(p, p.yzx + 33.33);
            return fract((p.x + p.y) * p.z);
        }

        float vnoise(vec3 p) {
            vec3 i = floor(p);
            vec3 f = fract(p);
            vec3 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
            float n000 = hash31(i + vec3(0.0, 0.0, 0.0));
            float n100 = hash31(i + vec3(1.0, 0.0, 0.0));
            float n010 = hash31(i + vec3(0.0, 1.0, 0.0));
            float n110 = hash31(i + vec3(1.0, 1.0, 0.0));
            float n001 = hash31(i + vec3(0.0, 0.0, 1.0));
            float n101 = hash31(i + vec3(1.0, 0.0, 1.0));
            float n011 = hash31(i + vec3(0.0, 1.0, 1.0));
            float n111 = hash31(i + vec3(1.0, 1.0, 1.0));
            float z0 = mix(mix(n000, n100, u.x), mix(n010, n110, u.x), u.y);
            float z1 = mix(mix(n001, n101, u.x), mix(n011, n111, u.x), u.y);
            return mix(z0, z1, u.z);
        }

        float fbm(vec3 p) {
            float value = vnoise(p) * 0.533333;
            value += vnoise(p * 2.0) * 0.266667;
            value += vnoise(p * 4.0) * 0.133333;
            value += vnoise(p * 8.0) * 0.066667;
            return value;
        }

        vec3 palette(float t) {
            t = clamp(t, 0.0, 1.0);
            if (t < 0.25) return mix(u_color1, u_color2, smoothstep(0.0, 0.25, t));
            if (t < 0.50) return mix(u_color2, u_color3, smoothstep(0.25, 0.50, t));
            if (t < 0.75) return mix(u_color3, u_color4, smoothstep(0.50, 0.75, t));
            return mix(u_color4, u_color5, smoothstep(0.75, 1.0, t));
        }

        void main() {
            vec3 sample_pos = block_pos;
            if (u_downscale > 1.0) {
                vec2 sample_pixel = floor(gl_FragCoord.xy / u_downscale) * u_downscale + u_downscale * 0.5;
                vec2 pixel_offset = sample_pixel - gl_FragCoord.xy;
                sample_pos += dFdx(block_pos) * pixel_offset.x + dFdy(block_pos) * pixel_offset.y;
            }
            vec3 p = (sample_pos - 0.5) * max(u_scale, 0.0001) * 0.72;
            float t = u_time;
            vec3 drift = vec3(t * 0.11, -t * 0.08, t * 0.09);
            float warp1 = fbm(p * 0.55 + drift);
            float warp2 = fbm(p * 0.55 + vec3(5.2, 1.3, 7.1) - drift * 0.73);
            float warp3 = fbm(p * 0.55 + vec3(2.7, 8.4, 3.6) + drift * 0.57);
            vec3 warped = p + (vec3(warp1, warp2, warp3) - 0.5) * u_distortion * 0.75;
            float value = 0.0;
            value += sin(warped.x * 1.75 + t * 0.7);
            value += sin(warped.y * 1.95 + t * 0.9);
            value += sin(warped.z * 1.85 - t * 0.8);
            value += sin((warped.x + warped.y + warped.z) * 1.15 + t * 0.5);
            value = value * 0.125 + 0.5;
            value += (fbm(warped * 0.65 + drift * 1.4) - 0.5) * u_distortion * 0.12;
            value = clamp(0.5 + (value - 0.5) * u_intensity, 0.0, 1.0);
            value = smoothstep(0.0, 1.0, value);
            vec3 color = palette(value);
            color += pow(value, 5.0) * 0.18;
            out_color = vec4(color, u_opacity);
        }
    """,
    blockShaderVertexSource
)

val pixelWaveGpuShader = shader(
    "block-overlay-pixel-wave-3d",
    """
        #version 330
        in vec3 block_pos;
        out vec4 out_color;
        uniform float u_time;
        uniform float u_speed;
        uniform float u_style;
        uniform float u_pixel_size;
        uniform float u_pattern_scale;
        uniform float u_opacity;
        uniform vec4 u_back_color;
        uniform vec4 u_front_color;

        const float TAU = 6.28318530718;

        float bayer8(ivec2 pixel) {
            const int matrix[64] = int[64](
                 0, 48, 12, 60,  3, 51, 15, 63,
                32, 16, 44, 28, 35, 19, 47, 31,
                 8, 56,  4, 52, 11, 59,  7, 55,
                40, 24, 36, 20, 43, 27, 39, 23,
                 2, 50, 14, 62,  1, 49, 13, 61,
                34, 18, 46, 30, 33, 17, 45, 29,
                10, 58,  6, 54,  9, 57,  5, 53,
                42, 26, 38, 22, 41, 25, 37, 21
            );
            ivec2 p = ivec2(pixel.x & 7, pixel.y & 7);
            return (float(matrix[p.y * 8 + p.x]) + 0.5) / 64.0;
        }

        float bayer4(ivec2 pixel) {
            const int matrix[16] = int[16](
                 0,  8,  2, 10,
                12,  4, 14,  6,
                 3, 11,  1,  9,
                15,  7, 13,  5
            );
            ivec2 p = ivec2(pixel.x & 3, pixel.y & 3);
            return (float(matrix[p.y * 4 + p.x]) + 0.5) / 16.0;
        }

        float hash31(vec3 p) {
            p = fract(p * vec3(0.1031, 0.1030, 0.0973));
            p += dot(p, p.yzx + 33.33);
            return fract((p.x + p.y) * p.z);
        }

        float vnoise(vec3 p) {
            vec3 i = floor(p);
            vec3 f = fract(p);
            vec3 u = f * f * (3.0 - 2.0 * f);
            float n000 = hash31(i + vec3(0.0, 0.0, 0.0));
            float n100 = hash31(i + vec3(1.0, 0.0, 0.0));
            float n010 = hash31(i + vec3(0.0, 1.0, 0.0));
            float n110 = hash31(i + vec3(1.0, 1.0, 0.0));
            float n001 = hash31(i + vec3(0.0, 0.0, 1.0));
            float n101 = hash31(i + vec3(1.0, 0.0, 1.0));
            float n011 = hash31(i + vec3(0.0, 1.0, 1.0));
            float n111 = hash31(i + vec3(1.0, 1.0, 1.0));
            float z0 = mix(mix(n000, n100, u.x), mix(n010, n110, u.x), u.y);
            float z1 = mix(mix(n001, n101, u.x), mix(n011, n111, u.x), u.y);
            return mix(z0, z1, u.z);
        }

        float fbm(vec3 p) {
            float value = 0.0;
            float amplitude = 0.533333;
            for (int i = 0; i < 4; i++) {
                value += vnoise(p) * amplitude;
                p = p * 2.03 + vec3(1.7, 3.1, 2.3);
                amplitude *= 0.5;
            }
            return value;
        }

        void main() {
            // Quantize in screen space like the original pxSize=3 component.
            // The local 3D position is reconstructed at the centre of the same
            // pixel cell, keeping the wave stable across neighbouring faces.
            float pixel_size = max(u_pixel_size, 1.0);
            vec2 pixel_id = floor(gl_FragCoord.xy / pixel_size);
            vec2 sample_pixel = (pixel_id + 0.5) * pixel_size;
            vec2 pixel_offset = sample_pixel - gl_FragCoord.xy;
            vec3 sample_pos = block_pos
                + dFdx(block_pos) * pixel_offset.x
                + dFdy(block_pos) * pixel_offset.y;

            float scale = max(u_pattern_scale, 0.25);
            float phase = u_time * u_speed * 0.16;

            float coverage;
            float hole_mask = 0.0;
            if (u_style < 0.5) {
                // The crest and the complete dither transition stay below y=1,
                // so Wave can never leak onto the upper face of the block.
                float crest = 0.50
                    + sin(TAU * (sample_pos.x * 0.72 * scale + phase)) * 0.13
                    + sin(TAU * (sample_pos.z * 0.58 * scale - phase * 0.81) + 1.35) * 0.09
                    + sin(TAU * ((sample_pos.x + sample_pos.z) * 0.31 * scale + phase * 0.37)) * 0.05;
                crest = min(crest, 0.72);
                float signed_wave = crest - sample_pos.y;
                coverage = smoothstep(-0.22, 0.22, signed_wave);
            } else if (u_style < 1.5) {
                // Smooth domain-warped noise rocks back and forth instead of
                // drifting forever. It is sampled in 3D to join at cube edges.
                float sway = sin(phase * TAU);
                float sway2 = cos(phase * TAU * 0.73);
                vec3 p = (sample_pos - 0.5) * (2.2 + scale * 0.9);
                vec3 drift = vec3(sway * 0.85, sway2 * 0.65, -sway * 0.72);
                float q = fbm(p + drift);
                float r = fbm(p + vec3(q * 2.4, -q * 1.8, q * 2.1) - drift * 0.61);
                float smoke = mix(q, r, 0.58);
                coverage = smoothstep(0.24, 0.78, smoke);
                float patch_field = fbm(p * 0.43 - drift * 0.37 + vec3(4.7, 1.3, 8.2));
                // Fixed equivalent of the former Patchiness slider at 0.1
                // (the UI value was normalized to an internal maximum of 0.65).
                float patchiness = 0.065;
                float hole_threshold = patchiness;
                hole_mask = 1.0 - smoothstep(
                    hole_threshold - 0.045,
                    hole_threshold + 0.045,
                    patch_field
                );
                hole_mask *= smoothstep(0.0, 0.08, patchiness);
            } else {
                // A fast tunnel-like domain warp, adapted from the Warp preset.
                float drive = u_time * u_speed * 0.8;
                vec3 p = (sample_pos - 0.5) * (2.6 + scale * 0.8);
                vec3 motion = vec3(
                    sin(drive * 0.71),
                    cos(drive * 0.57),
                    sin(drive * 0.43 + 1.7)
                );
                float q = fbm(p * 0.72 + motion);
                float r = fbm(p * 0.91 + vec3(q * 2.8, -q * 2.1, q * 2.5) - motion * 0.64);
                float s = fbm(p + vec3(r * 3.1, q * 2.4, -r * 2.7) + motion * 0.38);
                vec3 warped = p + (vec3(q, r, s) - 0.5) * 2.25;
                float tunnel = sin(length(warped) * 7.5 - drive * 3.2 + r * TAU);
                float streaks = sin((warped.x + warped.y - warped.z) * 3.4 + q * 8.0 + drive);
                coverage = clamp(0.5 + tunnel * 0.27 + streaks * 0.18 + (s - 0.5) * 0.42, 0.0, 1.0);
                coverage = smoothstep(0.08, 0.92, coverage);
            }
            float threshold = u_style > 1.5
                ? bayer4(ivec2(pixel_id))
                : bayer8(ivec2(pixel_id));
            float pattern_mask = step(threshold, coverage);
            pattern_mask *= 1.0 - hole_mask;

            vec3 color = mix(u_back_color.rgb, u_front_color.rgb, pattern_mask);
            float alpha = mix(u_back_color.a, u_front_color.a, pattern_mask);

            out_color = vec4(color, alpha * u_opacity);
        }
    """,
    blockShaderVertexSource
)

val blockShaderFormat = gpu.format(VertexAttribute.floats(3), VertexAttribute.floats(3))
val blockShaderMesh = gpu.indexedMesh(blockShaderFormat)
val blockGlowMesh = gpu.indexedMesh(blockShaderFormat)
val blockGlowPipeline = gpu.pipeline(
    blockGlowGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.TEST
)
val inkSmokePipeline = gpu.pipeline(
    inkSmokeGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.TEST
)
val dotsPipeline = gpu.pipeline(
    dotsGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.TEST
)
val plasmaPipeline = gpu.pipeline(
    plasmaGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.TEST
)
val pixelWavePipeline = gpu.pipeline(
    pixelWaveGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.TEST
)
val blockGlowPipeline3D = gpu.pipeline(
    blockGlowGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.OFF
)
val inkSmokePipeline3D = gpu.pipeline(
    inkSmokeGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.OFF
)
val dotsPipeline3D = gpu.pipeline(
    dotsGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.OFF
)
val plasmaPipeline3D = gpu.pipeline(
    plasmaGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.OFF
)
val pixelWavePipeline3D = gpu.pipeline(
    pixelWaveGpuShader,
    DrawMode.TRIANGLES,
    BlendMode.ALPHA,
    DepthMode.OFF
)
val inkSmokePass = gpu.renderType(inkSmokePipeline, blockShaderMesh)
val dotsPass = gpu.renderType(dotsPipeline, blockShaderMesh)
val plasmaPass = gpu.renderType(plasmaPipeline, blockShaderMesh)
val pixelWavePass = gpu.renderType(pixelWavePipeline, blockShaderMesh)
val blockGlowPass = gpu.renderType(blockGlowPipeline, blockGlowMesh)
val inkSmokePass3D = gpu.renderType(inkSmokePipeline3D, blockShaderMesh)
val dotsPass3D = gpu.renderType(dotsPipeline3D, blockShaderMesh)
val plasmaPass3D = gpu.renderType(plasmaPipeline3D, blockShaderMesh)
val pixelWavePass3D = gpu.renderType(pixelWavePipeline3D, blockShaderMesh)
val blockGlowPass3D = gpu.renderType(blockGlowPipeline3D, blockGlowMesh)

fun inkColorUniform(name: String, color: Int) {
    inkSmokeGpuShader.set(
        name,
        ((color ushr 16) and 255) / 255f,
        ((color ushr 8) and 255) / 255f,
        (color and 255) / 255f
    )
}

fun dotsColorUniform(name: String, color: Int) {
    dotsGpuShader.set(
        name,
        ((color ushr 16) and 255) / 255f,
        ((color ushr 8) and 255) / 255f,
        (color and 255) / 255f,
        ((color ushr 24) and 255) / 255f
    )
}

fun plasmaColorUniform(name: String, color: Int) {
    plasmaGpuShader.set(
        name,
        ((color ushr 16) and 255) / 255f,
        ((color ushr 8) and 255) / 255f,
        (color and 255) / 255f
    )
}

fun pixelWaveColorUniform(name: String, color: Int) {
    pixelWaveGpuShader.set(
        name,
        ((color ushr 16) and 255) / 255f,
        ((color ushr 8) and 255) / 255f,
        (color and 255) / 255f,
        ((color ushr 24) and 255) / 255f
    )
}

fun glowColorUniform(color: Int) {
    blockGlowGpuShader.set(
        "u_color",
        ((color ushr 16) and 255) / 255f,
        ((color ushr 8) and 255) / 255f,
        (color and 255) / 255f,
        ((color ushr 24) and 255) / 255f
    )
}

fun drawGpuBlockGlow(event: Render3DEvent, box: Box, opacity: Float, strength: Int, color: Int) {
    val camera = event.camera()
    val minX = box.minX()
    val minY = box.minY()
    val minZ = box.minZ()
    val maxX = box.maxX()
    val maxY = box.maxY()
    val maxZ = box.maxZ()
    val glowStrength = strength.coerceIn(1, 10)
    val radius = 0.008 + glowStrength * 0.0045
    val planeOffset = 0.003
    val verts = blockGlowMesh.verts()
    val indices = blockGlowMesh.idx() ?: return

    fun vertex(x: Double, y: Double, z: Double): Int = verts
        .putVec3(
            (x - camera.x()).toFloat(),
            (y - camera.y()).toFloat(),
            (z - camera.z()).toFloat()
        )
        .putVec3(
            ((x - minX) / (maxX - minX)).toFloat(),
            ((y - minY) / (maxY - minY)).toFloat(),
            ((z - minZ) / (maxZ - minZ)).toFloat()
        )
        .next()

    fun quad(
        ax: Double, ay: Double, az: Double,
        bx: Double, by: Double, bz: Double,
        cx: Double, cy: Double, cz: Double,
        dx: Double, dy: Double, dz: Double
    ) {
        val a = vertex(ax, ay, az)
        val b = vertex(bx, by, bz)
        val c = vertex(cx, cy, cz)
        val d = vertex(dx, dy, dz)
        indices.putQuad(a, b, c, d)
    }

    if (camera.x() < minX) quad(minX - planeOffset, minY - radius, minZ - radius, minX - planeOffset, maxY + radius, minZ - radius, minX - planeOffset, maxY + radius, maxZ + radius, minX - planeOffset, minY - radius, maxZ + radius)
    else if (camera.x() > maxX) quad(maxX + planeOffset, minY - radius, maxZ + radius, maxX + planeOffset, maxY + radius, maxZ + radius, maxX + planeOffset, maxY + radius, minZ - radius, maxX + planeOffset, minY - radius, minZ - radius)
    if (camera.y() < minY) quad(minX - radius, minY - planeOffset, maxZ + radius, maxX + radius, minY - planeOffset, maxZ + radius, maxX + radius, minY - planeOffset, minZ - radius, minX - radius, minY - planeOffset, minZ - radius)
    else if (camera.y() > maxY) quad(minX - radius, maxY + planeOffset, minZ - radius, maxX + radius, maxY + planeOffset, minZ - radius, maxX + radius, maxY + planeOffset, maxZ + radius, minX - radius, maxY + planeOffset, maxZ + radius)
    if (camera.z() < minZ) quad(maxX + radius, minY - radius, minZ - planeOffset, maxX + radius, maxY + radius, minZ - planeOffset, minX - radius, maxY + radius, minZ - planeOffset, minX - radius, minY - radius, minZ - planeOffset)
    else if (camera.z() > maxZ) quad(minX - radius, minY - radius, maxZ + planeOffset, minX - radius, maxY + radius, maxZ + planeOffset, maxX + radius, maxY + radius, maxZ + planeOffset, maxX + radius, minY - radius, maxZ + planeOffset)

    blockGlowGpuShader.setMat4("u_view", event.viewMatrix())
    blockGlowGpuShader.setMat4("u_projection", event.projectionMatrix())
    blockGlowGpuShader.set("u_radius", (radius / (maxX - minX)).toFloat())
    blockGlowGpuShader.set("u_opacity", (opacity * (0.22f + glowStrength * 0.028f)).coerceIn(0f, 1f))
    glowColorUniform(color)
    if (renderIn3D()) blockGlowPass3D.draw() else blockGlowPass.draw()
}

fun drawGpuBlockShader(
    event: Render3DEvent,
    boxes: List<Box>,
    patternBox: Box,
    opacity: Float,
    mode: String
) {
    if (boxes.isEmpty()) return
    val camera = event.camera()
    val patternSizeX = patternBox.sizeX().coerceAtLeast(0.0001)
    val patternSizeY = patternBox.sizeY().coerceAtLeast(0.0001)
    val patternSizeZ = patternBox.sizeZ().coerceAtLeast(0.0001)
    val verts = blockShaderMesh.verts()
    val indices = blockShaderMesh.idx() ?: return

    fun vertex(x: Double, y: Double, z: Double): Int = verts
        .putVec3(
            (x - camera.x()).toFloat(),
            (y - camera.y()).toFloat(),
            (z - camera.z()).toFloat()
        )
        .putVec3(
            ((x - patternBox.minX()) / patternSizeX).toFloat(),
            ((y - patternBox.minY()) / patternSizeY).toFloat(),
            ((z - patternBox.minZ()) / patternSizeZ).toFloat()
        )
        .next()

    fun quad(
        ax: Double, ay: Double, az: Double,
        bx: Double, by: Double, bz: Double,
        cx: Double, cy: Double, cz: Double,
        dx: Double, dy: Double, dz: Double
    ) {
        val a = vertex(ax, ay, az)
        val b = vertex(bx, by, bz)
        val c = vertex(cx, cy, cz)
        val d = vertex(dx, dy, dz)
        indices.putQuad(a, b, c, d)
    }

    val shape = mutableListOf<Box>()
    val xs = mutableListOf<Double>()
    val ys = mutableListOf<Double>()
    val zs = mutableListOf<Double>()

    fun addCoordinate(coordinates: MutableList<Double>, value: Double) {
        for (existing in coordinates) {
            if (Math.abs(existing - value) < 0.000001) return
        }
        coordinates.add(value)
    }

    fun sortCoordinates(coordinates: MutableList<Double>) {
        var i = 0
        while (i < coordinates.size) {
            var j = i + 1
            while (j < coordinates.size) {
                if (coordinates[j] < coordinates[i]) {
                    val swap = coordinates[i]
                    coordinates[i] = coordinates[j]
                    coordinates[j] = swap
                }
                j++
            }
            i++
        }
    }

    for (source in boxes) {
        val part = source.expand(0.004)
        shape.add(part)
        addCoordinate(xs, part.minX())
        addCoordinate(xs, part.maxX())
        addCoordinate(ys, part.minY())
        addCoordinate(ys, part.maxY())
        addCoordinate(zs, part.minZ())
        addCoordinate(zs, part.maxZ())
    }
    sortCoordinates(xs)
    sortCoordinates(ys)
    sortCoordinates(zs)

    fun occupied(xIndex: Int, yIndex: Int, zIndex: Int): Boolean {
        if (xIndex < 0 || yIndex < 0 || zIndex < 0) return false
        if (xIndex + 1 >= xs.size || yIndex + 1 >= ys.size || zIndex + 1 >= zs.size) return false
        val x = (xs[xIndex] + xs[xIndex + 1]) * 0.5
        val y = (ys[yIndex] + ys[yIndex + 1]) * 0.5
        val z = (zs[zIndex] + zs[zIndex + 1]) * 0.5
        for (part in shape) {
            if (x > part.minX() - 0.000001 && x < part.maxX() + 0.000001 &&
                y > part.minY() - 0.000001 && y < part.maxY() + 0.000001 &&
                z > part.minZ() - 0.000001 && z < part.maxZ() + 0.000001) return true
        }
        return false
    }

    var xi = 0
    while (xi + 1 < xs.size) {
        var yi = 0
        while (yi + 1 < ys.size) {
            var zi = 0
            while (zi + 1 < zs.size) {
                if (occupied(xi, yi, zi)) {
                    val x0 = xs[xi]
                    val x1 = xs[xi + 1]
                    val y0 = ys[yi]
                    val y1 = ys[yi + 1]
                    val z0 = zs[zi]
                    val z1 = zs[zi + 1]

                    if (!occupied(xi - 1, yi, zi) && camera.x() < x0) quad(x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1)
                    if (!occupied(xi + 1, yi, zi) && camera.x() > x1) quad(x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0)
                    if (!occupied(xi, yi - 1, zi) && camera.y() < y0) quad(x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0)
                    if (!occupied(xi, yi + 1, zi) && camera.y() > y1) quad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1)
                    if (!occupied(xi, yi, zi - 1) && camera.z() < z0) quad(x1, y0, z0, x1, y1, z0, x0, y1, z0, x0, y0, z0)
                    if (!occupied(xi, yi, zi + 1) && camera.z() > z1) quad(x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1)
                }
                zi++
            }
            yi++
        }
        xi++
    }

    val time = (client.tick().toFloat() + event.tickDelta()) / 20f
    if (mode == "Dots") {
        dotsGpuShader.setMat4("u_view", event.viewMatrix())
        dotsGpuShader.setMat4("u_projection", event.projectionMatrix())
        dotsGpuShader.set("u_time", time)
        dotsGpuShader.set("u_speed", shaderSpeed.value())
        dotsGpuShader.set("u_brightness", brightness.value())
        dotsGpuShader.set("u_dot_size", dotSize.value())
        dotsGpuShader.set("u_grid_density", gridDensity.value())
        dotsGpuShader.set("u_pattern_scale", patternScale.value())
        dotsGpuShader.set("u_opacity", opacity.coerceIn(0f, 1f))
        dotsColorUniform("u_dot_color", dotColor.value())
        dotsColorUniform("u_background", backgroundColor.value())
        if (renderIn3D()) dotsPass3D.draw() else dotsPass.draw()
    } else if (mode == "Ink Smoke") {
        inkSmokeGpuShader.setMat4("u_view", event.viewMatrix())
        inkSmokeGpuShader.setMat4("u_projection", event.projectionMatrix())
        inkSmokeGpuShader.set("u_time", time)
        inkSmokeGpuShader.set("u_speed", inkSpeed.value())
        inkSmokeGpuShader.set("u_scale", inkScale.value())
        inkSmokeGpuShader.set("u_warp", inkWarp.value())
        inkSmokeGpuShader.set("u_highlight", inkHighlight.value())
        inkSmokeGpuShader.set("u_downscale", when (inkDownscale.value().name()) {
            "x2" -> 2f
            "x4" -> 4f
            "x8" -> 8f
            else -> 1f
        })
        inkSmokeGpuShader.set("u_opacity", opacity.coerceIn(0f, 1f))
        inkColorUniform("u_ink1", inkColor1.value())
        inkColorUniform("u_ink2", inkColor2.value())
        inkColorUniform("u_ink3", inkColor3.value())
        inkColorUniform("u_ink4", inkColor4.value())
        inkColorUniform("u_glow", inkGlow.value())
        if (renderIn3D()) inkSmokePass3D.draw() else inkSmokePass.draw()
    } else if (mode == "Plasma") {
        plasmaGpuShader.setMat4("u_view", event.viewMatrix())
        plasmaGpuShader.setMat4("u_projection", event.projectionMatrix())
        plasmaGpuShader.set("u_time", time)
        plasmaGpuShader.set("u_scale", plasmaScale.value())
        plasmaGpuShader.set("u_intensity", plasmaIntensity.value())
        plasmaGpuShader.set("u_distortion", plasmaDistortion.value())
        plasmaGpuShader.set("u_downscale", when {
            plasmaDownscale.selected(plasmaDownscale2x) -> 2f
            plasmaDownscale.selected(plasmaDownscale4x) -> 4f
            plasmaDownscale.selected(plasmaDownscale8x) -> 8f
            else -> 1f
        })
        plasmaGpuShader.set("u_opacity", opacity.coerceIn(0f, 1f))
        plasmaColorUniform("u_color1", plasmaColor1.value())
        plasmaColorUniform("u_color2", plasmaColor2.value())
        plasmaColorUniform("u_color3", plasmaColor3.value())
        plasmaColorUniform("u_color4", plasmaColor4.value())
        plasmaColorUniform("u_color5", plasmaColor5.value())
        if (renderIn3D()) plasmaPass3D.draw() else plasmaPass.draw()
    } else if (mode == "Pixel") {
        pixelWaveGpuShader.setMat4("u_view", event.viewMatrix())
        pixelWaveGpuShader.setMat4("u_projection", event.projectionMatrix())
        pixelWaveGpuShader.set("u_time", time)
        pixelWaveGpuShader.set("u_speed", pixelWaveSpeed.value())
        pixelWaveGpuShader.set("u_style", when (pixelStyle.value().name()) {
            "Noise" -> 1f
            "Warp" -> 2f
            else -> 0f
        })
        pixelWaveGpuShader.set("u_pixel_size", pixelWaveSize.intValue().toFloat())
        pixelWaveGpuShader.set("u_pattern_scale", pixelWavePatternScale.value())
        pixelWaveGpuShader.set("u_opacity", opacity.coerceIn(0f, 1f))
        pixelWaveColorUniform("u_back_color", pixelWaveBackColor.value())
        pixelWaveColorUniform("u_front_color", pixelWaveFrontColor.value())
        if (renderIn3D()) pixelWavePass3D.draw() else pixelWavePass.draw()
    }
}

fun targetedOverlayBlock(): Block? {
    val cameraRotation = rotations.camera()
    val eye = player.eyePosition()

    if (blocksOnly.value()) {
        val direction = cameraRotation.direction()
        val rayEnd = eye.add(direction.multiply(BLOCK_REACH))
        var rayStart = eye

        repeat(96) {
            val hit = raycast.blocks(
                rayStart,
                rayEnd,
                RaycastShape.OUTLINE,
                FluidHandling.NONE
            )
            if (hit !is Hit.OnBlock) return null

            val block = world.block(hit.blockX(), hit.blockY(), hit.blockZ())
            if (!isPlantBlock(block)) return block

            rayStart = hit.position().add(direction.multiply(0.05))
            if (rayStart.squaredDistanceTo(eye) >= BLOCK_REACH * BLOCK_REACH) return null
        }
        return null
    } else {
        val hit = raycast.from(cameraRotation, BLOCK_REACH, true) {
            it.isLiving() && !it.isSelf()
        }
        if (hit !is Hit.OnBlock) return null
        return world.block(hit.blockX(), hit.blockY(), hit.blockZ())
    }
}

fun overlayBoxes(position: OverlayPosition): List<Box> {
    val block = world.block(position.x, position.y, position.z)
    if (block.air()) return emptyList()

    val outline = block.outlineBoxes()
    val shape = when {
        outline.isNotEmpty() -> outline
        block.collisionBoxes().isNotEmpty() -> block.collisionBoxes()
        else -> listOf(block.box())
    }

    return shape
        .filter { it.sizeX() > 0.0001 && it.sizeY() > 0.0001 && it.sizeZ() > 0.0001 }
        .map { it.expand(OVERLAY_EXPAND) }
}

fun horizontalOffset(facing: String): OverlayPosition = when (facing.lowercase()) {
    "north" -> OverlayPosition(0, 0, -1)
    "south" -> OverlayPosition(0, 0, 1)
    "west" -> OverlayPosition(-1, 0, 0)
    "east" -> OverlayPosition(1, 0, 0)
    "down" -> OverlayPosition(0, -1, 0)
    "up" -> OverlayPosition(0, 1, 0)
    else -> OverlayPosition(0, 0, 0)
}

fun clockwiseOffset(facing: String): OverlayPosition = when (facing.lowercase()) {
    "north" -> OverlayPosition(1, 0, 0)
    "east" -> OverlayPosition(0, 0, 1)
    "south" -> OverlayPosition(-1, 0, 0)
    "west" -> OverlayPosition(0, 0, -1)
    else -> OverlayPosition(0, 0, 0)
}

fun safeBlockProperty(block: Block, property: String): String =
    block.properties()[property] ?: ""

fun connectedOverlayPositions(block: Block): Set<OverlayPosition> {
    val positions = mutableSetOf(OverlayPosition(block.x(), block.y(), block.z()))
    if (!linkMultiblocks.value()) return positions
    val id = block.id()

    fun addMatching(x: Int, y: Int, z: Int, acceptedIds: Set<String> = setOf(id)) {
        val neighbour = world.block(x, y, z)
        if (!neighbour.air() && neighbour.id() in acceptedIds) {
            positions.add(OverlayPosition(x, y, z))
        }
    }

    when (safeBlockProperty(block, "half").lowercase()) {
        "lower" -> addMatching(block.x(), block.y() + 1, block.z())
        "upper" -> addMatching(block.x(), block.y() - 1, block.z())
    }

    if (id.substringAfter(':').endsWith("_bed")) {
        val offset = horizontalOffset(safeBlockProperty(block, "facing"))
        val multiplier = if (safeBlockProperty(block, "part").lowercase() == "head") -1 else 1
        addMatching(
            block.x() + offset.x * multiplier,
            block.y(),
            block.z() + offset.z * multiplier
        )
    }

    if (id.substringAfter(':').endsWith("chest")) {
        val type = safeBlockProperty(block, "type").lowercase()
        if (type == "left" || type == "right") {
            val clockwise = clockwiseOffset(safeBlockProperty(block, "facing"))
            val multiplier = if (type == "left") 1 else -1
            addMatching(
                block.x() + clockwise.x * multiplier,
                block.y(),
                block.z() + clockwise.z * multiplier
            )
        }
    }

    val path = id.substringAfter(':')
    if ((path == "piston" || path == "sticky_piston") && safeBlockProperty(block, "extended") == "true") {
        val offset = horizontalOffset(safeBlockProperty(block, "facing"))
        if (offset.x != 0 || offset.y != 0 || offset.z != 0) {
            addMatching(
                block.x() + offset.x,
                block.y() + offset.y,
                block.z() + offset.z,
                setOf("minecraft:piston_head", "piston_head")
            )
        }
    } else if (path == "piston_head") {
        val offset = horizontalOffset(safeBlockProperty(block, "facing"))
        if (offset.x != 0 || offset.y != 0 || offset.z != 0) {
            addMatching(
                block.x() - offset.x,
                block.y() - offset.y,
                block.z() - offset.z,
                setOf("minecraft:piston", "minecraft:sticky_piston", "piston", "sticky_piston")
            )
        }
    }

    return positions
}

fun structurePatternBox(position: OverlayPosition): Box {
    val positions = connectedOverlayPositions(world.block(position.x, position.y, position.z))
    var minX = position.x
    var minY = position.y
    var minZ = position.z
    var maxX = position.x
    var maxY = position.y
    var maxZ = position.z

    for (part in positions) {
        if (part.x < minX) minX = part.x
        if (part.y < minY) minY = part.y
        if (part.z < minZ) minZ = part.z
        if (part.x > maxX) maxX = part.x
        if (part.y > maxY) maxY = part.y
        if (part.z > maxZ) maxZ = part.z
    }

    return Box(
        minX.toDouble(),
        minY.toDouble(),
        minZ.toDouble(),
        maxX + 1.0,
        maxY + 1.0,
        maxZ + 1.0
    )
}

fun clearOverlayFades() {
    fadingOverlays.clear()
    lastPrimaryTarget = null
    swapActive = false
    swapProgress = 0f
    swapPreviousProgress = 0f
}

on<ClientTickEvent> {
    if (!inGame) {
        clearOverlayFades()
        return@on
    }

    val primaryBlock = targetedOverlayBlock()
    val primaryPos = primaryBlock?.let { OverlayPosition(it.x(), it.y(), it.z()) }
    val targets = primaryBlock?.let { connectedOverlayPositions(it) } ?: emptySet()

    if (blockSwapAnimation.value() && swapDuration.intValue() > 0) {
        if (primaryPos != null) {
            val previous = lastPrimaryTarget
            if (previous == null) {
                lastPrimaryTarget = primaryPos
            } else if (previous != primaryPos) {
                if (swapActive) {
                    val t = interpolateSwap(swapProgress)
                    swapFromX = swapFromX + (swapToX - swapFromX) * t
                    swapFromY = swapFromY + (swapToY - swapFromY) * t
                    swapFromZ = swapFromZ + (swapToZ - swapFromZ) * t
                } else {
                    swapFromX = previous.x.toDouble()
                    swapFromY = previous.y.toDouble()
                    swapFromZ = previous.z.toDouble()
                }
                swapToX = primaryPos.x.toDouble()
                swapToY = primaryPos.y.toDouble()
                swapToZ = primaryPos.z.toDouble()
                swapProgress = 0f
                swapPreviousProgress = 0f
                swapActive = true
                lastPrimaryTarget = primaryPos
            }
        } else {
            lastPrimaryTarget = null
            swapActive = false
            swapProgress = 0f
            swapPreviousProgress = 0f
        }

        if (swapActive) {
            swapPreviousProgress = swapProgress
            if (swapProgress >= 1f) {
                swapActive = false
                swapProgress = 1f
                swapPreviousProgress = 1f
            } else {
                swapProgress = (swapProgress + 1f / swapDuration.intValue().toFloat()).coerceAtMost(1f)
            }
        }
    } else {
        lastPrimaryTarget = primaryPos
        swapActive = false
        swapProgress = 0f
        swapPreviousProgress = 0f
    }

    // Keep shape cache while the block still exists so fade-out works after break
    for (target in targets) {
        val state = fadingOverlays.getOrPut(target) { FadingOverlay() }
        refreshOverlayCache(target, state)
    }
    for ((position, state) in fadingOverlays) {
        if (position !in targets) {
            refreshOverlayCache(position, state)
        }
    }

    if (swapActive) {
        fadingOverlays.clear()
        for (target in targets) {
            val state = FadingOverlay(1f, 1f)
            refreshOverlayCache(target, state)
            fadingOverlays[target] = state
        }
        return@on
    }

    if (fadeTicks.intValue() <= 0) {
        fadingOverlays.clear()
        for (target in targets) {
            val state = FadingOverlay(1f, 1f)
            refreshOverlayCache(target, state)
            fadingOverlays[target] = state
        }
        return@on
    }

    for (target in targets) {
        if (!fadingOverlays.containsKey(target)) {
            val state = FadingOverlay()
            refreshOverlayCache(target, state)
            fadingOverlays[target] = state
        }
    }

    val step = 1f / fadeTicks.intValue().toFloat()
    val iterator = fadingOverlays.entries.iterator()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        val state = entry.value
        state.previousOpacity = state.opacity
        state.opacity = if (entry.key in targets) {
            (state.opacity + step).coerceAtMost(1f)
        } else {
            (state.opacity - step).coerceAtLeast(0f)
        }
        if (entry.key !in targets && state.opacity <= 0f && state.previousOpacity <= 0f) {
            iterator.remove()
        }
    }
}

on<WorldLoadEvent> { clearOverlayFades() }
onDisable { clearOverlayFades() }

on<BlockOutlineEvent> { event ->
    event.cancel()
}

on<Render3DEvent> { event ->
    if (!inGame) return@on

    val render = event.render()
    val partialTick = event.tickDelta().coerceIn(0f, 1f)
    val (ox, oy, oz) = currentSwapOffset(partialTick)

    for ((position, state) in fadingOverlays) {
        val opacity = state.previousOpacity +
            (state.opacity - state.previousOpacity) * partialTick
        if (opacity <= 0.001f) continue

        // Prefer live shape; if block was broken, use last cached shape for fade-out
        val liveBoxes = overlayBoxes(position)
        val boxes = if (liveBoxes.isNotEmpty()) {
            refreshOverlayCache(position, state)
            liveBoxes
        } else {
            state.cachedBoxes
        }
        if (boxes.isEmpty()) continue

        val patternBox = if (liveBoxes.isNotEmpty()) {
            structurePatternBox(position)
        } else {
            state.cachedPattern ?: structurePatternBox(position)
        }

        val drawnBoxes = offsetBoxes(boxes, ox, oy, oz)
        val drawnPattern = patternBox.offset(ox, oy, oz)

        if (overlayMode.value().name() == "Shader") {
            val shaderAlpha = (opacity * translucency.value()).coerceIn(0f, 1f)
            when (shader.value().name()) {
                "Dots" -> drawGpuBlockShader(event, drawnBoxes, drawnPattern, shaderAlpha, "Dots")
                "Ink Smoke" -> drawGpuBlockShader(event, drawnBoxes, drawnPattern, shaderAlpha, "Ink Smoke")
                "Plasma" -> drawGpuBlockShader(event, drawnBoxes, drawnPattern, shaderAlpha, "Plasma")
                "Pixel" -> drawGpuBlockShader(event, drawnBoxes, drawnPattern, shaderAlpha, "Pixel")
            }
            continue
        }

        for (box in drawnBoxes) {
            when (overlayMode.value().name()) {
                "Outline" -> {
                    if (modeGlow.value()) {
                        drawGpuBlockGlow(event, box, opacity, 4, outlineColor.value())
                    }
                    drawThickOutline(
                        render,
                        box,
                        colorOpacity(outlineColor.value(), opacity),
                        outlineThickness.intValue(),
                        renderIn3D()
                    )
                }
                "Fill" -> {
                    if (modeGlow.value()) {
                        drawGpuBlockGlow(event, box, opacity, 4, fillColor.value())
                    }
                    render.filledBox(box, colorOpacity(fillColor.value(), opacity), renderIn3D())
                }
                "Outline + Fill" -> {
                    if (modeGlow.value()) {
                        drawGpuBlockGlow(event, box, opacity, 4, fillColor.value())
                        drawGpuBlockGlow(event, box, opacity, 4, outlineColor.value())
                    }
                    render.filledBox(box, colorOpacity(fillColor.value(), opacity), renderIn3D())
                    drawThickOutline(
                        render,
                        box,
                        colorOpacity(outlineColor.value(), opacity),
                        outlineThickness.intValue(),
                        renderIn3D()
                    )
                }
            }
        }
    }
}








