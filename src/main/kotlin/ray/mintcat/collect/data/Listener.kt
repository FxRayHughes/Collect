package ray.mintcat.collect.data

import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.item.Items
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import ray.mintcat.collect.Collect
import ray.mintcat.collect.util.Helper
import ray.mintcat.collect.util.Util

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
            collectData.location = event.clickedBlock?.location!!
            collectData.init()
            player.info("选择完成.")
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
            player.info("粘贴完成.")
        }
    }

}