package xyz.bluspring.lifelinedeathhandler.util

import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3i
import kotlin.math.floor

object ColorHelper {
    fun intToRgb(value: Int): Vec3i {
        return Vec3i(((value shr 16) and 0xFF), ((value shr 8) and 0xFF), (value and 0xFF))
    }

    fun rgbToInt(r: Int, g: Int, b: Int): Int {
        var int = r
        int = (int shl 8) + g
        int = (int shl 8) + b

        return int
    }

    fun rgbToInt(rgb: Vec3i): Int {
        return rgbToInt(rgb.x, rgb.y, rgb.z)
    }

    fun linearGradient(stops: List<Vec3i>, value: Double): Vec3i {
        val stopLength = 1 / (stops.size.toDouble() - 1)
        val valueRatio = value / stopLength
        val stopIndex = floor(valueRatio).toInt()

        if (stopIndex >= (stops.size - 1))
            return stops.last()

        val stopFraction = valueRatio % 1
        val gradient = Vec3i(
            MathHelper.lerp(stopFraction, stops[stopIndex].x.toDouble() / 255, stops[stopIndex + 1].x.toDouble() / 255) * 255,
            MathHelper.lerp(stopFraction, stops[stopIndex].y.toDouble() / 255, stops[stopIndex + 1].x.toDouble() / 255) * 255,
            MathHelper.lerp(stopFraction, stops[stopIndex].z.toDouble() / 255, stops[stopIndex + 1].x.toDouble() / 255) * 255
        )

        return gradient
    }
}