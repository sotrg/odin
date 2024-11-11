package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.blazeHeight
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.blazeWidth
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.puzzleTimersMap
import me.odinmain.utils.*
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.*
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.AxisAlignedBB
import kotlin.collections.set

object BlazeSolver {
    private var blazes = mutableListOf<EntityArmorStand>()
    private var roomType = 0
    private var lastBlazeCount = 10

    fun getBlaze() {
        val room = DungeonUtils.currentRoom ?: return
        if (!DungeonUtils.inDungeons || !room.data.name.equalsOneOf("Lower Blaze", "Higher Blaze")) return
        val hpMap = mutableMapOf<EntityArmorStand, Int>()
        blazes.clear()
        mc.theWorld?.loadedEntityList?.forEach { entity ->
            if (entity !is EntityArmorStand || entity in blazes) return@forEach
            val hp = Regex("^\\[Lv15] Blaze [\\d,]+/([\\d,]+)❤$").find(entity.name.noControlCodes)?.groups?.get(1)?.value?.replace(",", "")?.toIntOrNull() ?: return@forEach
            hpMap[entity] = hp
            blazes.add(entity)
        }
        if (room.data.name == "Lower Blaze") blazes.sortByDescending { hpMap[it] }
        else blazes.sortBy { hpMap[it] }
    }

    fun onRenderWorld() {
        if (!DungeonUtils.currentRoomName.equalsOneOf("Lower Blaze", "Higher Blaze")) return
        if (blazes.isEmpty()) return
        blazes.removeAll {
            mc.theWorld?.getEntityByID(it.entityId) == null
        }
        if (blazes.isEmpty() && lastBlazeCount == 1) {
            LocationUtils.currentDungeon?.puzzles?.find { it.name == Puzzle.Blaze.name }?.status = PuzzleStatus.Completed
            if (PuzzleSolvers.blazeSendComplete) partyMessage("Blaze puzzle solved!")
            puzzleTimersMap["Higher Blaze"]?.hasCompleted = true
            puzzleTimersMap["Lower Blaze"]?.hasCompleted = true
            lastBlazeCount = 0
            return
        }
        lastBlazeCount = blazes.size
        blazes.forEachIndexed { index, entity ->
            val color = when (index) {
                0 -> PuzzleSolvers.blazeFirstColor
                1 -> PuzzleSolvers.blazeSecondColor
                else -> PuzzleSolvers.blazeAllColor
            }
            val aabb = AxisAlignedBB(-blazeWidth / 2, -1 - (blazeHeight / 2), -blazeWidth / 2, blazeWidth / 2, (blazeHeight / 2) - 1, blazeWidth / 2).offset(entity.positionVector)

            Renderer.drawBox(aabb, color,
                outlineAlpha = if (PuzzleSolvers.blazeStyle == 0) 0 else color.alpha, fillAlpha = if (PuzzleSolvers.blazeStyle == 1) 0 else color.alpha, depth = true)

            if (PuzzleSolvers.blazeLineNext && index > 0 && index <= PuzzleSolvers.blazeLineAmount)
                Renderer.draw3DLine(listOf(blazes[index - 1].renderVec, entity.renderBoundingBox.middle), color = color, lineWidth = 1f, depth = true)
        }
    }

    fun reset() {
        blazes.clear()
        roomType = 0
        lastBlazeCount = 10
    }
}