package ray.mintcat.collect.util

import io.izzel.taboolib.Version
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.util.lite.cooldown.Cooldown
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

interface Helper {

    fun Player.getTargetBlockExact(): Block? {
        return if (Version.isAfter(Version.v1_13)) {
            this.getTargetBlockExact(10, FluidCollisionMode.NEVER)
        } else {
            this.getTargetBlock(setOf(Material.AIR), 10)
        }
    }

    fun CommandSender.info(value: String) {
        this.sendMessage("§8[§f Collect §8] §7${value.replace("&", "§")}")
        if (this is Player && !Global.cd.isCooldown(this.name)) {
            this.playSound(this.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
        }
    }

    fun CommandSender.error(value: String) {
        this.sendMessage("§8[§c Collect §8] §7${value.replace("&", "§")}")
        if (this is Player && !Global.cd.isCooldown(this.name)) {
            this.playSound(this.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
        }
    }

    fun Player.info(value: String) {
        this.sendMessage("§8[§f Collect §8] §7${value.replace("&", "§")}")
        if (!Global.cd.isCooldown(this.name)) {
            this.playSound(this.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
        }
    }

    fun Player.error(value: String) {
        this.sendMessage("§8[§c Collect §8] §7${value.replace("&", "§")}")
        if (!Global.cd.isCooldown(this.name)) {
            this.playSound(this.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
        }
    }

    fun Block.display() {
        world.playEffect(location, Effect.STEP_SOUND, type)
    }

    fun Location.disPlay() {
        world?.playEffect(this, Effect.STEP_SOUND, Material.COMMAND_BLOCK)
    }

    fun String.unColored(): String {
        return TLocale.Translate.setUncolored(this)
    }

    object Global {
        @TInject
        val cd = Cooldown("command.sound", 50)
    }
}