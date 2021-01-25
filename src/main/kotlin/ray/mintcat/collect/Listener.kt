package ray.mintcat.collect

import io.izzel.taboolib.module.inject.TListener
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

@TListener
class Listener : Listener {


    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block
        if (block.isEmpty || block.type == Material.AIR){
            return
        }

        val collcetData = CollectData(block.type.name,block.blockData,block.location)


    }

}