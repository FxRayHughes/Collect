package ray.mintcat.collect

import github.saukiya.sxattribute.SXAttribute
import ink.ptms.zaphkiel.ZaphkielAPI
import io.izzel.taboolib.TabooLibAPI
import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.util.Features
import io.izzel.taboolib.util.book.builder.BookBuilder
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import io.izzel.taboolib.util.lite.Materials
import io.lumine.xikage.mythicmobs.MythicMobs
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CollectData(
    var type: String,
    var data: String,
    val location: String,
    val drops: MutableList<String> = ArrayList(),
    val conditions: MutableList<String> = ArrayList()
) : Helper {

    lateinit var material: Material
    lateinit var blockData: BlockData
    lateinit var locationData: Location

    init {
        init()
    }

    fun init() {
        material = if (type == "AIR") {
            Material.BARRIER
        } else {
            Material.valueOf(type)
        }
        blockData = Bukkit.createBlockData(data)
        locationData = Util.toLocation(location.replace("__", "."))
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

    fun drop(player: Player) {
        val data = getdrop()
        val info = data?.action?.papi(player)?.split(": ") ?: return
        (0..data.amount).forEach { _ ->
            CronusUtils.addItem(player, getDrops(player, info[0], info[1]))
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

    fun isBlock(block: Block): Boolean {
        return this.locationData == block.location
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


    fun openEdit(player: Player) {
        val menu = MenuBuilder.builder()
        menu.title("编辑脚本 ${Util.fromLocation(locationData)}")
        menu.rows(3)
        menu.build { inv ->
            inv.setItem(
                11,
                ItemBuilder(material).name("§f方块信息")
                    .lore(listOf("§7类型:§f $type", "§7附加值:§f $data", "§7位置:§f $location")).build()
            )
            inv.setItem(
                13,
                ItemBuilder(Material.DISPENSER).name("§f掉落")
                    .lore(drops.map { "§7$it" }).build()
            )
            inv.setItem(
                15,
                ItemBuilder(Material.OBSERVER).name("§f条件")
                    .lore(conditions.map { "§7$it" }).build()
            )
        }
        menu.event { event ->
            event.isCancelled = true
            when (event.rawSlot) {
                11 -> {
                    player.closeInventory()
                    player.info("使用§f获取工具§7左键设置方块 设置完后需再次打开编辑器.")
                    CronusUtils.addItem(
                        player, ItemBuilder(Material.IRON_SHOVEL).name("§f§f§f获取工具§l§f§c")
                            .lore("§7Coolect", "§7$location").shiny().build()
                    )
                }
                13 -> {
                    player.closeInventory()
                    CronusUtils.addItem(
                        player,
                        ItemBuilder(
                            BookBuilder(Materials.WRITABLE_BOOK.parseItem()).pagesRaw(
                                drops.joinToString("\n")
                            ).build()
                        ).name("§f§f§f编辑掉落")
                            .lore("§7Coolect", "§7$location").build()
                    )
                }
                15 -> {
                    player.closeInventory()
                    CronusUtils.addItem(
                        player,
                        ItemBuilder(
                            BookBuilder(Materials.WRITABLE_BOOK.parseItem()).pagesRaw(
                                conditions.joinToString("\n")
                            ).build()
                        ).name("§f§f§f编辑采集条件")
                            .lore("§7Coolect", "§7$location").build()
                    )
                }
            }
        }
        menu.close {
            Collect.export()
        }
        menu.open(player)
    }

}