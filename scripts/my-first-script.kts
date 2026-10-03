import kotlin.math.*
import java.time.Instant
import kotlin.random.Random

name("Кастомные партиклы")
description("Эффектные 3D партиклы при ударах, прыжках и движении с физикой, шейдерным свечением и палитрами")

// --- Настройки на русском языке ---
val spreadSpeed = 2.05f
val gravity = 1.65f
val particleCount by slider("Количество частиц", 25, 1, 100, 1)
val lifetime by slider("Время жизни", 0.9f, 0.3f, 6.0f, 0.1f).postfix(Postfixes.SECONDS)

val customColor1 by colorPicker("Свой цвет 1", 0xFF568EFF)
val customColor2 by colorPicker("Свой цвет 2", 0xFF568EFF)

// --- GPU Shader & Pipelines (GPU-биллбординг с нулевой нагрузкой на CPU) ---
val particleShader = shader("custom_particles_shader", """
    #version 330
    in vec2 v_uv;
    in vec4 v_color;
    out vec4 out_color;

    uniform int u_shape;

    float starSDF(vec2 p) {
        vec2 q = abs(p);
        float beam1 = max(0.0, 1.0 - q.x * 0.7 - q.y * 6.5);
        float beam2 = max(0.0, 1.0 - q.y * 0.7 - q.x * 6.5);
        float core = max(0.0, 1.0 - length(p) * 2.5);
        float glow = exp(-length(p) * 3.2);
        return (beam1 * beam1 + beam2 * beam2) * 1.8 + core * core * 1.2 + glow * 0.7;
    }

    float orbSDF(vec2 p) {
        float d = length(p);
        if (d > 1.0) return 0.0;
        float core = smoothstep(0.42, 0.0, d);
        float glow = exp(-d * 3.0);
        return core * 1.5 + glow * 0.8;
    }

    float diamondSDF(vec2 p) {
        vec2 q = abs(p);
        float d = q.x * 1.3 + q.y * 1.0;
        if (d > 1.0) return exp(-d * 2.5) * 0.2;
        float edge = smoothstep(1.0, 0.85, d);
        float inner = smoothstep(0.5, 0.0, d);
        return edge * 0.9 + inner * 0.6 + exp(-d * 2.5) * 0.4;
    }

    float heartSDF(vec2 p) {
        p.y -= 0.12;
        vec2 q = p;
        q.x = abs(q.x);
        float d = length(vec2(q.x * 1.15, q.y - sqrt(max(0.0, q.x)) * 0.65));
        if (d > 1.0) return 0.0;
        float edge = smoothstep(0.85, 0.65, d);
        float glow = exp(-d * 2.8) * 0.5;
        return edge + glow;
    }

    float sparkSDF(vec2 p) {
        vec2 q = abs(p);
        float line = max(0.0, 1.0 - q.x * 1.2 - q.y * 8.0);
        float crossLine = max(0.0, 1.0 - q.y * 2.5 - q.x * 2.5);
        float glow = exp(-length(p) * 4.0);
        return line * 2.0 + crossLine * 0.8 + glow * 0.6;
    }

    float runeSDF(vec2 p) {
        float d = length(p);
        float ring1 = smoothstep(0.08, 0.0, abs(d - 0.75));
        float ring2 = smoothstep(0.06, 0.0, abs(d - 0.45));
        float crossBars = max(0.0, 1.0 - min(abs(p.x), abs(p.y)) * 12.0) * smoothstep(0.8, 0.4, d);
        float glow = exp(-d * 2.5) * 0.3;
        return ring1 * 1.2 + ring2 * 0.9 + crossBars * 0.8 + glow;
    }

    void main() {
        vec2 p = v_uv;
        float intensity = 0.0;

        if (u_shape == 0) {
            intensity = starSDF(p);
        } else if (u_shape == 1) {
            intensity = orbSDF(p);
        } else if (u_shape == 2) {
            intensity = diamondSDF(p);
        } else if (u_shape == 3) {
            intensity = heartSDF(p);
        } else if (u_shape == 4) {
            intensity = sparkSDF(p);
        } else if (u_shape == 5) {
            intensity = runeSDF(p);
        } else {
            float h = fract(sin(dot(v_uv, vec2(12.9898, 78.233))) * 43758.5453);
            if (h < 0.33) intensity = starSDF(p);
            else if (h < 0.66) intensity = orbSDF(p);
            else intensity = diamondSDF(p);
        }

        if (intensity <= 0.01) discard;

        vec3 rgb = v_color.rgb;
        vec3 hotColor = mix(rgb, vec3(1.0), clamp((intensity - 1.0) * 0.75, 0.0, 1.0));
        float alpha = clamp(intensity * v_color.a, 0.0, 1.0);

        out_color = vec4(hotColor, alpha);
    }
""", """
    #version 330
    layout(location=0) in vec3 pos;
    layout(location=1) in vec4 uvRot; // x: uv.x, y: uv.y, z: size, w: rotation
    layout(location=2) in vec4 color;

    uniform mat4 u_view;
    uniform mat4 u_projection;

    out vec2 v_uv;
    out vec4 v_color;

    void main() {
        vec4 viewPos = u_view * vec4(pos, 1.0);
        // Slightly pull particles toward the camera to avoid water-surface depth conflicts.
        viewPos.z -= 0.01;
        float c = cos(uvRot.w);
        float s = sin(uvRot.w);
        vec2 rotatedOffset = vec2(uvRot.x * c - uvRot.y * s, uvRot.x * s + uvRot.y * c) * uvRot.z;
        viewPos.xy += rotatedOffset;
        gl_Position = u_projection * viewPos;
        v_uv = uvRot.xy;
        v_color = color.bgra;
    }
""")

val format = gpu.format(VertexAttribute.floats(3), VertexAttribute.floats(4), VertexAttribute.color())
val mesh = gpu.indexedMesh(format)

val addPipeline = gpu.pipeline(particleShader, DrawMode.TRIANGLES, BlendMode.ADDITIVE, DepthMode.OFF)
val addPass = gpu.renderType(addPipeline, mesh)

// --- Класс частицы ---
class Particle(
    var x: Double,
    var y: Double,
    var z: Double,
    var vx: Double,
    var vy: Double,
    var vz: Double,
    val size: Float,
    var life: Float = 0f,
    val maxLife: Float,
    val seed: Float,
    var rotation: Float = 0f,
    val rotSpeed: Float = 0f,
    var groundY: Double = -64.0,
    var stillTime: Float = 0f
)

val particles = ArrayList<Particle>()

var lastTimeMs: Long = 0L
var clock: Float = 0f

fun hsvToRgb(h: Float, s: Float, v: Float): IntArray {
    val i = (h * 6.0f).toInt() % 6
    val f = h * 6.0f - i
    val p = v * (1.0f - s)
    val q = v * (1.0f - f * s)
    val t = v * (1.0f - (1.0f - f) * s)
    val r: Float; val g: Float; val b: Float
    when (i) {
        0 -> { r = v; g = t; b = p }
        1 -> { r = q; g = v; b = p }
        2 -> { r = p; g = v; b = t }
        3 -> { r = p; g = q; b = v }
        4 -> { r = t; g = p; b = v }
        else -> { r = v; g = p; b = q }
    }
    return intArrayOf((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
}

fun getPaletteColor(pal: String, progress: Float, seed: Float, alpha: Float): Int {
    val a = (alpha.coerceIn(0f, 1f) * 255).toInt()
    val p = (progress + seed) % 1.0f

    return when {
        pal.contains("неон") || pal.contains("neon") -> {
            val t = 0.5f + 0.5f * sin(p * PI * 2.0f).toFloat()
            val r = (t * 255).toInt()
            val g = (240 * (1f - t * 0.85f)).toInt()
            val b = 255
            Colors.rgba(r, g, b, a)
        }
        pal.contains("закат") || pal.contains("sunset") -> {
            val t = (p * 2.0f) % 2.0f
            if (t < 1.0f) {
                val r = 255
                val g = (75 - t * 10).toInt().coerceIn(0, 255)
                val b = (43 + t * 65).toInt().coerceIn(0, 255)
                Colors.rgba(r, g, b, a)
            } else {
                val t2 = t - 1.0f
                val r = (255 - t2 * 117).toInt().coerceIn(0, 255)
                val g = (65 - t2 * 30).toInt().coerceIn(0, 255)
                val b = (108 + t2 * 27).toInt().coerceIn(0, 255)
                Colors.rgba(r, g, b, a)
            }
        }
        pal.contains("радуга") || pal.contains("rainbow") -> {
            val hue = (clock * 0.35f + p) % 1.0f
            val rgb = hsvToRgb(hue, 0.90f, 1.0f)
            Colors.rgba(rgb[0], rgb[1], rgb[2], a)
        }
        pal.contains("астольфо") || pal.contains("astolfo") -> {
            val t = 0.5f + 0.5f * sin(p * PI * 2.0f).toFloat()
            val r = (255 * t).toInt()
            val g = (119 + t * 110).toInt()
            val b = (169 + t * 86).toInt()
            Colors.rgba(r, g, b, a)
        }
        pal.contains("огонь") || pal.contains("fire") -> {
            val t = p
            val r = 255
            val g = (238 * (1.0f - t * 0.85f)).toInt().coerceIn(0, 255)
            val b = (85 * (1.0f - t)).toInt().coerceIn(0, 255)
            Colors.rgba(r, g, b, a)
        }
        pal.contains("кровь") || pal.contains("blood") -> {
            val t = 0.5f + 0.5f * sin(p * PI * 2.0f).toFloat()
            val r = (153 + t * 102).toInt()
            val g = 0
            val b = (24 + t * 27).toInt()
            Colors.rgba(r, g, b, a)
        }
        pal.contains("золото") || pal.contains("gold") -> {
            val t = 0.5f + 0.5f * sin(p * PI * 2.0f).toFloat()
            val r = 255
            val g = (184 + t * 58).toInt()
            val b = (t * 117).toInt()
            Colors.rgba(r, g, b, a)
        }
        else -> {
            val t = 0.5f + 0.5f * sin(p * PI * 2.0f).toFloat()
            val r1 = (customColor1 shr 16) and 0xFF
            val g1 = (customColor1 shr 8) and 0xFF
            val b1 = customColor1 and 0xFF

            val r2 = (customColor2 shr 16) and 0xFF
            val g2 = (customColor2 shr 8) and 0xFF
            val b2 = customColor2 and 0xFF

            val r = (r1 + (r2 - r1) * t).toInt()
            val g = (g1 + (g2 - g1) * t).toInt()
            val b = (b1 + (b2 - b1) * t).toInt()
            Colors.rgba(r, g, b, a)
        }
    }
}

fun spawnParticleBurst(x: Double, y: Double, z: Double, floorY: Double, count: Int, baseSpeed: Float) {
    if (particles.size > 500) return
    val spdMul = spreadSpeed * baseSpeed
    val maxL = lifetime

    for (i in 0 until count) {
        val theta = Random.nextDouble(0.0, PI * 2.0)
        val horizontalSpeed = Random.nextDouble(2.0, 4.8) * spdMul

        // Разлетаются в стороны как попрыгунчики, затем гравитация тянет их вниз.
        val vx = cos(theta) * horizontalSpeed
        val vy = Random.nextDouble(0.35, 1.15) * spdMul
        val vz = sin(theta) * horizontalSpeed

        val p = Particle(
            x = x + Random.nextDouble(-0.12, 0.12),
            y = y + Random.nextDouble(-0.12, 0.20),
            z = z + Random.nextDouble(-0.12, 0.12),
            vx = vx,
            vy = vy,
            vz = vz,
            size = 1.0f,
            maxLife = maxL * (Random.nextFloat() * 0.4f + 0.8f),
            seed = Random.nextFloat(),
            rotation = Random.nextFloat() * 6.28f,
            rotSpeed = (Random.nextFloat() - 0.5f) * 10.0f,
            groundY = floorY
        )
        particles.add(p)
    }
}

fun clearAll() {
    particles.clear()
}

onDisable {
    clearAll()
}

on<WorldLoadEvent> {
    clearAll()
}

on<AttackEvent> { event ->
    val target = try { event.target() } catch (_: Throwable) { null } ?: return@on
    val tx = target.x()
    val ty = maxOf(target.y() + target.height() * 0.55, floor(target.y()) + 1.35)
    val tz = target.z()
    spawnParticleBurst(tx, ty, tz, target.y(), particleCount.toInt(), 1.0f)
}

on<AttackedEvent> { event ->
    val target = try { event.target() } catch (_: Throwable) { null } ?: return@on
    val tx = target.x()
    val ty = maxOf(target.y() + target.height() * 0.55, floor(target.y()) + 1.35)
    val tz = target.z()
    spawnParticleBurst(tx, ty, tz, target.y(), particleCount.toInt(), 0.9f)
}

/* Disabled: kill bursts, running trails, jump particles and target orbit are removed.
on<ClientTickEvent> {
    whenInGame {
        tickCounter++
        if (tickCounter % 80 == 0) {
            deadEntities.clear()
        }

        // 1. Оптимизированный детект ударов и смертей (только ближайшие мобы)
        val px = player.x()
        val py = player.y()
        val pz = player.z()

        if (tickCounter % 2 == 0) {
            try {
                val entities = world.entities()
                for (e in entities) {
                    if (e.id() == player.id()) continue
                    val dx = e.x() - px
                    val dz = e.z() - pz
                    if (dx * dx + dz * dz > 12.0 * 12.0) continue

                    val living = e.asLiving() ?: continue
                    val id = e.id()

                    val prevHurt = trackedHurtTicks[id] ?: 0
                    val currHurt = living.hurtTicks()
                    trackedHurtTicks[id] = currHurt

                    if (hitParticles && currHurt >= 9 && prevHurt < 9) {
                        val tx = e.x()
                        val ty = e.y() + e.height() * 0.55
                        val tz = e.z()
                        spawnParticleBurst(tx, ty, tz, e.y(), (hitCount * 0.75f).toInt().coerceAtLeast(5), 0.95f)
                    }

                    if (killBurst && !deadEntities.contains(id)) {
                        if (living.dead() || (living.health() <= 0.01f && currHurt > 0)) {
                            deadEntities.add(id)
                            val tx = e.x()
                            val ty = e.y() + e.height() * 0.55
                            val tz = e.z()
                            spawnParticleBurst(tx, ty, tz, e.y(), killCount.toInt(), 1.25f, 1.2f)
                        }
                    }
                }
            } catch (_: Throwable) {}
        }

        // 2. След при движении
        if (trail && (player.sprinting() || player.hasMovementInput())) {
            val vel = player.velocity()
            val spd = vel.length()
            if (spd > 0.08) {
                for (i in 0 until 1) {
                    particles.add(
                        Particle(
                            x = px - vel.x * 0.25 + Random.nextDouble(-0.15, 0.15),
                            y = py + Random.nextDouble(0.05, 0.30),
                            z = pz - vel.z * 0.25 + Random.nextDouble(-0.15, 0.15),
                            vx = -vel.x * 0.2 + Random.nextDouble(-0.3, 0.3),
                            vy = Random.nextDouble(0.3, 0.9),
                            vz = -vel.z * 0.2 + Random.nextDouble(-0.3, 0.3),
                            size = particleSize * 0.75f,
                            maxLife = lifetime * 0.5f,
                            seed = Random.nextFloat(),
                            rotation = Random.nextFloat() * 6.28f,
                            rotSpeed = (Random.nextFloat() - 0.5f) * 6f,
                            groundY = py
                        )
                    )
                }
            }
        }

        // 3. Орбита вокруг цели
        if (targetAura && lastTargetId != -1) {
            val target = try { world.entityById(lastTargetId) } catch (_: Throwable) { null }
            if (target != null && target.alive() && target.distanceTo(player) < 7.0) {
                targetAuraAngle += 0.35f
                val h = target.height() * 0.55
                val rad = target.width() * 0.85 + 0.3
                for (i in 0 until 1) {
                    val a = targetAuraAngle
                    val ox = cos(a) * rad
                    val oz = sin(a) * rad
                    val oy = sin(targetAuraAngle * 1.5f) * 0.25
                    particles.add(
                        Particle(
                            x = target.x() + ox,
                            y = target.y() + h + oy,
                            z = target.z() + oz,
                            vx = -sin(a) * 0.7,
                            vy = Random.nextDouble(-0.05, 0.15),
                            vz = cos(a) * 0.7,
                            size = particleSize * 0.8f,
                            maxLife = lifetime * 0.55f,
                            seed = Random.nextFloat(),
                            rotation = Random.nextFloat() * 6.28f,
                            rotSpeed = 3.5f,
                            groundY = target.y()
                        )
                    )
                }
            }
        }
    }
}

*/

fun isSolidBlockAt(x: Double, y: Double, z: Double): Boolean {
    return try {
        val block = world.block(floor(x).toInt(), floor(y).toInt(), floor(z).toInt())
        val id = block.id().lowercase()
        val thinSnow = id.contains("snow") && !id.contains("snow_block")
        !thinSnow && block.blocksMovement()
    } catch (_: Throwable) {
        false
    }
}

fun landingSurfaceY(x: Double, y: Double, z: Double): Double? {
    return try {
        val blockX = floor(x).toInt()
        val blockY = floor(y).toInt()
        val blockZ = floor(z).toInt()
        val block = world.block(blockX, blockY, blockZ)
        val id = block.id().lowercase()
        if (id.contains("snow") && !id.contains("snow_block")) {
            val below = world.block(blockX, blockY - 1, blockZ)
            if (below.blocksMovement()) blockY.toDouble() else null
        } else if (!block.blocksMovement()) {
            val below = world.block(blockX, blockY - 1, blockZ)
            val belowId = below.id().lowercase()
            if (belowId.contains("snow") && !belowId.contains("snow_block")) {
                val support = world.block(blockX, blockY - 2, blockZ)
                if (support.blocksMovement()) (blockY - 1).toDouble() else null
            } else {
                null
            }
        } else if (block.blocksMovement()) {
            blockY + 1.0
        } else {
            null
        }
    } catch (_: Throwable) {
        null
    }
}

fun blockSurfaceY(x: Double, y: Double, z: Double): Double {
    val blockY = floor(y).toInt()
    val block = world.block(floor(x).toInt(), blockY, floor(z).toInt())
    val id = block.id().lowercase()
    return if (id.contains("snow") && !id.contains("snow_block")) {
        blockY.toDouble()
    } else {
        blockY + 1.0
    }
}

fun particleRenderY(x: Double, y: Double, z: Double): Double {
    return try {
        val blockX = floor(x).toInt()
        val blockY = floor(y).toInt()
        val blockZ = floor(z).toInt()
        val currentId = world.block(blockX, blockY, blockZ).id().lowercase()
        val belowId = world.block(blockX, blockY - 1, blockZ).id().lowercase()
        val nearThinSnow = (currentId.contains("snow") && !currentId.contains("snow_block")) ||
                (belowId.contains("snow") && !belowId.contains("snow_block"))
        if (nearThinSnow) y + 0.12 else y
    } catch (_: Throwable) {
        y
    }
}

fun solidBetween(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): Boolean {
    return try {
        val steps = 24
        for (i in 1 until steps) {
            val t = i.toDouble() / steps.toDouble()
            val x = ax + (bx - ax) * t
            val y = ay + (by - ay) * t
            val z = az + (bz - az) * t
            val block = world.block(floor(x).toInt(), floor(y).toInt(), floor(z).toInt())
            val id = block.id().lowercase()
            val thinSnow = id.contains("snow") && !id.contains("snow_block")
            if (block.blocksMovement() && !thinSnow) return true
        }
        false
    } catch (_: Throwable) {
        false
    }
}

on<Render3DEvent> { event ->
    if (particles.isEmpty()) return@on

    val now = Instant.now().toEpochMilli()
    if (lastTimeMs == 0L) lastTimeMs = now
    var dt = (now - lastTimeMs) / 1000.0
    lastTimeMs = now
    if (dt > 0.08) dt = 0.08
    if (dt <= 0.0) dt = 0.016
    clock += dt.toFloat()

    val cam = event.camera()
    val camX = cam.x()
    val camY = cam.y()
    val camZ = cam.z()

    val grav = gravity * 7.5

    // Обновление физики частиц
    for (i in particles.indices.reversed()) {
        val p = particles[i]
        p.life += dt.toFloat()
        if (p.life >= p.maxLife) {
            particles.removeAt(i)
            continue
        }

        p.vy -= grav * dt
        p.vx *= (1.0 - 0.95 * dt)
        p.vz *= (1.0 - 0.95 * dt)

        val speedSq = p.vx * p.vx + p.vy * p.vy + p.vz * p.vz
        if (speedSq < 0.025) {
            p.stillTime += dt.toFloat()
            if (p.stillTime > 0.75f) {
                particles.removeAt(i)
                continue
            }
        } else {
            p.stillTime = 0f
        }

        var nx = p.x + p.vx * dt
        var ny = p.y + p.vy * dt
        var nz = p.z + p.vz * dt

        // Проверяем каждую ось отдельно, чтобы частицы отскакивали от стен и пола.
        if (isSolidBlockAt(nx, p.y, p.z)) {
            nx = p.x
            p.vx = -p.vx * 0.82
        }
        if (isSolidBlockAt(p.x, p.y, nz)) {
            nz = p.z
            p.vz = -p.vz * 0.82
        }
        val surfaceY = landingSurfaceY(p.x, ny, p.z)
        if (p.vy < 0.0 && surfaceY != null && p.y >= surfaceY && ny <= surfaceY) {
            // Ставим частицу чуть выше верхней грани именно найденного блока.
            ny = surfaceY + 0.04
            p.vy = abs(p.vy).coerceAtLeast(1.8) * 0.82
            p.vx *= 0.92
            p.vz *= 0.92
        }

        if (p.vy > 0.0 && isSolidBlockAt(p.x, ny, p.z)) {
            ny = floor(ny) - 0.04
            p.vy = -abs(p.vy) * 0.70
        }

        p.x = nx
        p.y = ny
        p.z = nz

        p.rotation += p.rotSpeed * dt.toFloat()
    }

    if (particles.isEmpty()) return@on

    val verts: VertexWriter
    val idx: IndexWriter
    try {
        verts = mesh.verts()
        idx = mesh.idx() ?: return@on
    } catch (_: Throwable) {
        return@on
    }

    val palName = "свой цвет"
    for (p in particles) {
        val dx = (p.x - camX).toFloat()
        val dy = (particleRenderY(p.x, p.y, p.z) - camY).toFloat()
        val dz = (p.z - camZ).toFloat()

        val distSq = dx * dx + dy * dy + dz * dz
        if (distSq > 40f * 40f) continue
        if (solidBetween(camX, camY, camZ, p.x, p.y, p.z)) continue

        val prog = (p.life / p.maxLife).coerceIn(0f, 1f)
        val alphaProg = if (prog < 0.15f) (prog / 0.15f) else (1.0f - (prog - 0.15f) / 0.85f)
        val col = getPaletteColor(palName, prog, p.seed, alphaProg)

        // Размер частицы всегда фиксированный: 1.
        val sz = 0.15f
        val rot = p.rotation

        // GPU-биллбординг: передаём центр частицы, UV, размер и угол поворота
        val v0 = verts.putVec3(dx, dy, dz).putFloat(-1f).putFloat(-1f).putFloat(sz).putFloat(rot).putColor(col).next()
        val v1 = verts.putVec3(dx, dy, dz).putFloat( 1f).putFloat(-1f).putFloat(sz).putFloat(rot).putColor(col).next()
        val v2 = verts.putVec3(dx, dy, dz).putFloat( 1f).putFloat( 1f).putFloat(sz).putFloat(rot).putColor(col).next()
        val v3 = verts.putVec3(dx, dy, dz).putFloat(-1f).putFloat( 1f).putFloat(sz).putFloat(rot).putColor(col).next()

        idx.putQuad(v0, v1, v2, v3)
    }

    val shapeId = 1

    try {
        particleShader.setMat4("u_view", event.viewMatrix())
        particleShader.setMat4("u_projection", event.projectionMatrix())
        particleShader.set("u_shape", shapeId)

        addPass.draw()
    } catch (_: Throwable) {}
}
