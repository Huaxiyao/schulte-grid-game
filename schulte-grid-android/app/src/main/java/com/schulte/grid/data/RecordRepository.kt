package com.schulte.grid.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.schulte.grid.model.GameRecord
import com.schulte.grid.model.GridSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.recordStore by preferencesDataStore(name = "schulte_records")

/**
 * 游戏记录仓库 —— 持久化最佳记录和历史记录
 * 使用 DataStore<Preferences> + org.json（Android 内置，无额外依赖）
 */
class RecordRepository(private val context: Context) {

    companion object {
        private fun bestKey(size: Int) = intPreferencesKey("best_${size}")
        private val historyKey = stringPreferencesKey("history_list")
    }

    /** 获取某个尺寸的最佳记录（毫秒），以 Flow 形式暴露 */
    fun getBestFlow(size: GridSize): Flow<Long?> =
        context.recordStore.data.map { prefs ->
            prefs[bestKey(size.size)]?.toLong()
        }

    /** 获取所有尺寸的最佳记录 */
    suspend fun getAllBests(): Map<GridSize, Long> {
        val prefs = context.recordStore.data.first()
        val map = mutableMapOf<GridSize, Long>()
        GridSize.entries.forEach { size ->
            prefs[bestKey(size.size)]?.let { ms ->
                if (ms > 0L) map[size] = ms.toLong()
            }
        }
        return map
    }

    /** 尝试更新最佳记录，返回是否刷新了纪录 */
    suspend fun tryUpdateBest(size: GridSize, ms: Long): Boolean {
        val current = context.recordStore.data.first()[bestKey(size.size)] ?: Int.MAX_VALUE
        if (ms < current) {
            context.recordStore.edit { prefs ->
                prefs[bestKey(size.size)] = ms.toInt()
            }
            return true
        }
        return false
    }

    /** 获取历史记录列表 */
    suspend fun getHistory(): List<GameRecord> {
        val prefs = context.recordStore.data.first()
        val raw = prefs[historyKey] ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                GameRecord(
                    gridSize = GridSize.fromSize(obj.getInt("size")),
                    elapsedMs = obj.getLong("time"),
                    reverseMode = obj.optBoolean("reverse", false),
                    timestamp = obj.optLong("ts", System.currentTimeMillis()),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** 添加一条历史记录 */
    suspend fun addHistory(record: GameRecord) {
        val history = getHistory().toMutableList()
        history.add(0, record)
        if (history.size > 20) history.removeAt(history.size - 1)

        val arr = JSONArray()
        history.forEach { r ->
            arr.put(JSONObject().apply {
                put("size", r.gridSize.size)
                put("time", r.elapsedMs)
                put("reverse", r.reverseMode)
                put("ts", r.timestamp)
            })
        }
        context.recordStore.edit { prefs ->
            prefs[historyKey] = arr.toString()
        }
    }

    /** 清除所有记录 */
    suspend fun clearAll() {
        context.recordStore.edit { prefs ->
            GridSize.entries.forEach { prefs.remove(bestKey(it.size)) }
            prefs.remove(historyKey)
        }
    }
}
