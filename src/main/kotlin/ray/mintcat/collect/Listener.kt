package ray.mintcat.collect

import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.item.Items
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot

@TListener
class Listener : Listener, Helper {


    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block
        if (block.isEmpty || block.type == Material.AIR) {
            return
        }
        Collect.getCollect(block.getRelative(BlockFace.DOWN))?.run {
            if (this.check(player)){
                this.drop(player)
            }
        } ?: return

    }

    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) {
            return
        }
        val player = event.player
        val item = event.player.inventory.itemInMainHand
        if (player.isOp && event.action == Action.LEFT_CLICK_BLOCK && Items.hasName(item, "获取工具") && Items.hasLore(item, "Coolect")) {
            event.isCancelled = true
            val location = Util.toLocation(item.itemMeta!!.lore!![1].unColored())
            val collectData = Collect.getCollect(location.block)
            if (collectData == null) {
                player.error("该方案已失效.")
                return
            }
            collectData.type = event.clickedBlock?.type?.name ?: "AIR"
            collectData.data = event.clickedBlock?.blockData?.asString ?: ""
            collectData.location = Util.fromLocation(event.clickedBlock?.location!!)
            collectData.init()
            return
        }
        if (player.isOp && event.action == Action.LEFT_CLICK_BLOCK && Items.hasName(item, "复制工具") && Items.hasLore(item, "Coolect")) {
            event.isCancelled = true
            val locationTools = Util.toLocation(item.itemMeta!!.lore!![1].unColored())
            val collectDataTools = Collect.getCollect(locationTools.block)
            val collectDataBlock = Collect.getCollect(event.clickedBlock!!)
            if (collectDataTools == null) {
                player.error("该方案已失效.")
                return
            }
            if (collectDataBlock == null) {
                player.error("该方块不存在方案.")
                return
            }
            collectDataTools.drops = collectDataBlock.drops
            collectDataTools.conditions = collectDataBlock.conditions
            collectDataTools.init()
        }
    }

    @EventHandler
    fun onEditBook(event:PlayerEditBookEvent){
        if (!event.player.isOp) {
            return
        }
        val book = event.previousBookMeta
        val player = event.player
        if (book.displayName.contains("编辑掉落") && book.lore!![0].unColored() == "Coolect"){
            val collectData = Collect.getCollect(Util.toLocation(book.lore!![1].unColored()).block)
            if (collectData == null) {
                player.error("该方案已失效.")
                return
            }
            collectData.drops.clear()
            if (book.pages[0].unColored() != "clear"){
                collectData.drops.addAll(book.pages.flatMap { it.replace("§0","").split("\n") })
            }
            collectData.init()
            event.isSigning = false
            return
        }
        if (book.displayName.contains("编辑采集条件") && book.lore!![0].unColored() == "Coolect"){
            val collectData = Collect.getCollect(Util.toLocation(book.lore!![1].unColored()).block)
            if (collectData == null) {
                player.error("该方案已失效.")
                return
            }
            collectData.conditions.clear()
            if (book.pages[0].unColored() != "clear"){
                collectData.conditions.addAll(book.pages.flatMap { it.replace("§0","").split("\n") })
            }
            collectData.init()
            event.isSigning = false
        }
    }

}