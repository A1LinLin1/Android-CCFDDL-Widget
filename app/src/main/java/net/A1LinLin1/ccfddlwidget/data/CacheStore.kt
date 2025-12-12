package net.A1LinLin1.ccfddlwidget.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object CacheStore {
    private const val CACHE_FILE = "ccfddl_cache.json"
    private const val META_FILE = "ccfddl_cache_meta.json"

    fun read(context: Context): List<Conference>? {
        val file = File(context.cacheDir, CACHE_FILE)
        if (!file.exists()) return null
        val text = runCatching { file.readText() }.getOrNull() ?: return null
        return runCatching { decode(text) }.getOrNull()
    }

    fun write(context: Context, list: List<Conference>) {
        val file = File(context.cacheDir, CACHE_FILE)
        runCatching { file.writeText(encode(list)) }
    }

    fun readMeta(context: Context): Long? {
        val file = File(context.cacheDir, META_FILE)
        if (!file.exists()) return null
        val text = runCatching { file.readText() }.getOrNull() ?: return null
        return runCatching {
            JSONObject(text).optLong("saved_at", 0L).takeIf { it > 0L }
        }.getOrNull()
    }

    fun writeMeta(context: Context, savedAt: Long) {
        val file = File(context.cacheDir, META_FILE)
        val obj = JSONObject().put("saved_at", savedAt)
        runCatching { file.writeText(obj.toString()) }
    }

    private fun encode(list: List<Conference>): String {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(
                JSONObject()
                    .put("id", c.id)
                    .put("title", c.title)
                    .put("year", c.year)
                    .put("sub", c.sub)
                    .put("ccfRank", c.ccfRank)
                    .put("deadlineMillis", c.deadlineMillis)
                    .put("dday", c.dday)
                    .put("desc", c.desc)
                    .put("link", c.link)
            )
        }
        return arr.toString()
    }

    private fun decode(text: String): List<Conference> {
        val arr = JSONArray(text)
        val out = ArrayList<Conference>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Conference(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    year = if (o.isNull("year")) null else o.optInt("year"),
                    sub = o.optString("sub").ifBlank { null },
                    ccfRank = o.optString("ccfRank").ifBlank { null },
                    deadlineMillis = o.optLong("deadlineMillis"),
                    dday = o.optLong("dday"),
                    desc = o.optString("desc").ifBlank { null },
                    link = o.optString("link").ifBlank { null }
                )
            )
        }
        return out
    }
}
