package ray.mintcat.collect.data

import io.izzel.taboolib.TabooLibAPI
import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.kotlin.ketherx.KetherFunction
import io.izzel.taboolib.util.Features
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import ray.mintcat.collect.Collect
import ray.mintcat.collect.drop.Drop
import ray.mintcat.collect.drop.DropData
import ray.mintcat.collect.util.Helper
import ray.mintcat.collect.util.Util
import ray.mintcat.collect.util.WeightCategory
import ray.mintcat.collect.util.WeightUtil

class CollectData(
    var type: String,
    var data: String,
    var location: Location,
    var drops: MutableList<String> = ArrayList(),
    var conditions: MutableList<String> = ArrayList()
) : Helper {

    lateinit var material: Material
    lateinit var blockData: BlockData
    lateinit var locationData: Location
    lateinit var dropData: List<DropData>

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
        dropData = getDropList()
    }


    fun String.papi(player: Player): String {
        return TabooLibAPI.getPluginBridge().setPlaceholders(player, this)
    }

    fun List<String>.papi(player: Player): MutableList<String>? {
        return TabooLibAPI.getPluginBridge().setPlaceholders(player, this)
    }

    fun check(player: Player): Boolean {
        val info = conditions.papi(player) ?: return true
        val a = !info.map { KetherFunction.parse(it, cacheFunction = false, cacheScript = true) }.contains("false")
        return !info.map { Features.compileScript(it)?.eval().toString().toBoolean() }.contains(false)
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
            val drop = Drop.getDrop(it)
            if (drop != null){
                dropList.add(drop)
            }
        }
        return dropList
    }

    fun isBlock(block: Block): Boolean {
        return this.locationData == block.location
    }


    fun openEdit(player: Player) {
        val menu = MenuBuilder.builder()
        menu.title("编辑方案 $location")
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

                }
                15 -> {
                    player.closeInventory()
                    player.info("请编辑内容，编辑后会自动保存. 语法:§f JavaScript")
                    Features.inputBook(player,"§f§f§f编辑条件",false,drops) { list->
                        conditions.clear()
                        conditions.addAll(list)
                        player.info("编辑完成.")
                        init()
                    }
                }
            }
        }
        menu.close {
            Collect.export()
        }
        menu.open(player)
    }

    fun openDrops(player: Player){
        val menu = MenuBuilder.builder()
        menu.rows(3)
        menu.title("正在编辑方案 $location")
        menu.build{ inv->
        }
        menu.event {

        }
        menu.close {
            openEdit(player)
        }
        menu.open(player)
    }
}