package ray.mintcat.collect

import io.izzel.taboolib.loader.Plugin
import io.izzel.taboolib.module.db.local.LocalFile
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.inject.TSchedule
import org.bukkit.Location
import org.bukkit.block.Block
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
                    data.getString("${it}.type", "AIR") ?: "AIR",
                    data.getString("${it}.data") ?: "",
                    data.getString("${it}.location") ?: "World#0.0#0.0#0.0",
                    data.getStringList("${it}.drops"),
                    data.getStringList("${it}.conditions")
                )
            )
        }
    }

    @TFunction.Cancel
    fun export() {
        data.getKeys(false).forEach { data.set(it, null) }
        collects.forEach { collectData ->
            val location = collectData.location
            data.set("${location}.type", collectData.location)
            data.set("${location}.data", collectData.data)
            data.set("${location}.location", collectData.location)
            data.set("${location}.drops", collectData.drops)
            data.set("${location}.conditions", collectData.conditions)
        }
    }

    fun delete(location: String) {
        data.set(location.replace(".", "__"), null)
    }

    fun getCollect(block: Block): CollectData? {
        return collects.firstOrNull { it.isBlock(block) }
    }

}