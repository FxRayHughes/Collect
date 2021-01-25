package ray.mintcat.collect

import github.saukiya.sxattribute.SXAttribute
import ink.ptms.zaphkiel.ZaphkielAPI
import io.izzel.taboolib.TabooLibAPI
import io.izzel.taboolib.util.Features
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.lumine.xikage.mythicmobs.MythicMobs
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CollectData(
    val type: String,
    val data: String,
    val location: Location,
    val drops: List<String>,
    val conditions: List<String>
) {

    lateinit var material:Material
    lateinit var blockData:BlockData

    init {
        init()
    }

    fun init(){
        material = Material.valueOf(type)
        blockData = Bukkit.createBlockData(data)
    }


    fun String.papi(player: Player): String {
        return TabooLibAPI.getPluginBridge().setPlaceholders(player, this)
    }

    fun List<String>.papi(player: Player): MutableList<String>? {
        return TabooLibAPI.getPluginBridge().setPlaceholders(player, this)
    }

    fun check(player: Player): Boolean {
        return !conditions.map { Features.compileScript(it)?.eval().toString().toBoolean() }.contains(false)
    }

    fun drop(player: Player,data:DropData?){
        val info = data?.action?.papi(player)?.split(": ") ?: return
        val itemStack = ItemBuilder(getDrops(player,info[0],info[1]))
        (0..data.amount).forEach { _ ->
            player.inventory.addItem(itemStack.build())
        }
    }

    fun getdrop(): DropData? {
        //开始权重运算 想起了战士教我的写法
        val list = mutableListOf<WeightCategory<DropData>>()
        for (i in getDropList()) {
            list.add(WeightCategory(i, i.weight))
        }
        return WeightUtil.getWeightRandom(list)
    }

    fun getDropList(): List<DropData> {
        val dropList = mutableListOf<DropData>()
        drops.forEach {
            val info = it.split(" | ")
            dropList.add(DropData(info[0].toInt(), info[1], info[2].toInt()))
        }
        return dropList
    }

    //这个写法超级阴间 因为引入了地府API！
    fun getDrops(player: Player, key: String, value: String): ItemStack {
        return when (key) {
            "mm", "MM" -> MythicMobs.inst().itemManager.getItemStack(value) ?: ItemStack(Material.AIR)
            "mc", "MC" -> ItemBuilder(Material.valueOf(value)).build()
            "ij", "IJ" -> Items.fromJson(value) ?: ItemStack(Material.AIR)
            "sx", "SX" -> SXAttribute.getApi().getItem(value, player)
            // cmd: op->
            "cmd", "Command" -> run {
                val cmd = value.split("->")
                when (cmd[0]) {
                    "player" -> Features.dispatchCommand(player, cmd[1])
                    "op" -> Features.dispatchCommand(player, cmd[1], true)
                    "server" -> Features.dispatchCommand(Bukkit.getConsoleSender(), cmd[1])
                }
                ItemStack(Material.AIR)
            }
            "msg", "message" -> run {
                player.sendMessage(value)
                ItemStack(Material.AIR)
            }
            "zap", "ZAP" -> ZaphkielAPI.getItem(value)?.rebuild(player) ?: ItemStack(Material.AIR)
            else -> ItemStack(Material.AIR)
        }
    }

}