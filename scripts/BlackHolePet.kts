import kotlin.math.*
import nursultan.party.*

requireApi(5)

name("Singularity Pet Stable")
description("A cinematic, client-side 3D black hole companion with a ray-bent procedural accretion disk.")

// Public API note: the Script API exposes script textures, but not the current world framebuffer.
// Lensing therefore bends the black hole's own procedural disk/rings; it never invents a scene sampler.

val visuals by checkBox("Enabled Visuals", true)
val partySync by checkBox("Party Sync", true)

// Keep the panel short: only presets and collapsed categories are visible by default.
val presetSetting = selectable(
    "Preset", "Interstellar", "Balanced", "Performance", "Custom", selected = "Balanced"
)
val preset by presetSetting

val coreGroup = checkBox("Core & Quality", false)
val qualitySetting = selectable(coreGroup, "Quality", "Low", "Medium", "High", "Ultra", selected = "High")
val quality by qualitySetting
val sizeSetting = slider(coreGroup, "Size", 0.92f, 0.65f, 1.55f, 0.05f).postfix(Postfixes.MULTIPLIER)
val size by sizeSetting

val placementGroup = checkBox("Placement", false)
val sideOffsetSetting = slider(placementGroup, "Side Offset", 2.35f, -4.00f, 4.00f, 0.05f).postfix(Postfixes.BLOCKS)
val sideOffset by sideOffsetSetting
val verticalOffsetSetting = slider(placementGroup, "Vertical Offset", -0.18f, -1.20f, 1.20f, 0.05f).postfix(Postfixes.BLOCKS)
val verticalOffset by verticalOffsetSetting
val backOffsetSetting = slider(placementGroup, "Back Offset", 0.45f, -1.00f, 3.00f, 0.05f).postfix(Postfixes.BLOCKS)
val backOffset by backOffsetSetting

val motionGroup = checkBox("Follow Motion", false)
val followStiffnessSetting = slider(motionGroup, "Follow Stiffness", 13.0f, 2.0f, 30.0f, 0.5f)
val followStiffness by followStiffnessSetting
val followDampingSetting = slider(motionGroup, "Follow Damping", 7.2f, 1.0f, 15.0f, 0.2f)
val followDamping by followDampingSetting
val maxFollowSpeedSetting = slider(motionGroup, "Max Follow Speed", 5.2f, 1.0f, 12.0f, 0.2f).postfix(Postfixes.BLOCKS)
val maxFollowSpeed by maxFollowSpeedSetting
val idleBobSetting = slider(motionGroup, "Idle Bob", 0.065f, 0.0f, 0.16f, 0.005f).postfix(Postfixes.BLOCKS)
val idleBob by idleBobSetting
val orbitRadiusSetting = slider(motionGroup, "Orbit Radius", 3.35f, 2.20f, 6.00f, 0.05f).postfix(Postfixes.BLOCKS)
val orbitRadius by orbitRadiusSetting
val orbitSpeedSetting = slider(motionGroup, "Orbit Speed", 0.32f, 0.08f, 1.20f, 0.02f).postfix(Postfixes.MULTIPLIER)
val orbitSpeed by orbitSpeedSetting
val orbitWanderSetting = slider(motionGroup, "Orbit Wander", 0.72f, 0.0f, 1.00f, 0.05f).postfix(Postfixes.MULTIPLIER)
val orbitWander by orbitWanderSetting

val diskGroup = checkBox("Accretion Disk", false)
val diskRadiusSetting = slider(diskGroup, "Disk Radius", 10.2f, 8.0f, 12.0f, 0.2f)
val diskRadius by diskRadiusSetting
val diskTiltSetting = slider(diskGroup, "Disk Tilt", 18.0f, 5.0f, 35.0f, 1.0f).postfix(Postfixes.DEGREES)
val diskTilt by diskTiltSetting
val diskBrightnessSetting = slider(diskGroup, "Disk Brightness", 1.30f, 0.35f, 2.50f, 0.05f).postfix(Postfixes.MULTIPLIER)
val diskBrightness by diskBrightnessSetting
val diskTemperatureSetting = slider(diskGroup, "Disk Temperature", 1.00f, 0.55f, 1.45f, 0.05f).postfix(Postfixes.MULTIPLIER)
val diskTemperature by diskTemperatureSetting
val diskSpinSpeedSetting = slider(diskGroup, "Disk Spin Speed", 1.00f, -2.00f, 2.00f, 0.05f).postfix(Postfixes.MULTIPLIER)
val diskSpinSpeed by diskSpinSpeedSetting

val relativityGroup = checkBox("Relativity & Glow", false)
val lensingStrengthSetting = slider(relativityGroup, "Lensing Strength", 1.00f, 0.25f, 1.35f, 0.05f).postfix(Postfixes.MULTIPLIER)
val lensingStrength by lensingStrengthSetting
val dopplerStrengthSetting = slider(relativityGroup, "Doppler Strength", 1.00f, 0.0f, 1.50f, 0.05f).postfix(Postfixes.MULTIPLIER)
val dopplerStrength by dopplerStrengthSetting
val redshiftStrengthSetting = slider(relativityGroup, "Redshift Strength", 0.75f, 0.0f, 1.25f, 0.05f).postfix(Postfixes.MULTIPLIER)
val redshiftStrength by redshiftStrengthSetting
val photonRingBrightnessSetting = slider(relativityGroup, "Photon Ring Brightness", 1.25f, 0.0f, 2.50f, 0.05f).postfix(Postfixes.MULTIPLIER)
val photonRingBrightness by photonRingBrightnessSetting
val glowStrengthSetting = slider(relativityGroup, "Glow Strength", 0.75f, 0.0f, 1.60f, 0.05f).postfix(Postfixes.MULTIPLIER)
val glowStrength by glowStrengthSetting

val plasmaGroup = checkBox("Plasma & Animation", false)
val plasmaAmountSetting = slider(plasmaGroup, "Plasma Amount", 0.85f, 0.0f, 1.50f, 0.05f).postfix(Postfixes.MULTIPLIER)
val plasmaAmount by plasmaAmountSetting
val particleAmountSetting = slider(plasmaGroup, "Particle Amount", 0.65f, 0.0f, 1.00f, 0.05f).postfix(Postfixes.MULTIPLIER)
val particleAmount by particleAmountSetting
val animationSpeedSetting = slider(plasmaGroup, "Animation Speed", 1.00f, 0.15f, 2.00f, 0.05f).postfix(Postfixes.MULTIPLIER)
val animationSpeed by animationSpeedSetting

fun applyPreset(presetName: String) {
    when (presetName.lowercase()) {
        "interstellar" -> {
            qualitySetting.select("Ultra")
            sizeSetting.value(0.95f)
            sideOffsetSetting.value(2.55f)
            backOffsetSetting.value(0.55f)
            diskRadiusSetting.value(10.8f)
            diskTiltSetting.value(20.0f)
            diskBrightnessSetting.value(1.55f)
            diskTemperatureSetting.value(1.10f)
            lensingStrengthSetting.value(1.12f)
            dopplerStrengthSetting.value(1.08f)
            redshiftStrengthSetting.value(0.82f)
            photonRingBrightnessSetting.value(1.55f)
            glowStrengthSetting.value(0.92f)
            plasmaAmountSetting.value(1.00f)
            particleAmountSetting.value(0.85f)
            animationSpeedSetting.value(0.90f)
            orbitRadiusSetting.value(3.60f)
            orbitSpeedSetting.value(0.27f)
            orbitWanderSetting.value(0.82f)
        }
        "balanced" -> {
            qualitySetting.select("High")
            sizeSetting.value(0.92f)
            sideOffsetSetting.value(2.35f)
            backOffsetSetting.value(0.45f)
            diskRadiusSetting.value(10.2f)
            diskTiltSetting.value(18.0f)
            diskBrightnessSetting.value(1.30f)
            diskTemperatureSetting.value(1.00f)
            lensingStrengthSetting.value(1.00f)
            dopplerStrengthSetting.value(1.00f)
            redshiftStrengthSetting.value(0.75f)
            photonRingBrightnessSetting.value(1.25f)
            glowStrengthSetting.value(0.75f)
            plasmaAmountSetting.value(0.85f)
            particleAmountSetting.value(0.65f)
            animationSpeedSetting.value(1.00f)
            orbitRadiusSetting.value(3.35f)
            orbitSpeedSetting.value(0.32f)
            orbitWanderSetting.value(0.72f)
        }
        "performance" -> {
            qualitySetting.select("Low")
            sizeSetting.value(0.84f)
            sideOffsetSetting.value(2.25f)
            backOffsetSetting.value(0.50f)
            diskRadiusSetting.value(9.2f)
            diskTiltSetting.value(16.0f)
            diskBrightnessSetting.value(1.15f)
            diskTemperatureSetting.value(0.92f)
            lensingStrengthSetting.value(0.85f)
            dopplerStrengthSetting.value(0.85f)
            redshiftStrengthSetting.value(0.62f)
            photonRingBrightnessSetting.value(1.00f)
            glowStrengthSetting.value(0.52f)
            plasmaAmountSetting.value(0.62f)
            particleAmountSetting.value(0.30f)
            animationSpeedSetting.value(0.85f)
            orbitRadiusSetting.value(3.10f)
            orbitSpeedSetting.value(0.28f)
            orbitWanderSetting.value(0.55f)
        }
    }
}

presetSetting.onChange { selected ->
    if (selected.name().lowercase() != "custom") applyPreset(selected.name())
}

data class SphereTemplate(val positions: FloatArray, val indices: IntArray)
data class QualityProfile(
    val sphere: SphereTemplate,
    val raySteps: Int,
    val noiseOctaves: Int,
    val particleCap: Int
)
data class ParticleSeed(
    val phase: Double,
    val radius: Double,
    val eccentricity: Double,
    val lift: Double,
    val heat: Double,
    val lifeRate: Double
)

fun clampD(value: Double, minimum: Double, maximum: Double): Double =
    max(minimum, min(maximum, value))

fun clampF(value: Float, minimum: Float, maximum: Float): Float =
    max(minimum, min(maximum, value))

fun smoothstepD(edge0: Double, edge1: Double, value: Double): Double {
    val t = clampD((value - edge0) / (edge1 - edge0), 0.0, 1.0)
    return t * t * (3.0 - 2.0 * t)
}

fun lerpVec(a: Vec, b: Vec, t: Double): Vec = Vec(
    a.x() + (b.x() - a.x()) * t,
    a.y() + (b.y() - a.y()) * t,
    a.z() + (b.z() - a.z()) * t
)

fun safeNormalize(v: Vec, fallback: Vec = Vec(0.0, 1.0, 0.0)): Vec =
    if (v.squaredLength() > 1.0e-12) v.normalize() else fallback

fun cross(a: Vec, b: Vec): Vec = Vec(
    a.y() * b.z() - a.z() * b.y(),
    a.z() * b.x() - a.x() * b.z(),
    a.x() * b.y() - a.y() * b.x()
)

fun fract(value: Double): Double = value - floor(value)

fun deterministic(index: Int, salt: Double): Double =
    fract(sin(index * 91.731 + salt * 17.113) * 43758.5453123)

fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
    ((alpha.coerceIn(0, 255) and 255) shl 24) or
        ((red.coerceIn(0, 255) and 255) shl 16) or
        ((green.coerceIn(0, 255) and 255) shl 8) or
        (blue.coerceIn(0, 255) and 255)

fun buildSphere(segments: Int, stacks: Int): SphereTemplate {
    val positions = FloatArray((segments + 1) * (stacks + 1) * 3)
    val indices = IntArray(segments * stacks * 6)
    var vertexOffset = 0
    for (stack in 0..stacks) {
        val v = stack.toDouble() / stacks.toDouble()
        val phi = PI * v
        val y = cos(phi)
        val ring = sin(phi)
        for (segment in 0..segments) {
            val u = segment.toDouble() / segments.toDouble()
            val theta = u * PI * 2.0
            positions[vertexOffset++] = (cos(theta) * ring).toFloat()
            positions[vertexOffset++] = y.toFloat()
            positions[vertexOffset++] = (sin(theta) * ring).toFloat()
        }
    }
    var indexOffset = 0
    val row = segments + 1
    for (stack in 0 until stacks) {
        for (segment in 0 until segments) {
            val a = stack * row + segment
            val b = a + row
            // Counter-clockwise from outside, so culling keeps the front shell.
            indices[indexOffset++] = a
            indices[indexOffset++] = a + 1
            indices[indexOffset++] = b
            indices[indexOffset++] = a + 1
            indices[indexOffset++] = b + 1
            indices[indexOffset++] = b
        }
    }
    return SphereTemplate(positions, indices)
}

val lowProfile = QualityProfile(buildSphere(20, 13), 28, 2, 18)
val mediumProfile = QualityProfile(buildSphere(30, 19), 44, 3, 40)
val highProfile = QualityProfile(buildSphere(42, 27), 66, 4, 84)
val ultraProfile = QualityProfile(buildSphere(60, 38), 92, 5, 144)

fun currentProfile(): QualityProfile = when (quality.lowercase()) {
    "low" -> lowProfile
    "medium" -> mediumProfile
    "ultra" -> ultraProfile
    else -> highProfile
}

val particleSeeds = Array(128) { index ->
    ParticleSeed(
        phase = deterministic(index, 1.0) * PI * 2.0,
        radius = deterministic(index, 2.0),
        eccentricity = 0.04 + deterministic(index, 3.0) * 0.18,
        lift = deterministic(index, 4.0) * 2.0 - 1.0,
        heat = deterministic(index, 5.0),
        lifeRate = 0.055 + deterministic(index, 6.0) * 0.075
    )
}

val horizonVertexShader = """
    #version 330
    layout(location = 0) in vec3 a_pos;
    uniform mat4 u_view;
    uniform mat4 u_projection;
    out vec3 v_pos;
    void main() {
        v_pos = a_pos;
        gl_Position = u_projection * u_view * vec4(a_pos, 1.0);
    }
""".trimIndent()

val horizonFragmentShader = """
    #version 330
    in vec3 v_pos;
    out vec4 out_color;
    void main() {
        out_color = vec4(0.0, 0.0, 0.0, 1.0);
    }
""".trimIndent()

val colorVertexShader = """
    #version 330
    layout(location = 0) in vec3 a_pos;
    layout(location = 1) in vec4 a_color;
    uniform mat4 u_view;
    uniform mat4 u_projection;
    out vec4 v_color;
    void main() {
        v_color = a_color.bgra;
        gl_Position = u_projection * u_view * vec4(a_pos, 1.0);
    }
""".trimIndent()

val colorFragmentShader = """
    #version 330
    in vec4 v_color;
    out vec4 out_color;
    void main() {
        out_color = v_color;
    }
""".trimIndent()

val lensingFragmentShader = """
    #version 330

    in vec3 v_pos;
    out vec4 out_color;

    uniform vec3 u_center;
    uniform vec3 u_diskNormal;
    uniform float u_rs;
    uniform float u_boundRadius;
    uniform float u_innerRadius;
    uniform float u_outerRadius;
    uniform float u_time;
    uniform float u_brightness;
    uniform float u_temperature;
    uniform float u_spin;
    uniform float u_lensing;
    uniform float u_doppler;
    uniform float u_redshift;
    uniform float u_photonBrightness;
    uniform float u_glow;
    uniform float u_plasma;
    uniform float u_opacity;
    uniform float u_impact;
    uniform float u_impactPhase;
    uniform int u_steps;
    uniform int u_noiseOctaves;

    const float PI = 3.14159265359;
    const int MAX_STEPS = 96;

    float saturate(float x) { return clamp(x, 0.0, 1.0); }

    vec3 safeNormalize(vec3 v) {
        float q = dot(v, v);
        return q > 1.0e-12 ? v * inversesqrt(q) : vec3(0.0, 1.0, 0.0);
    }

    float hash21(vec2 p) {
        p = fract(p * vec2(123.34, 456.21));
        p += dot(p, p + 45.32);
        return fract(p.x * p.y);
    }

    float valueNoise(vec2 p) {
        vec2 i = floor(p);
        vec2 f = fract(p);
        f = f * f * (3.0 - 2.0 * f);
        float a = hash21(i);
        float b = hash21(i + vec2(1.0, 0.0));
        float c = hash21(i + vec2(0.0, 1.0));
        float d = hash21(i + vec2(1.0, 1.0));
        return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
    }

    float fbm(vec2 p) {
        float sum = 0.0;
        float amplitude = 0.55;
        mat2 turn = mat2(0.82, -0.57, 0.57, 0.82);
        for (int octave = 0; octave < 5; ++octave) {
            if (octave >= u_noiseOctaves) break;
            sum += amplitude * valueNoise(p);
            p = turn * p * 2.07 + vec2(11.7, 3.1);
            amplitude *= 0.49;
        }
        return sum;
    }

    vec3 temperatureGradient(float radial, float heat) {
        // A photographic palette: dusty rose in the optically thin outskirts,
        // pearl-white gas in the body and a slightly blue inner photosphere.
        vec3 dust = vec3(0.17, 0.055, 0.045);
        vec3 rose = vec3(0.70, 0.30, 0.29);
        vec3 warmPearl = vec3(1.08, 0.88, 0.76);
        vec3 whiteHot = vec3(1.18, 1.15, 1.10);
        vec3 blueWhite = vec3(0.94, 1.08, 1.28);
        vec3 color = mix(dust, rose, 1.0 - smoothstep(0.60, 1.0, radial));
        color = mix(color, warmPearl, 1.0 - smoothstep(0.34, 0.76, radial));
        color = mix(color, whiteHot, saturate(heat * 1.18));
        color = mix(color, blueWhite, smoothstep(0.72, 1.0, heat) * 0.56);
        return color;
    }

    vec4 diskEmission(vec3 point, vec3 pointCamera, float travelLength) {
        vec3 n = safeNormalize(u_diskNormal);
        float signedHeight = dot(point, n);
        vec3 radialVector = point - n * signedHeight;
        float radius = max(length(radialVector), 1.0e-4);
        if (radius < u_innerRadius || radius > u_outerRadius) return vec4(0.0);

        vec3 radialDirection = radialVector / radius;
        vec3 reference = abs(n.y) < 0.92 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
        vec3 axisU = safeNormalize(cross(reference, n));
        vec3 axisV = cross(n, axisU);
        float angle = atan(dot(radialDirection, axisV), dot(radialDirection, axisU));
        float normalizedRadius = saturate((radius - u_innerRadius) / max(u_outerRadius - u_innerRadius, 0.01));
        // Reject empty space before any procedural noise. This is the dominant
        // performance win for rays passing above or below the disk volume.
        float coarseEnvelope = mix(0.48, 1.82, pow(normalizedRadius, 0.70));
        if (abs(signedHeight) > coarseEnvelope) return vec4(0.0);
        float omega = u_spin * pow(max(radius / u_innerRadius, 1.0), -1.5);
        float phase = angle - u_time * omega;

        float warped = fbm(vec2(phase * 0.78 + log(radius) * 1.7, radius * 0.73));
        float clouds = fbm(vec2(phase * 1.85 - warped * 1.4, radius * 1.65 + u_time * 0.035));
        // Fine structure does not need four full FBM stacks. Two FBMs plus cheap
        // decorrelated value-noise layers retain the same turbulent silhouette.
        float shear = valueNoise(vec2(
            phase * 0.46 + warped * 1.9 - u_time * omega * 0.17,
            log(radius) * 5.6 - u_time * 0.021
        ));
        float microTurbulence = valueNoise(vec2(
            phase * 3.1 + clouds * 2.3,
            radius * 3.25 - u_time * omega * 0.11
        ));
        // Lower spatial frequency avoids moire bands when the disk is almost edge-on.
        float filaments = valueNoise(vec2(phase * 3.4 + warped * 1.6, radius * 2.35));
        float spiralA = 0.5 + 0.5 * sin(phase * 3.0 - log(radius) * 7.4 + warped * 2.8);
        float spiralB = 0.5 + 0.5 * sin(phase * 2.0 + log(radius) * 5.1 - clouds * 2.1);
        float waves = mix(spiralA, spiralB, 0.38);
        float radialShock = 0.5 + 0.5 * sin(radius * 3.65 - warped * 4.1 + shear * 2.2);
        float spiralShock = smoothstep(0.54, 0.91, waves * 0.72 + shear * 0.48);
        float hotSpot = smoothstep(0.90, 0.995, filaments * clouds);
        float density = mix(0.20, 1.24, saturate(
            waves * 0.36 + clouds * 0.42 + shear * 0.28 + microTurbulence * 0.16
        ));
        density *= mix(0.91, 1.08, radialShock);
        density += spiralShock * 0.24;
        density *= mix(1.0, density, saturate(u_plasma));
        density += hotSpot * 1.55 * u_plasma;

        float innerGuard = max(1.0 - sqrt(u_innerRadius / max(radius, u_innerRadius)), 0.0);
        float physicalTemperature = pow(max(radius, 0.01), -0.75) * pow(innerGuard, 0.25);
        float heat = saturate(physicalTemperature * 4.8 * u_temperature);
        float edge = smoothstep(u_innerRadius, u_innerRadius + 0.30, radius) *
                     (1.0 - smoothstep(u_outerRadius - 1.05, u_outerRadius, radius));

        // Real volume instead of an infinitely thin plane. The body flares outward,
        // has a turbulent displaced mid-plane, luminous skins and a translucent corona.
        float halfThickness = mix(0.13, 0.46, pow(normalizedRadius, 0.72));
        halfThickness *= mix(0.82, 1.20, clouds) * (0.90 + 0.16 * u_plasma);
        float corrugation = (warped - 0.5) * halfThickness * 0.58 +
                            sin(phase * 2.0 - radius * 1.7) * halfThickness * 0.075;
        float heightNorm = abs(signedHeight - corrugation) / max(halfThickness, 0.04);
        if (heightNorm > 3.10) return vec4(0.0);
        float denseBody = exp(-heightNorm * heightNorm * 2.85);
        float luminousSkin = exp(-pow((heightNorm - 0.48) / 0.23, 2.0));
        float corona = exp(-heightNorm * 1.18);
        float verticalDensity = denseBody * 0.63 + luminousSkin * 0.46 + corona * 0.13;
        verticalDensity *= mix(0.76, 1.20, microTurbulence);

        // A dark equatorial dust lane creates parallax between both bright surfaces.
        float midPlaneDust = mix(0.44, 1.0, smoothstep(0.05, 0.46, heightNorm));
        midPlaneDust = mix(midPlaneDust, 1.0, heat * 0.58);

        vec3 tangent = safeNormalize(cross(n, radialDirection)) * sign(u_spin == 0.0 ? 1.0 : u_spin);
        vec3 toCamera = safeNormalize(-pointCamera);
        float beta = clamp((0.48 * sqrt(u_innerRadius / radius)) * u_doppler, 0.0, 0.72);
        float gamma = inversesqrt(max(1.0 - beta * beta, 0.08));
        float doppler = 1.0 / max(gamma * (1.0 - beta * dot(tangent, toCamera)), 0.22);
        float beaming = clamp(doppler * doppler * doppler, 0.22, 4.2);

        float gravityShift = sqrt(max(1.0 - 1.0 / max(radius, 1.001), 0.025));
        float redshift = mix(1.0, gravityShift, saturate(u_redshift));
        vec3 color = temperatureGradient(normalizedRadius, heat * redshift);
        color.r *= mix(1.18, 1.0, redshift);
        color.gb *= mix(0.62, 1.0, redshift);
        color = mix(color, vec3(dot(color, vec3(0.299, 0.587, 0.114))) * vec3(1.04, 1.08, 1.16),
                    saturate((doppler - 1.0) * 0.38));

        float radialEmissivity = pow(u_innerRadius / max(radius, u_innerRadius), 1.42);
        float innerWall = exp(-abs(radius - (u_innerRadius + 0.18)) * 3.7);
        float opticalDepth = edge * verticalDensity * density *
                             (0.56 + radialEmissivity * 0.78) * travelLength;
        float alpha = 1.0 - exp(-opticalDepth * 1.48);
        float photosphere = denseBody * 0.52 + luminousSkin * 0.88 + corona * 0.18;
        float radiance = photosphere * density * radialEmissivity * (0.42 + heat * 1.58);
        radiance += innerWall * (0.34 + luminousSkin * 0.76);
        float softGlow = corona * edge * (0.025 + clouds * 0.055 + shear * 0.035) * u_glow;
        vec3 emission = color * (alpha * radiance * beaming * redshift * midPlaneDust +
                                 softGlow * travelLength) * u_brightness;
        return vec4(emission, alpha);
    }

    void main() {
        if (u_rs <= 0.0 || u_boundRadius <= 1.0) discard;

        vec3 rayDirection = safeNormalize(v_pos);
        // Reconstruct the exact front intersection with the mathematical bounding sphere.
        // Starting on the rasterized mesh surface exposes its triangle facets as radial wedges.
        vec3 cameraToCenter = -u_center;
        float boundRadiusWorld = u_boundRadius * u_rs;
        float rayB = dot(rayDirection, cameraToCenter);
        float rayC = dot(cameraToCenter, cameraToCenter) - boundRadiusWorld * boundRadiusWorld;
        float rayDiscriminant = rayB * rayB - rayC;
        if (rayDiscriminant <= 0.0) discard;
        float entryDistance = -rayB - sqrt(rayDiscriminant);
        if (entryDistance <= 0.0) discard;
        vec3 position = (rayDirection * entryDistance - u_center) / u_rs;
        vec3 diskNormal = safeNormalize(u_diskNormal);
        float minimumRadius = 1.0e9;
        vec3 accumulated = vec3(0.0);
        float accumulatedAlpha = 0.0;
        float stepScale = 36.0 / float(max(u_steps, 1));
        // One stable sub-pixel offset removes coherent shell banding without the
        // sparkling produced by independently randomising every integration step.
        float rayJitter = hash21(gl_FragCoord.xy * 0.75487766);
        position += rayDirection * (rayJitter - 0.5) * stepScale * 0.54;

        for (int stepIndex = 0; stepIndex < MAX_STEPS; ++stepIndex) {
            if (stepIndex >= u_steps) break;
            float radius = max(length(position), 1.0e-4);
            minimumRadius = min(minimumRadius, radius);

            float nearField = 1.0 - smoothstep(1.22, 6.5, radius);
            float stepLength = mix(0.70, 0.105, nearField) * stepScale;
            vec3 towardCenter = -position / radius;
            float strongDenominator = max(radius - 1.10, 0.22);
            float curvature = u_lensing * (0.20 / max(radius * radius, 0.08) +
                                             0.115 / (strongDenominator * strongDenominator));
            rayDirection = safeNormalize(rayDirection + towardCenter * curvature * stepLength);

            float marchedLength = stepLength;
            vec3 previousPosition = position;
            position += rayDirection * marchedLength;
            vec3 samplePosition = mix(previousPosition, position, 0.5);
            vec3 sampleCamera = u_center + samplePosition * u_rs;
            vec4 sampleValue = diskEmission(samplePosition, sampleCamera, marchedLength);
            if (sampleValue.a > 0.0001 || dot(sampleValue.rgb, sampleValue.rgb) > 1.0e-8) {
                float visibility = 1.0 - accumulatedAlpha;
                accumulated += sampleValue.rgb * visibility;
                accumulatedAlpha += sampleValue.a * visibility;
                // Once the volume is optically opaque, later samples cannot affect
                // the image. Stop instead of tracing the hidden half of the ray.
                if (accumulatedAlpha > 0.995 && minimumRadius > 3.05) break;
            }

            radius = length(position);
            minimumRadius = min(minimumRadius, radius);
            if (radius < 0.985) break;
            if (stepIndex > 2 && radius > u_boundRadius && dot(position, rayDirection) > 0.0) break;
        }

        float criticalRadius = clamp(1.0 + 1.60 * u_lensing, 1.10, 2.72);
        bool captured = minimumRadius < criticalRadius;
        float derivativeWidth = max(fwidth(minimumRadius) * 1.05, 0.013);
        float ringCore = exp(-pow((minimumRadius - 2.598) / derivativeWidth, 2.0));
        float ringHalo = exp(-abs(minimumRadius - 2.598) * 8.2) * 0.31 * u_glow;
        float secondaryRing = exp(-pow((minimumRadius - 2.34) / max(derivativeWidth * 1.55, 0.031), 2.0)) * 0.13;
        float outerEcho = exp(-pow((minimumRadius - 2.86) / max(derivativeWidth * 2.10, 0.052), 2.0)) * 0.075;
        float ringAngle = atan(v_pos.y, v_pos.x);
        float angularVariation = 0.94 + 0.06 * valueNoise(vec2(ringAngle * 3.1, u_time * 0.09));
        float ringDoppler = 0.5 + 0.5 * cos(ringAngle - u_time * 0.025);
        vec3 ringTint = mix(vec3(1.18, 0.78, 0.58), vec3(0.91, 1.06, 1.31), ringDoppler * u_doppler);
        vec3 ringColor = ringTint * (ringCore + ringHalo + secondaryRing + outerEcho) *
                         u_photonBrightness * angularVariation;
        accumulated += ringColor;
        accumulatedAlpha = max(accumulatedAlpha, saturate(ringCore * 0.96 + ringHalo * 0.55 + secondaryRing + outerEcho));

        // A hit launches a lens-distorted shell from the photon sphere through
        // the accretion volume. It is analytic, so it costs almost nothing.
        float shockRadius = mix(2.82, u_boundRadius * 0.90, u_impactPhase);
        float shockWidth = mix(0.075, 0.48, u_impactPhase);
        float shockWave = exp(-pow((minimumRadius - shockRadius) / shockWidth, 2.0)) * u_impact;
        vec3 shockTint = mix(vec3(1.18, 0.70, 0.48), vec3(0.70, 1.02, 1.42), u_impactPhase);
        accumulated += shockTint * shockWave * (0.88 + u_glow * 0.34);
        accumulatedAlpha = max(accumulatedAlpha, saturate(shockWave * 0.72));

        if (captured) {
            out_color = vec4(accumulated * u_opacity, u_opacity);
            return;
        }

        float alpha = saturate(accumulatedAlpha * u_opacity);
        if (alpha < 0.003 && dot(accumulated, accumulated) < 1.0e-7) discard;
        out_color = vec4(accumulated * u_opacity, alpha);
    }
""".trimIndent()

// Versioned shader ids prevent an older duplicate script from releasing resources
// that belong to this renderer when both files are present during an upgrade.
val horizonShader = shader("singularity-stable-v6-horizon", horizonFragmentShader, horizonVertexShader)
val lensingShader = shader("singularity-stable-v6-lensing", lensingFragmentShader, horizonVertexShader)
val colorShader = shader("singularity-stable-v6-plasma", colorFragmentShader, colorVertexShader)

val positionFormat = gpu.format(VertexAttribute.floats(3))
val coloredFormat = gpu.format(VertexAttribute.floats(3), VertexAttribute.color())

val horizonMesh = gpu.indexedMesh(positionFormat)
val lensingMesh = gpu.indexedMesh(positionFormat)
val particleMesh = gpu.indexedMesh(coloredFormat)
val fallbackMesh = gpu.indexedMesh(coloredFormat)

val horizonPipeline = gpu.pipeline(
    horizonShader, DrawMode.TRIANGLES, BlendMode.OFF, DepthMode.TEST_AND_WRITE, true
)
val lensingPipeline = gpu.pipeline(
    lensingShader, DrawMode.TRIANGLES, BlendMode.ALPHA, DepthMode.TEST, true
)
val particlePipeline = gpu.pipeline(
    colorShader, DrawMode.TRIANGLES, BlendMode.ADDITIVE, DepthMode.TEST, false
)
val fallbackPipeline = gpu.pipeline(
    colorShader, DrawMode.TRIANGLES, BlendMode.ALPHA, DepthMode.TEST, false
)

val horizonPass = gpu.renderType(horizonPipeline, horizonMesh)
val lensingPass = gpu.renderType(lensingPipeline, lensingMesh)
val particlePass = gpu.renderType(particlePipeline, particleMesh)
val fallbackPass = gpu.renderType(fallbackPipeline, fallbackMesh)

var petPosition: Vec? = null
var previousPetPosition: Vec? = null
var petVelocity = Vec.ZERO
var petForward = Vec(0.0, 0.0, 1.0)
var petDiskNormal = Vec(0.0, 1.0, 0.0)
var shaderErrorReported = false
var rendererAcceptingFrames = true
var closedResourceFaultReported = false
val animationOriginNanos = client.nanos()
val orbitSessionSeed = fract((animationOriginNanos % 1_000_000_007L).toDouble() * 0.000000119)
var orbitTimeOffset = 0.0
var flightOrbitPhase = orbitSessionSeed * PI * 2.0
var flightOrbitRate = 0.14
var pendingFlightOrbitPhase = flightOrbitPhase
var flightAttached = false
var wasElytraFlight = false

// A versioned channel/shape must never consume cached state produced by older
// incompatible releases. Every client using this file speaks protocol v4.
val petSyncShape = PartyShape.builder("pet_v4")
    .intField(1, "protocol", 4)
    .boolField(2, "active", false)
    .vecField(3, "center")
    .vecField(4, "normal")
    .stringField(5, "dimension", "")
    .intField(6, "sequence", 0)
    .build()
val petSyncChannel = party.channel("singularity:pet_v4")
val remoteCenters = mutableMapOf<String, Vec>()
val remoteTargetCenters = mutableMapOf<String, Vec>()
val remoteNormals = mutableMapOf<String, Vec>()
val remoteTargetNormals = mutableMapOf<String, Vec>()
val remoteVelocities = mutableMapOf<String, Vec>()
val remoteFrameNanos = mutableMapOf<String, Long>()
val remoteDimensions = mutableMapOf<String, String>()
val remoteUpdatedAt = mutableMapOf<String, Long>()
val remoteSequences = mutableMapOf<String, Int>()
var lastPartyPublishTick = -1000L
var sharedStatePublished = false
var partySyncReadyReported = false
var partyFaultReported = false
var partySequence = 0
val remoteSeenKeys = mutableSetOf<String>()
var impactStartedNanos = 0L
var impactPower = 0.0
var impactSpinSign = 1.0
var impactCombo = 0
var lastImpactMillis = -10000L
var jumpStartedNanos = 0L
var jumpRelocated = false
var pendingOrbitTimeOffset = 0.0

fun reportPartyFault(stage: String, error: Throwable) {
    if (partyFaultReported) return
    partyFaultReported = true
    val detail = error.message ?: "unknown party exception"
    log.warn("Party Sync $stage was ignored safely: $detail")
}

fun removeRemotePet(key: String) {
    remoteCenters.remove(key)
    remoteTargetCenters.remove(key)
    remoteNormals.remove(key)
    remoteTargetNormals.remove(key)
    remoteVelocities.remove(key)
    remoteFrameNanos.remove(key)
    remoteDimensions.remove(key)
    remoteUpdatedAt.remove(key)
    remoteSequences.remove(key)
}

fun clearRemotePets() {
    remoteCenters.clear()
    remoteTargetCenters.clear()
    remoteNormals.clear()
    remoteTargetNormals.clear()
    remoteVelocities.clear()
    remoteFrameNanos.clear()
    remoteDimensions.clear()
    remoteUpdatedAt.clear()
    remoteSequences.clear()
    remoteSeenKeys.clear()
}

fun validSharedVec(value: Vec): Boolean =
    value.x().isFinite() && value.y().isFinite() && value.z().isFinite() &&
        abs(value.x()) <= 3.0e7 && abs(value.y()) <= 4096.0 && abs(value.z()) <= 3.0e7

fun clearSharedPet() {
    if (!sharedStatePublished) return
    try {
        if (party.connected() && party.inParty()) petSyncChannel.clearState(petSyncShape)
    } catch (error: Throwable) {
        reportPartyFault("clear", error)
    } finally {
        sharedStatePublished = false
    }
}

fun publishSharedPet() {
    try {
        if (!partySync || !visuals || !inGame || !party.connected() || !party.inParty()) {
            clearSharedPet()
            return
        }
        val center = petPosition ?: return
        val normal = safeNormalize(petDiskNormal)
        if (!validSharedVec(center) || !validSharedVec(normal)) return
        partySequence = (partySequence + 1) and 0x3fffffff
        val stateResult = petSyncChannel.publishState(petSyncShape) { writer ->
            writer
                .set("protocol", 4)
                .set("active", true)
                .set("center", center)
                .set("normal", normal)
                .set("dimension", world.dimension())
                .set("sequence", partySequence)
        }
        // State provides a cached snapshot for late subscribers. The targeted
        // event is a live fan-out that bypasses role-dependent state routing.
        val eventResult = petSyncChannel.sendEvent(petSyncShape, PartyTarget.all()) { writer ->
            writer
                .set("protocol", 4)
                .set("active", true)
                .set("center", center)
                .set("normal", normal)
                .set("dimension", world.dimension())
                .set("sequence", partySequence)
        }
        if (stateResult.ok() || eventResult.ok()) {
            sharedStatePublished = true
            if (!partySyncReadyReported) {
                partySyncReadyReported = true
                log.info("Party Sync v4 active; outbound heartbeat confirmed")
            }
        }
    } catch (error: Throwable) {
        sharedStatePublished = false
        reportPartyFault("publish", error)
    }
}

fun partySenderKey(sender: nursultan.party.PartyMember): String {
    val login = sender.login().trim()
    return if (login.isNotEmpty()) login else sender.name().trim()
}

fun receiveSharedPet(message: nursultan.party.PartyShapedMessage) {
    try {
        val sender = message.sender()
        if (sender.self()) return
        val key = partySenderKey(sender)
        val protocol = message.intValue("protocol")
        val active = message.booleanValue("active")
        val center = message.vec("center")
        val normal = message.vec("normal")
        val dimension = message.string("dimension")
        val sequence = message.intValue("sequence")
        val normalLength = normal.squaredLength()
        if (!partySync || protocol != 4 || !active || sequence < 0 || key.isBlank() || key.length > 64 ||
            !validSharedVec(center) || !validSharedVec(normal) ||
            normalLength < 0.25 || normalLength > 4.0 || dimension.length > 96) {
            onClientThread { removeRemotePet(key) }
            return
        }

        val now = client.millis()
        val safeNormal = normal.normalize()
        onClientThread {
            try {
                if (remoteSequences[key] == sequence) return@onClientThread
                remoteSequences[key] = sequence
                val previousTarget = remoteTargetCenters[key]
                val previousUpdate = remoteUpdatedAt[key]
                val previousDimension = remoteDimensions[key]
                if (previousTarget != null && previousUpdate != null && previousDimension == dimension) {
                    val deltaSeconds = clampD((now - previousUpdate).toDouble() / 1000.0, 0.025, 0.75)
                    var measuredVelocity = center.subtract(previousTarget).multiply(1.0 / deltaSeconds)
                    val maximumVelocity = 48.0
                    if (measuredVelocity.squaredLength() > maximumVelocity * maximumVelocity) {
                        measuredVelocity = measuredVelocity.normalize().multiply(maximumVelocity)
                    }
                    remoteVelocities[key] = lerpVec(
                        remoteVelocities[key] ?: measuredVelocity,
                        measuredVelocity,
                        0.58
                    )
                } else {
                    remoteVelocities[key] = Vec.ZERO
                }
                if (!remoteCenters.containsKey(key)) {
                    remoteCenters[key] = center
                    remoteNormals[key] = safeNormal
                }
                remoteTargetCenters[key] = center
                remoteTargetNormals[key] = safeNormal
                remoteDimensions[key] = dimension
                remoteUpdatedAt[key] = now
                if (remoteSeenKeys.add(key)) log.info("Party Sync v4 inbound confirmed from $key")
            } catch (error: Throwable) {
                removeRemotePet(key)
                reportPartyFault("apply", error)
            }
        }
    } catch (error: Throwable) {
        reportPartyFault("receive", error)
    }
}

fun removeSharedPet(message: nursultan.party.PartyMessage) {
    try {
        val key = partySenderKey(message.sender())
        onClientThread { removeRemotePet(key) }
    } catch (error: Throwable) {
        reportPartyFault("remove", error)
    }
}

// In this Party API build leader state has a dedicated receive route. Listening
// to both rules makes synchronization symmetrical for owners and regular members.
petSyncChannel.onState(petSyncShape, SenderRule.ANY_MEMBER) { message ->
    receiveSharedPet(message)
}
petSyncChannel.onState(petSyncShape, SenderRule.LEADER_ONLY) { message ->
    receiveSharedPet(message)
}
petSyncChannel.onEvent(petSyncShape, SenderRule.ANY_MEMBER) { message ->
    receiveSharedPet(message)
}
petSyncChannel.onEvent(petSyncShape, SenderRule.LEADER_ONLY) { message ->
    receiveSharedPet(message)
}
petSyncChannel.onStateCleared(petSyncShape, SenderRule.ANY_MEMBER) { message ->
    removeSharedPet(message)
}
petSyncChannel.onStateCleared(petSyncShape, SenderRule.LEADER_ONLY) { message ->
    removeSharedPet(message)
}

fun horizontalForward(): Vec {
    val look = player.rotation().direction()
    return safeNormalize(Vec(look.x(), 0.0, look.z()), petForward)
}

fun orbitRandom(channel: Double): Double =
    fract(sin((orbitSessionSeed + channel * 17.173) * 91.731) * 43758.5453123)

fun orbitSeconds(): Double =
    (client.nanos() - animationOriginNanos).toDouble() / 1_000_000_000.0

fun currentImpactPhase(): Double {
    if (impactStartedNanos <= 0L || impactPower <= 0.0) return 1.0
    val age = (client.nanos() - impactStartedNanos).toDouble() / 1_000_000_000.0
    return clampD(age / 0.92, 0.0, 1.0)
}

fun currentImpactEnvelope(): Double {
    val phase = currentImpactPhase()
    if (phase >= 1.0) return 0.0
    val decay = 1.0 - smoothstepD(0.0, 1.0, phase)
    return impactPower * decay
}

fun singularityJumpActive(): Boolean = jumpStartedNanos > 0L

fun currentJumpPhase(): Double {
    if (!singularityJumpActive()) return 1.0
    val age = (client.nanos() - jumpStartedNanos).toDouble() / 1_000_000_000.0
    return clampD(age / 0.94, 0.0, 1.0)
}

fun currentJumpScale(): Double {
    if (!singularityJumpActive()) return 1.0
    val phase = currentJumpPhase()
    val scale = if (phase < 0.5) {
        1.0 - smoothstepD(0.0, 0.48, phase)
    } else {
        smoothstepD(0.52, 1.0, phase)
    }
    return max(0.018, scale)
}

fun beginSingularityJump(salt: Double) {
    if (singularityJumpActive()) return
    val randomUnit = fract(sin(salt * 12.9898 + orbitSessionSeed * 78.233) * 43758.5453)
    val randomAdvance = 3.2 + randomUnit * 13.0
    pendingOrbitTimeOffset = orbitTimeOffset + randomAdvance
    pendingFlightOrbitPhase = flightOrbitPhase + PI * (0.72 + randomUnit * 1.16)
    jumpStartedNanos = client.nanos()
    jumpRelocated = false
    petVelocity = Vec.ZERO
    petPosition?.let { center ->
        world.playSound("minecraft:block.respawn_anchor.charge", center, 0.42f, 1.72f)
    }
}

fun dotVec(a: Vec, b: Vec): Double =
    a.x() * b.x() + a.y() * b.y() + a.z() * b.z()

fun tryHitSingularity(): Boolean {
    if (!visuals || !inGame || game.screenOpen() || singularityJumpActive()) return false
    val center = petPosition ?: return false
    val eye = player.eyePosition()
    val direction = safeNormalize(player.rotation().direction(), Vec(0.0, 0.0, 1.0))
    val toCenter = center.subtract(eye)
    val alongRay = dotVec(toCenter, direction)
    if (alongRay < 0.15 || alongRay > 6.25) return false
    val closestSquared = max(0.0, toCenter.squaredLength() - alongRay * alongRay)
    val interactionRadius = 0.40 * size.toDouble()
    if (closestSquared > interactionRadius * interactionRadius) return false

    val now = client.millis()
    impactCombo = if (now - lastImpactMillis <= 1250L) (impactCombo % 3) + 1 else 1
    lastImpactMillis = now
    impactStartedNanos = client.nanos()
    impactPower = if (impactCombo == 3) 1.42 else 0.78 + impactCombo * 0.13
    impactSpinSign = if (impactCombo % 2 == 0) -1.0 else 1.0

    val sparkOrigin = center.subtract(direction.multiply(interactionRadius * 0.62))
    val sparkSalt = (now % 100000L).toDouble() * 0.0137
    if (impactCombo == 3) {
        beginSingularityJump(sparkSalt + now.toDouble() * 0.001)
    } else {
        val (kickU, kickV) = basisFor(direction)
        val sideKick = (deterministic(impactCombo, sparkSalt + 3.0) - 0.5) * (3.2 + impactCombo * 0.65)
        val liftKick = 1.15 + deterministic(impactCombo, sparkSalt + 7.0) * 1.35
        val impulse = 6.8 + impactCombo * 1.35
        val kickDirection = safeNormalize(
            direction.multiply(1.0)
                .add(kickU.multiply(sideKick / impulse))
                .add(kickV.multiply((deterministic(impactCombo, sparkSalt + 11.0) - 0.5) * 0.32)),
            direction
        )
        petVelocity = kickDirection.multiply(impulse).add(0.0, liftKick, 0.0)
    }

    interaction.swing(player.usingHand())
    world.playSound("minecraft:entity.player.attack.crit", sparkOrigin, 0.72f, 1.04f + impactCombo * 0.09f)
    if (impactCombo == 3) {
        world.playSound("minecraft:entity.generic.explode", sparkOrigin, 0.34f, 1.72f)
        world.playSound("minecraft:block.amethyst_block.chime", sparkOrigin, 0.58f, 1.28f)
    }
    return true
}

fun targetPositionAt(seconds: Double): Vec {
    val speed = orbitSpeed.toDouble()
    val wander = orbitWander.toDouble()
    val golden = 1.618033988749895
    val rootTwo = 1.4142135623730951
    val phaseA = orbitRandom(1.0) * PI * 2.0 + sideOffset.toDouble() * 0.17
    val phaseB = orbitRandom(2.0) * PI * 2.0 + backOffset.toDouble() * 0.23
    val phaseC = orbitRandom(3.0) * PI * 2.0
    val phaseD = orbitRandom(4.0) * PI * 2.0

    val theta = seconds * speed * (0.86 + orbitRandom(5.0) * 0.18) + phaseA
    val radialPhase = seconds * speed * golden * 0.37 + phaseB
    val latitudePhase = seconds * speed * rootTwo * 0.61 + phaseC
    val precession = seconds * speed * (0.105 + orbitRandom(6.0) * 0.055) + phaseD
    val breathing = 1.0 + wander * (
        0.13 * sin(radialPhase) +
            0.055 * sin(radialPhase * rootTwo + phaseC)
        )
    val radius = orbitRadius.toDouble() * breathing
    val eccentricity = wander * (0.10 + orbitRandom(7.0) * 0.08)
    val xPlane = cos(theta) * radius * (1.0 + eccentricity)
    val zPlane = sin(theta) * radius * (1.0 - eccentricity)
    val latitude = wander * radius * (
        0.24 * sin(latitudePhase) +
            0.11 * sin(theta * golden + phaseB) +
            0.045 * sin(seconds * speed * 0.071 + phaseD)
        )
    val cosNode = cos(precession)
    val sinNode = sin(precession)
    val worldX = xPlane * cosNode - zPlane * sinNode
    val worldZ = xPlane * sinNode + zPlane * cosNode

    return player.eyePosition().add(
        worldX,
        verticalOffset.toDouble() + latitude,
        worldZ
    )
}

fun targetPosition(): Vec = targetPositionAt(orbitSeconds() + orbitTimeOffset)

fun elytraTargetPosition(
    phase: Double = flightOrbitPhase,
    eyePosition: Vec = player.eyePosition()
): Vec {
    val look = safeNormalize(player.rotation().direction(), Vec(0.0, 0.0, 1.0))
    val referenceUp = if (abs(look.y()) < 0.94) Vec(0.0, 1.0, 0.0) else Vec(1.0, 0.0, 0.0)
    val right = safeNormalize(cross(look, referenceUp), Vec(1.0, 0.0, 0.0))
    val orbitUp = safeNormalize(cross(right, look), Vec(0.0, 1.0, 0.0))
    val radius = clampD(orbitRadius.toDouble() * 0.52, 1.05, 1.90)
    // Keep the whole flight orbit behind the view plane. The safety distance
    // grows with pet size so even the outer glow cannot sweep across the screen.
    val rearClearance = 1.15 + size.toDouble() * 1.35
    val bodyCenter = eyePosition.subtract(look.multiply(rearClearance)).add(0.0, -0.72, 0.0)
    val forwardWobble = sin(phase * 0.47 + orbitSessionSeed * PI * 2.0) * radius * 0.16
    return bodyCenter
        .add(right.multiply(cos(phase) * radius))
        .add(orbitUp.multiply(sin(phase) * radius))
        .add(look.multiply(forwardWobble))
}

fun companionTargetPosition(): Vec =
    if (player.gliding()) elytraTargetPosition() else targetPosition()

fun updateSingularityJump() {
    if (!singularityJumpActive()) return
    val phase = currentJumpPhase()
    if (!jumpRelocated && phase >= 0.5) {
        if (player.gliding()) {
            flightOrbitPhase = pendingFlightOrbitPhase
        } else {
            orbitTimeOffset = pendingOrbitTimeOffset
        }
        val destination = companionTargetPosition()
        petPosition = destination
        previousPetPosition = destination
        petVelocity = Vec.ZERO
        petForward = orbitTangent()
        jumpRelocated = true
        world.playSound("minecraft:entity.enderman.teleport", destination, 0.48f, 1.38f)
    } else if (jumpRelocated) {
        // Keep the emerging singularity attached to its new orbital point while
        // the expansion half of the transition is still playing.
        val destination = companionTargetPosition()
        petPosition = destination
        previousPetPosition = destination
    }
    if (phase >= 1.0) {
        jumpStartedNanos = 0L
        jumpRelocated = false
        pendingOrbitTimeOffset = orbitTimeOffset
    }
}

fun orbitTangent(): Vec {
    val now = orbitSeconds() + orbitTimeOffset
    val current = targetPositionAt(now)
    val next = targetPositionAt(now + 0.05)
    return safeNormalize(
        Vec(next.x() - current.x(), 0.0, next.z() - current.z()),
        horizontalForward()
    )
}

fun desiredDiskNormal(forward: Vec, movementBlocksPerTick: Double): Vec {
    val reactionDegrees = clampD(movementBlocksPerTick * 12.0, 0.0, 7.0)
    val precession = orbitSeconds() * orbitSpeed.toDouble() * 0.19 + orbitRandom(8.0) * PI * 2.0
    val radians = Math.toRadians(
        (diskTilt + reactionDegrees + sin(precession * 0.61) * 3.5 * orbitWander).toDouble()
    )
    val node = atan2(forward.z(), forward.x()) + precession
    return safeNormalize(Vec(
        cos(node) * sin(radians),
        cos(radians),
        sin(node) * sin(radians)
    ))
}

fun resetPet() {
    petPosition = null
    previousPetPosition = null
    petVelocity = Vec.ZERO
    impactStartedNanos = 0L
    impactPower = 0.0
    impactCombo = 0
    jumpStartedNanos = 0L
    jumpRelocated = false
    pendingOrbitTimeOffset = orbitTimeOffset
    pendingFlightOrbitPhase = flightOrbitPhase
    flightAttached = false
    wasElytraFlight = false
}

fun snapPet() {
    if (!inGame) return
    val forward = orbitTangent()
    val target = targetPosition()
    petPosition = target
    previousPetPosition = target
    petVelocity = Vec.ZERO
    petForward = forward
    petDiskNormal = desiredDiskNormal(forward, player.velocity().length())
}

fun fillSphere(mesh: Mesh, template: SphereTemplate, center: Vec, radius: Double) {
    val vertices = mesh.verts()
    var positionIndex = 0
    while (positionIndex < template.positions.size) {
        vertices.putVec3(
            (center.x() + template.positions[positionIndex].toDouble() * radius).toFloat(),
            (center.y() + template.positions[positionIndex + 1].toDouble() * radius).toFloat(),
            (center.z() + template.positions[positionIndex + 2].toDouble() * radius).toFloat()
        ).next()
        positionIndex += 3
    }
    val indices = mesh.idx() ?: return
    for (index in template.indices) indices.put(index)
}

fun setMatrices(shaderHandle: Shader, event: Render3DEvent) {
    shaderHandle.setMat4("u_view", event.viewMatrix())
    shaderHandle.setMat4("u_projection", event.projectionMatrix())
}

fun basisFor(normal: Vec): Pair<Vec, Vec> {
    val reference = if (abs(normal.y()) < 0.92) Vec(0.0, 1.0, 0.0) else Vec(1.0, 0.0, 0.0)
    val axisU = safeNormalize(cross(reference, normal), Vec(1.0, 0.0, 0.0))
    return axisU to safeNormalize(cross(normal, axisU), Vec(0.0, 0.0, 1.0))
}

fun putColoredVertex(vertices: VertexWriter, x: Double, y: Double, z: Double, color: Int): Int =
    vertices.putVec3(x.toFloat(), y.toFloat(), z.toFloat()).putColor(color).next()

fun fillParticles(
    center: Vec,
    normal: Vec,
    axisU: Vec,
    axisV: Vec,
    rs: Double,
    outerRadius: Double,
    time: Double,
    count: Int,
    opacity: Double
) {
    val vertices = particleMesh.verts()
    val indices = particleMesh.idx() ?: return
    val spinSign = if (diskSpinSpeed < 0.0f) -1.0 else 1.0
    val spinMagnitude = max(abs(diskSpinSpeed.toDouble()), 0.08)

    for (index in 0 until count.coerceAtMost(particleSeeds.size)) {
        val seed = particleSeeds[index]
        val life = fract(time * seed.lifeRate + seed.phase / (PI * 2.0))
        val fade = sin(PI * life).let { it * it }
        if (fade < 0.002) continue

        val radiusNorm = 3.25 + (outerRadius - 3.25) * seed.radius
        val angularVelocity = spinSign * spinMagnitude * 1.15 * (3.25 / radiusNorm).pow(1.5)
        val angle = seed.phase + time * angularVelocity + life * 0.45
        val eccentricScale = 1.0 + seed.eccentricity * sin(angle * 2.0 + seed.phase)
        val localRadius = radiusNorm * rs * eccentricScale
        val lift = seed.lift * rs * (0.10 + life * life * 0.75)
        val x = cos(angle) * localRadius
        val z = sin(angle) * localRadius
        val px = center.x() + axisU.x() * x + axisV.x() * z + normal.x() * lift
        val py = center.y() + axisU.y() * x + axisV.y() * z + normal.y() * lift
        val pz = center.z() + axisU.z() * x + axisV.z() * z + normal.z() * lift
        val radialDirection = safeNormalize(
            axisU.multiply(cos(angle)).add(axisV.multiply(sin(angle))),
            axisU
        )
        val tangent = safeNormalize(cross(normal, radialDirection).multiply(spinSign), axisV)
        val toCamera = safeNormalize(Vec(-px, -py, -pz), normal)
        val ribbonSide = safeNormalize(cross(tangent, toCamera), normal)
        val streakLength = rs * (0.18 + seed.heat * 0.34) * (0.72 + fade * 0.72)
        val streakWidth = rs * (0.018 + seed.heat * 0.020) * (0.70 + fade * 0.45)
        val alpha = (230.0 * fade * opacity * (0.38 + seed.heat * 0.62)).roundToInt()
        val heat = seed.heat
        val red = 255
        val green = (104.0 + heat * 151.0).roundToInt()
        val blue = (12.0 + heat.pow(2.4) * 243.0).roundToInt()

        fun ribbon(width: Double, length: Double, alphaScale: Double, coreBoost: Double) {
            val tail = Vec(
                px - tangent.x() * length * 0.72,
                py - tangent.y() * length * 0.72,
                pz - tangent.z() * length * 0.72
            )
            val head = Vec(
                px + tangent.x() * length * 0.38,
                py + tangent.y() * length * 0.38,
                pz + tangent.z() * length * 0.38
            )
            val sideX = ribbonSide.x() * width
            val sideY = ribbonSide.y() * width
            val sideZ = ribbonSide.z() * width
            val tipColor = argb((alpha * alphaScale * 0.06).roundToInt(), red, green, blue)
            val bodyColor = argb(
                (alpha * alphaScale).roundToInt(),
                red,
                (green * coreBoost).roundToInt(),
                (blue * coreBoost).roundToInt()
            )
            val v0 = putColoredVertex(vertices, tail.x(), tail.y(), tail.z(), tipColor)
            val v1 = putColoredVertex(vertices, px + sideX, py + sideY, pz + sideZ, bodyColor)
            val v2 = putColoredVertex(vertices, head.x(), head.y(), head.z(), tipColor)
            val v3 = putColoredVertex(vertices, px - sideX, py - sideY, pz - sideZ, bodyColor)
            indices.putQuad(v0, v1, v2, v3)
        }

        // A soft plasma envelope plus a narrow white-hot core. Both are camera-facing,
        // so particles read as orbiting gas streaks instead of crossed square sprites.
        ribbon(streakWidth * 2.6, streakLength * 1.16, 0.24, 0.92)
        ribbon(streakWidth, streakLength, 0.92, 1.08)
    }
}

fun fallbackColor(radial: Double, alpha: Int): Int {
    val t = clampD(radial, 0.0, 1.0)
    val white = smoothstepD(0.42, 0.0, t)
    val red = 255
    val green = (58.0 + (1.0 - t) * 167.0 + white * 30.0).roundToInt()
    val blue = (5.0 + (1.0 - t) * 42.0 + white * 190.0).roundToInt()
    return argb(alpha, red, green, blue)
}

fun fillFallbackDisk(center: Vec, normal: Vec, axisU: Vec, axisV: Vec, rs: Double, opacity: Double) {
    val vertices = fallbackMesh.verts()
    val indices = fallbackMesh.idx() ?: return
    val segments = 96
    val bands = 9
    val inner = 3.15
    val outer = diskRadius.toDouble()
    for (band in 0 until bands) {
        val r0n = inner + (outer - inner) * band.toDouble() / bands.toDouble()
        val r1n = inner + (outer - inner) * (band + 1).toDouble() / bands.toDouble()
        val radial = (r0n + r1n - inner * 2.0) / (2.0 * (outer - inner))
        val edge = smoothstepD(0.0, 0.08, radial) * (1.0 - smoothstepD(0.78, 1.0, radial))
        val color = fallbackColor(radial, (225.0 * edge * opacity).roundToInt())
        for (segment in 0 until segments) {
            val a0 = segment.toDouble() / segments.toDouble() * PI * 2.0
            val a1 = (segment + 1).toDouble() / segments.toDouble() * PI * 2.0
            fun point(radius: Double, angle: Double): Vec = center
                .add(axisU.multiply(cos(angle) * radius * rs))
                .add(axisV.multiply(sin(angle) * radius * rs))
            val p0 = point(r0n, a0)
            val p1 = point(r1n, a0)
            val p2 = point(r1n, a1)
            val p3 = point(r0n, a1)
            val v0 = putColoredVertex(vertices, p0.x(), p0.y(), p0.z(), color)
            val v1 = putColoredVertex(vertices, p1.x(), p1.y(), p1.z(), color)
            val v2 = putColoredVertex(vertices, p2.x(), p2.y(), p2.z(), color)
            val v3 = putColoredVertex(vertices, p3.x(), p3.y(), p3.z(), color)
            indices.putQuad(v0, v1, v2, v3)
        }
    }
}

onEnable {
    rendererAcceptingFrames = true
    closedResourceFaultReported = false
    partyFaultReported = false
    clearRemotePets()
    lastPartyPublishTick = -1000L
    whenInGame {
        snapPet()
        publishSharedPet()
    }
}

onDisable {
    // Stop stale render callbacks before the runtime releases GPU resources.
    rendererAcceptingFrames = false
    clearSharedPet()
    clearRemotePets()
    resetPet()
}

on<WorldLoadEvent> {
    clearSharedPet()
    clearRemotePets()
    lastPartyPublishTick = -1000L
    resetPet()
}

on<KeyEvent> { event ->
    if (!event.mouse() || !event.matches(Key.MOUSE_1) || !event.pressed()) return@on
    if (tryHitSingularity()) event.cancel()
}

on<ClientTickEvent> {
    try {
    if (!inGame) {
        resetPet()
        return@on
    }
    if (petPosition == null || previousPetPosition == null) {
        snapPet()
        publishSharedPet()
        return@on
    }

    val elytraFlight = player.gliding()
    val flightSpeed = if (elytraFlight) player.velocity().length() else 0.0
    if (elytraFlight && !wasElytraFlight) flightAttached = false
    if (!elytraFlight && wasElytraFlight) flightAttached = false
    wasElytraFlight = elytraFlight
    if (elytraFlight) {
        val wantedOrbitRate = 0.12 + clampD(flightSpeed * 0.14, 0.0, 0.22)
        flightOrbitRate += (wantedOrbitRate - flightOrbitRate) * 0.18
        flightOrbitPhase += flightOrbitRate
    }

    if (singularityJumpActive()) {
        updateSingularityJump()
        if (client.tick() - lastPartyPublishTick >= 3L) {
            lastPartyPublishTick = client.tick()
            publishSharedPet()
        }
        return@on
    }

    if (elytraFlight) {
        val current = petPosition ?: elytraTargetPosition()
        val target = elytraTargetPosition()
        previousPetPosition = current
        if (flightAttached) {
            petPosition = target
            petVelocity = player.velocity().multiply(20.0)
        } else {
            val dt = 0.05
            val error = target.subtract(current)
            val nextOrbitTarget = elytraTargetPosition(flightOrbitPhase + flightOrbitRate)
            val carrierVelocity = player.velocity().multiply(20.0)
                .add(nextOrbitTarget.subtract(target).multiply(20.0))
            val relativeVelocity = petVelocity.subtract(carrierVelocity)
            val acceleration = error.multiply(7.5).subtract(relativeVelocity.multiply(4.2))
            var nextRelativeVelocity = relativeVelocity.add(acceleration.multiply(dt))
            val maximumCatchupSpeed = 9.0 + clampD(flightSpeed * 3.0, 0.0, 3.0)
            if (nextRelativeVelocity.squaredLength() > maximumCatchupSpeed * maximumCatchupSpeed) {
                nextRelativeVelocity = nextRelativeVelocity.normalize().multiply(maximumCatchupSpeed)
            }
            val nextVelocity = carrierVelocity.add(nextRelativeVelocity)
            petVelocity = nextVelocity
            petPosition = current.add(nextVelocity.multiply(dt))
            if (error.squaredLength() < 0.0324) {
                flightAttached = true
            }
        }
        val look = safeNormalize(player.rotation().direction(), petForward)
        val orbitalDirection = safeNormalize((petPosition ?: target).subtract(current), look)
        petForward = safeNormalize(lerpVec(petForward, orbitalDirection, 0.72), orbitalDirection)
        // Flight changes only the companion path. Keep the original disk tilt and
        // precession model so the accretion ring never turns to face the body.
        val wantedNormal = desiredDiskNormal(petForward, flightSpeed)
        petDiskNormal = safeNormalize(lerpVec(petDiskNormal, wantedNormal, 0.09), wantedNormal)
        if (client.tick() - lastPartyPublishTick >= 3L) {
            lastPartyPublishTick = client.tick()
            publishSharedPet()
        }
        return@on
    }

    val target = targetPosition()
    val current = petPosition ?: target
    previousPetPosition = current

    val error = target.subtract(current)
    val rawForward = safeNormalize(Vec(error.x(), 0.0, error.z()), orbitTangent())
    petForward = safeNormalize(lerpVec(petForward, rawForward, 0.12), rawForward)
    if (error.squaredLength() > 144.0) {
        beginSingularityJump(client.millis().toDouble() * 0.001 + error.squaredLength())
    } else {
        val dt = 0.05
        val reactionPhase = currentImpactPhase()
        val freeFlight = 1.0 - smoothstepD(0.18, 0.72, reactionPhase)
        val activeStiffness = followStiffness.toDouble() * (1.0 - freeFlight * 0.91)
        val activeDamping = followDamping.toDouble() * (1.0 - freeFlight * 0.88)
        val acceleration = error.multiply(activeStiffness)
            .subtract(petVelocity.multiply(activeDamping))
        var velocity = petVelocity.add(acceleration.multiply(dt))
        val maximumSpeed = max(maxFollowSpeed.toDouble(), 11.5 * freeFlight)
        if (velocity.squaredLength() > maximumSpeed * maximumSpeed) {
            velocity = velocity.normalize().multiply(maximumSpeed)
        }
        petVelocity = velocity
        petPosition = current.add(velocity.multiply(dt))
    }

    val wantedNormal = desiredDiskNormal(petForward, player.velocity().length())
    petDiskNormal = safeNormalize(lerpVec(petDiskNormal, wantedNormal, 0.09), wantedNormal)

    if (client.tick() - lastPartyPublishTick >= 3L) {
        lastPartyPublishTick = client.tick()
        publishSharedPet()
    }
    } catch (error: Throwable) {
        reportPartyFault("tick", error)
    }
}

fun renderPetInstance(
    event: Render3DEvent,
    centerWorld: Vec,
    baseNormal: Vec,
    motion: Double,
    time: Double,
    particleMultiplier: Double,
    localReaction: Boolean
) {
    val center = centerWorld.subtract(event.camera())
    if (center.squaredLength() > 128.0 * 128.0) return
    val profile = currentProfile()
    val reactionPhase = if (localReaction) currentImpactPhase() else 1.0
    val reactionEnvelope = if (localReaction) currentImpactEnvelope() else 0.0
    val jumpActive = localReaction && singularityJumpActive()
    val jumpPhase = if (jumpActive) currentJumpPhase() else 1.0
    val jumpScale = if (jumpActive) currentJumpScale() else 1.0
    val jumpEnergy = if (jumpActive) sin(PI * jumpPhase).pow(0.72) else 0.0
    val visualReaction = max(reactionEnvelope, jumpEnergy * 1.18)
    val visualReactionPhase = if (jumpActive) jumpPhase else reactionPhase
    val compressionWave = if (reactionEnvelope > 0.0) {
        sin(reactionPhase * PI * 4.0) * reactionEnvelope * (1.0 - reactionPhase) * 0.11
    } else 0.0
    val rs = 0.106 * size.toDouble() * (1.0 + compressionWave) * jumpScale
    val boundRadiusNormalized = diskRadius.toDouble() + 1.35 + glowStrength.toDouble() * 0.35
    val volumeRadius = rs * boundRadiusNormalized
    val cameraDistance = center.length()
    // Never render the front-shell ray marcher through/against the near camera plane.
    // This removes the rectangular volume seam visible when the pet gets too close.
    val opacity = smoothstepD(volumeRadius * 1.08, volumeRadius * 1.42, cameraDistance)
    if (opacity <= 0.001) return

    // Screen-space LOD: close objects already cover far more pixels, so using the
    // full step count there wastes GPU time without adding visible detail. Farther
    // cinematic views retain the complete Ultra profile.
    val projectedLoad = volumeRadius / max(cameraDistance, 0.001)
    val closePressure = smoothstepD(0.18, 0.72, projectedLoad)
    val rayStepFactor = 1.0 - closePressure * 0.58
    val remoteStepFactor = if (localReaction) 1.0 else 0.66
    val minimumSteps = min(if (localReaction) 24 else 16, profile.raySteps)
    val adaptiveRaySteps = (profile.raySteps * rayStepFactor * remoteStepFactor)
        .roundToInt().coerceIn(minimumSteps, profile.raySteps)
    val adaptiveNoiseOctaves = if (!localReaction) {
        min(3, profile.noiseOctaves)
    } else when {
        closePressure > 0.62 -> min(3, profile.noiseOctaves)
        closePressure > 0.30 -> min(4, profile.noiseOctaves)
        else -> profile.noiseOctaves
    }

    val normal = safeNormalize(baseNormal)
    val (axisU, axisV) = basisFor(normal)
    val movementBrightness = 1.0 + clampD(motion * 1.8, 0.0, 0.10)

    // Pass 1: a real, depth-writing, unlit black sphere at r = 1 Rs.
    fillSphere(horizonMesh, profile.sphere, center, rs)
    setMatrices(horizonShader, event)
    horizonPass.draw()

    // Pass 2: batched, camera-facing plasma ribbons. The later lens pass masks captured rays.
    val particleCount = (profile.particleCap * particleAmount * particleMultiplier)
        .roundToInt().coerceIn(0, profile.particleCap)
    if (particleCount > 0) {
        fillParticles(
            center, normal, axisU, axisV, rs, diskRadius.toDouble(), time,
            particleCount, opacity
        )
        setMatrices(colorShader, event)
        particlePass.draw()
    }

    // Pass 3: front surface of a bounding sphere; every fragment integrates a bent camera ray.
    fillSphere(lensingMesh, profile.sphere, center, rs * boundRadiusNormalized)
    setMatrices(lensingShader, event)
    lensingShader
        .set("u_center", center.x().toFloat(), center.y().toFloat(), center.z().toFloat())
        .set("u_diskNormal", normal.x().toFloat(), normal.y().toFloat(), normal.z().toFloat())
        .set("u_rs", rs.toFloat())
        .set("u_boundRadius", boundRadiusNormalized.toFloat())
        .set("u_innerRadius", 3.15f)
        .set("u_outerRadius", diskRadius)
        .set("u_time", time.toFloat())
        .set("u_brightness", (diskBrightness * movementBrightness.toFloat() * (1.0 + visualReaction * 0.86).toFloat()))
        .set("u_temperature", (diskTemperature + visualReaction.toFloat() * 0.20f))
        .set("u_spin", (diskSpinSpeed + (impactSpinSign * reactionEnvelope * 3.4 + jumpEnergy * 9.0).toFloat()))
        .set("u_lensing", lensingStrength)
        .set("u_doppler", dopplerStrength)
        .set("u_redshift", redshiftStrength)
        .set("u_photonBrightness", photonRingBrightness)
        .set("u_glow", glowStrength)
        .set("u_plasma", plasmaAmount)
        .set("u_opacity", opacity.toFloat())
        .set("u_impact", visualReaction.toFloat())
        .set("u_impactPhase", visualReactionPhase.toFloat())
        .set("u_steps", adaptiveRaySteps)
        .set("u_noiseOctaves", adaptiveNoiseOctaves)
    lensingPass.draw()

    val lensError = lensingShader.error()
    if (lensError != null) {
        if (!shaderErrorReported) {
            log.error("Singularity lens shader failed; using the procedural geometry fallback: $lensError")
            shaderErrorReported = true
        }
        fillFallbackDisk(center, normal, axisU, axisV, rs, opacity)
        setMatrices(colorShader, event)
        fallbackPass.draw()
    }
}

on<Render3DEvent> { event ->
    if (!rendererAcceptingFrames) return@on
    try {
    if (!visuals || !inGame) return@on
    val current = petPosition ?: return@on
    val previous = previousPetPosition ?: current
    val time = (client.nanos() - animationOriginNanos).toDouble() / 1_000_000_000.0 * animationSpeed.toDouble()
    val tickDelta = clampF(event.tickDelta(), 0.0f, 1.0f).toDouble()
    val elytraVisual = player.gliding() && flightAttached && !singularityJumpActive()
    val interpolated = if (elytraVisual) {
        // Player positions update at 20 Hz while the camera is interpolated every
        // frame. Reconstruct the same in-between body position to avoid tick jitter.
        val renderEye = player.eyePosition()
            .subtract(player.velocity().multiply(1.0 - tickDelta))
        elytraTargetPosition(
            flightOrbitPhase + flightOrbitRate * tickDelta,
            renderEye
        )
    } else {
        lerpVec(previous, current, tickDelta)
    }
    val motion = player.velocity().length()
    val orbitAmount = if (elytraVisual) 0.0 else 0.038 * smoothstepD(0.22, 0.0, motion)
    val bobAmount = if (elytraVisual) 0.0 else idleBob.toDouble()
    val microPhase = time * 0.23 + sin(time * 0.071) * 1.7
    val centerWorld = interpolated
        .add(petForward.multiply(
            (sin(microPhase) + 0.43 * sin(microPhase * 1.61803398875 + 1.3)) * orbitAmount
        ))
        .add(
            0.0,
            (sin(time * 1.48) + 0.38 * sin(time * 1.48 * 1.41421356237 + 2.1)) * bobAmount,
            0.0
        )
    val wobble = sin(time * 0.22) * Math.toRadians(2.7)
    val localNormal = safeNormalize(petDiskNormal.add(petForward.multiply(wobble)), petDiskNormal)
    renderPetInstance(event, centerWorld, localNormal, motion, time, 1.0, true)

    if (!partySync || !party.connected() || !party.inParty()) return@on
    val now = client.millis()
    remoteUpdatedAt.keys.toList().forEach { key ->
        val updatedAt = remoteUpdatedAt[key]
        if (updatedAt == null || now - updatedAt > 5000L) removeRemotePet(key)
    }

    // Render from a stable snapshot. Distance sorting used to instantiate an
    // inlined comparator inside the frame callback; the script watchdog could
    // isolate that comparator on slower clients even with only a few members.
    val remoteKeysToRender = remoteTargetCenters.keys.toList()
        .filter { key -> remoteDimensions[key] == world.dimension() }
        .take(12)
    remoteKeysToRender.forEach { key ->
            try {
                val targetCenter = remoteTargetCenters[key] ?: return@forEach
                val targetNormal = remoteTargetNormals[key] ?: return@forEach
                val frameNow = client.nanos()
                val previousFrame = remoteFrameNanos[key] ?: frameNow
                remoteFrameNanos[key] = frameNow
                val frameSeconds = clampD(
                    (frameNow - previousFrame).toDouble() / 1_000_000_000.0,
                    0.0,
                    0.10
                )
                val updateAge = clampD(
                    (client.millis() - (remoteUpdatedAt[key] ?: client.millis())).toDouble() / 1000.0,
                    0.0,
                    0.22
                )
                val predictedCenter = targetCenter.add(
                    (remoteVelocities[key] ?: Vec.ZERO).multiply(updateAge)
                )
                val positionBlend = 1.0 - exp(-frameSeconds * 15.0)
                val normalBlend = 1.0 - exp(-frameSeconds * 12.0)
                val center = lerpVec(remoteCenters[key] ?: targetCenter, predictedCenter, positionBlend)
                val normal = safeNormalize(
                    lerpVec(remoteNormals[key] ?: targetNormal, targetNormal, normalBlend),
                    targetNormal
                )
                if (!validSharedVec(center) || !validSharedVec(normal)) {
                    removeRemotePet(key)
                    return@forEach
                }
                remoteCenters[key] = center
                remoteNormals[key] = normal
                renderPetInstance(event, center, normal, 0.0, time, 0.55, false)
            } catch (error: Throwable) {
                if ((error.message ?: "").contains("closed", ignoreCase = true)) throw error
                removeRemotePet(key)
                reportPartyFault("remote-render", error)
            }
        }
    } catch (error: Throwable) {
        val message = error.message ?: error.toString()
        if (message.contains("closed", ignoreCase = true)) {
            rendererAcceptingFrames = false
        }
        if (!closedResourceFaultReported) {
            closedResourceFaultReported = true
            log.warn("Render frame was isolated safely: $message")
        }
        return@on
    }
}

onUnload {
    rendererAcceptingFrames = false
    clearSharedPet()
    clearRemotePets()
}
