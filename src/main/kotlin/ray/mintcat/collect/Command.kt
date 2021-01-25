package ray.mintcat.collect

import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.module.command.base.*
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.collect.Util.formatsOne
import java.io.File

@BaseCommand(name = "collect", permission = "*")
class Command : BaseMainCommand(), Helper {

    @SubCommand(description = "创建方案", permission = "*")
    var create: BaseSubCommand = object : BaseSubCommand() {
        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val block = (sender as Player).getTargetBlockExact()
            if (block == null || block.type == Material.AIR) {
                sender.error("无效的方块.")
                return
            }
            val collectData = Collect.getCollect(block)
            if (collectData != null) {
                block.display()
                sender.error("该方块已存在方案.")
                return
            }
            block.display()
            sender.info("方案已创建.")
            Collect.collects.add(
                CollectData(
                    block.type.name,
                    block.blockData.asString,
                    Util.fromLocation(block.location)
                ).run {
                    this.openEdit(sender)
                    this
                })
            Collect.export()
        }
    }

    @SubCommand(description = "移除方案", permission = "*")
    var remove: BaseSubCommand = object : BaseSubCommand() {
        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val block = (sender as Player).getTargetBlockExact()
            if (block == null || block.type == Material.AIR) {
                sender.error("无效的方块.")
                return
            }
            val collectData = Collect.getCollect(block)
            if (collectData == null) {
                block.display()
                sender.error("该方块不存在方案.")
                return
            }
            block.display()
            sender.info("方案已移除.")
            Collect.collects.remove(collectData)
            Collect.delete(collectData.location)
            Collect.export()
        }
    }

    @SubCommand(description = "编辑方案", permission = "*")
    var edit: BaseSubCommand = object : BaseSubCommand() {
        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val block = (sender as Player).getTargetBlockExact()
            if (block == null || block.type == Material.AIR) {
                sender.error("无效的方块.")
                return
            }
            val collectData = Collect.getCollect(block)
            if (collectData == null) {
                block.display()
                sender.error("该方块不存在方案.")
                return
            }
            sender.info("正在编辑方案.")
            collectData.openEdit(sender)
        }
    }

    @SubCommand(description = "复制方案", permission = "*")
    var copy: BaseSubCommand = object : BaseSubCommand() {
        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val block = (sender as Player).getTargetBlockExact()
            if (block == null || block.type == Material.AIR) {
                sender.error("无效的方块.")
                return
            }
            val collectData = Collect.getCollect(block)
            if (collectData == null) {
                block.display()
                sender.error("该方块不存在方案.")
                return
            }
            block.display()
            sender.info("使用§f复制工具§7左键方块进行复制,复制掉落和条件.")
            CronusUtils.addItem(
                sender,
                ItemBuilder(Material.BLAZE_ROD).name("§f§f§f复制工具")
                    .lore("§7Collect", "§7${collectData.location}").shiny().build()
            )
        }
    }

    @SubCommand(description = "附近方案", permission = "*")
    var near: BaseSubCommand = object : BaseSubCommand() {
        override fun onCommand(
            sender: CommandSender,
            command: Command,
            s: String,
            args: Array<String>
        ) {
            sender.info("附近方案:")
            Collect.collects.forEach {
                if (it.locationData.world?.name == (sender as Player).world.name && it.locationData.distance(
                        sender.location
                    ) < 50
                ) {
                    it.locationData.disPlay()
                    sender.info(
                        "§8 - §f${Util.fromLocation(it.locationData)} §7(${
                            it.locationData.distance(sender.location).formatsOne()
                        }m)"
                    )
                }
            }
        }
    }

    @SubCommand(description = "重载方案", permission = "*")
    var reload: BaseSubCommand = object : BaseSubCommand() {
        override fun onCommand(
            sender: CommandSender,
            command: Command,
            s: String,
            args: Array<String>
        ) {
            Collect.data.load(File(Collect.plugin.dataFolder, "data.yml"))
            Collect.import()
            sender.info("操作成功.")
        }

    }
}