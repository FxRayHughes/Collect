package ray.mintcat.collect.drop

import io.izzel.taboolib.TabooLibAPI
import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.util.Features
import io.izzel.taboolib.util.item.Items
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DropData(
    val id:String,
    val weight: Int,
    val item: ItemStack,
    val command: List<String>,
    val amount: Int
){
    fun isThis(id: String):Boolean{
        return this.id == id
    }

    fun give(player:Player){
        for (i in (1..amount)){
            CronusUtils.addItem(player,item)
            runCommand(player)
        }
    }

    //server: TODO op: TODO or player: TODO
    private fun runCommand(player: Player){
        command.forEach {
            val key = TabooLibAPI.getPluginBridge().setPlaceholders(player, it).split(": ")
            when (key[0]) {
                "player" -> Features.dispatchCommand(player, key[1])
                "op" -> Features.dispatchCommand(player, key[1], true)
                "server" -> Features.dispatchCommand(Bukkit.getConsoleSender(), key[1])
                "message" -> player.sendMessage(key[1])
            }
        }
    }
}