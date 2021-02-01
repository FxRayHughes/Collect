package ray.mintcat.collect.drop

import io.izzel.taboolib.module.db.local.LocalFile
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.inject.TSchedule
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.ClickEvent
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import io.izzel.taboolib.util.item.inventory.linked.MenuLinked
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import ray.mintcat.collect.data.CollectData
import java.awt.Menu

object Drop {

    @LocalFile("drops.yml")
    lateinit var data: FileConfiguration
        private set

    val drops = ArrayList<DropData>()

    @TSchedule
    fun import() {
        drops.clear()
        data.getKeys(false).forEach {
            drops.add(
                DropData(
                    it,
                    data.getInt("${it}.weight"),
                    data.getItemStack("${it}.item") ?: ItemStack(Material.AIR),
                    data.getStringList("${it}.commands"),
                    data.getInt("${it}.amount") ?: 1
                )
            )
        }
    }

    @TFunction.Cancel
    fun export() {
        data.getKeys(false).forEach { data.set(it, null) }
        drops.forEach { dropData ->
            val id = dropData.id
            data.set("${id}.weight", dropData.weight)
            data.set("${id}.item", dropData.item)
            data.set("${id}.commands", dropData.command)
            data.set("${id}.amount", dropData.amount)
        }
    }

    fun delete(id: String) {
        data.set(id, null)
    }

    fun getDrop(id: String): DropData? {
        return drops.firstOrNull { it.isThis(id) }
    }

    fun openDropList(player: Player) {
        val menu = MenuBuilder.builder()
        menu.rows(3)
        menu.title("Drops列表")
        menu.build { inv ->
            drops.forEach { dropData ->
                val itemsType = if (Items.isNull(dropData.item)){ ItemStack(Material.COMMAND_BLOCK_MINECART) } else{ dropData.item }
                val lore = mutableListOf("§7动作列表:")
                lore.addAll(dropData.command)
                lore.add("§7权重: ${dropData.weight}")
                lore.add("§7数量: ${dropData.amount}")
                inv.addItem(ItemBuilder(itemsType).lore(lore.map { "§f$it" }).build())
            }
        }
        menu.event {
        }
        menu.close {
        }
        menu.open(player)
    }


}