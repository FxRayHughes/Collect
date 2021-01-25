package ray.mintcat.collect

import io.izzel.taboolib.loader.Plugin
import io.izzel.taboolib.module.db.local.LocalFile
import io.izzel.taboolib.module.inject.TSchedule
import org.bukkit.configuration.file.FileConfiguration

object Collect : Plugin() {

    @LocalFile("data.yml")
    lateinit var data: FileConfiguration
        private set

    val collects = ArrayList<CollectData>()

    @TSchedule
    fun import() {
        collects.clear()
        data.getKeys(false).forEach {
            collects.add(
                CollectData(
                    data.getString("${it}.type") ?: "AIR",
                    data.getString("${it}.data")
                )
            )
        }
    }

}