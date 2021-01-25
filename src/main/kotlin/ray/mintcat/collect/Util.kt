package ray.mintcat.collect

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.NumberConversions
import java.math.RoundingMode
import java.text.DecimalFormat

object Util {

    fun fromLocation(location: Location): String {
        return "${location.world?.name},${location.x},${location.y},${location.z}"
    }

    fun toLocation(source: String): Location {
        return source.split(",").run {
            Location(Bukkit.getWorld(get(0)), getOrElse(1) { "0" }.asDouble(), getOrElse(2) { "0" }.asDouble(), getOrElse(3) { "0" }.asDouble())
        }
    }
    private fun String.asDouble(): Double {
        return NumberConversions.toDouble(this)
    }

    fun Double.formatsOne(): String {
        val format = DecimalFormat("0")
        //未保留小数的舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.roundingMode = RoundingMode.FLOOR
        return format.format(this)
    }

}