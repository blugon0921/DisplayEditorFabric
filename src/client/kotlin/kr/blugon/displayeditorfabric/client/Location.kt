package kr.blugon.displayeditorfabric.client

import com.mojang.brigadier.context.CommandContext
import kr.blugon.kotlinbrigadierfabric.get
import net.minecraft.command.argument.PosArgument
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World


open class WorldlessLocation(
    open var x: Double,
    open var y: Double,
    open var z: Double,
    open var yaw: Float = 0f,
    open var pitch: Float = 0f
) {
    constructor(position: Vec3d) : this(position.x, position.y, position.z, 0f, 0f)
    constructor(position: Vec3d, rotation: Vec2f) : this(position.x, position.y, position.z, rotation.y, rotation.x)

    fun toLocation(world: ServerWorld): Location = Location(world, x, y, z, yaw, pitch)

    val position: Vec3d
        get() = Vec3d(x, y, z)

    val rotation: Vec2f
        get() = Vec2f(yaw, pitch)

    inline fun <reified T> spawnEntity(world: ServerWorld, entityType: EntityType<*>, spawnBefore: (T) -> Unit = {}): T {
        return world.spawnEntity(this, entityType, spawnBefore)
    }

    val blockX: Int
        get() = floor(x)
    val blockY: Int
        get() = floor(y)
    val blockZ: Int
        get() = floor(z)

    private fun floor(num: Double): Int {
        val floor = num.toInt()
        return if (floor.toDouble() == num) floor else floor - (java.lang.Double.doubleToRawLongBits(num) ushr 63).toInt()
    }
}

data class Location(
    val world: World,
    override var x: Double,
    override var y: Double,
    override var z: Double,
    override var yaw: Float = 0f,
    override var pitch: Float = 0f
): WorldlessLocation(x, y, z, yaw, pitch) {

    constructor(world: World, position: Vec3d) : this(world, position.x, position.y, position.z, 0f, 0f)
    constructor(world: World, position: BlockPos) : this(world, position.x+.0, position.y+.0, position.z+.0, 0f, 0f)
    constructor(world: World, position: Vec3d, rotation: Vec2f) : this(world, position.x, position.y, position.z, rotation.y, rotation.x)

    inline fun <reified T> spawnEntity(entityType: EntityType<*>, spawnBefore: (T) -> Unit = {}): T {
        return world.spawnEntity(this, entityType, spawnBefore)
    }
}

val Entity.location: Location
    get() {
        return Location(world, x, y, z, yaw, pitch)
    }

fun CommandContext<ServerCommandSource>.getLocation(world: World, positionKey: String, rotationKey: String): Location {
    val positionArg: PosArgument = this[positionKey]
    val rotationArg: PosArgument = this[rotationKey]

    val position = positionArg.toAbsolutePos(this.source)
    val rotation = rotationArg.toAbsoluteRotation(this.source)
    return Location(world, position, rotation)
}

fun CommandContext<ServerCommandSource>.getLocationWithoutRotation(world: World, positionKey: String): Location {
    val positionArg: PosArgument = this[positionKey]

    val position = positionArg.toAbsolutePos(this.source)
    return Location(world, position)
}