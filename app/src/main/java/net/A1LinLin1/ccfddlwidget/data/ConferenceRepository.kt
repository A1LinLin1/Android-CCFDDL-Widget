package net.A1LinLin1.ccfddlwidget.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.yaml.snakeyaml.Yaml
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

data class Conference(
    val id: String,
    val title: String,
    val year: Int?,
    val sub: String?,
    val ccfRank: String?,
    val deadlineMillis: Long,
    val dday: Long,
    val desc: String?,
    val link: String?
)

object ConferenceRepository {

    private val client = OkHttpClient()

    // 关键：用 GitHub Contents API 列目录
    private fun listDirApi(sub: String): String =
        "https://api.github.com/repos/ccfddl/ccf-deadlines/contents/conference/$sub"

    // GitHub raw 下载链接（Contents API 会直接给 download_url，我们优先用它）
    private val yaml = Yaml()

    /**
     * 拉取指定 sub(方向) 的会议，解析所有 yml，取每个 conf 的“最近未来 deadline”作为排序依据。
     * @param subs 例如 setOf("SC","SE","DB")
     * @param maxFilesPerSub 避免一次拉太多（先跑通，后续再做缓存/增量）
     * @param maxResults 最终返回的会议条数（按 ddl 由近到远）
     */
    suspend fun fetchConferences(
        subs: Set<String>,
        maxFilesPerSub: Int = 80,
        maxResults: Int = 200
    ): List<Conference> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // 1) 先列出每个 sub 目录下的 yml 下载链接
        val downloadUrls = mutableListOf<String>()
        for (sub in subs) {
            val jsonText = httpGet(listDirApi(sub)) ?: continue
            val arr = JSONArray(jsonText)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val type = obj.optString("type")
                val name = obj.optString("name")
                if (type == "file" && name.endsWith(".yml", ignoreCase = true)) {
                    val dl = obj.optString("download_url")
                    if (dl.isNotBlank()) downloadUrls.add(dl)
                }
            }
        }

        // 控制数量，先跑通（后面你想做全量也可以）
        val urls = downloadUrls.take(maxFilesPerSub * max(1, subs.size))

        // 2) 并发下载并解析每个 yml
        val parsed = urls.map { url ->
            async {
                runCatching { parseConferenceYaml(url, now) }.getOrNull()
            }
        }.awaitAll().filterNotNull().flatten()

        // 3) 按 ddl 升序（越近越前），截断
        parsed.sortedBy { it.deadlineMillis }.take(maxResults)
    }

    suspend fun fetchConferencesCached(
        context: Context,
        subs: Set<String>,
        ranks: Set<String>,
        ttlHours: Int = 6,
        maxFilesPerSub: Int = 50,
        maxResults: Int = 200
    ): List<Conference> = withContext(Dispatchers.IO) {

        val now = System.currentTimeMillis()
        val ttlMillis = ttlHours * 60L * 60L * 1000L

        val savedAt = CacheStore.readMeta(context)
        val cached = CacheStore.read(context)

        // 1) 缓存存在且未过期：直接返回（并应用 ranks 过滤）
        if (savedAt != null && cached != null && now - savedAt <= ttlMillis) {
            val filtered = if (ranks.isEmpty()) cached else cached.filter { (it.ccfRank ?: "").uppercase() in ranks }
            return@withContext filtered.sortedBy { it.deadlineMillis }.take(80)
        }

        // 2) 缓存过期：尝试联网刷新
        val fresh = runCatching {
            fetchConferences(subs = subs, maxFilesPerSub = maxFilesPerSub, maxResults = maxResults)
        }.getOrNull()

        if (!fresh.isNullOrEmpty()) {
            CacheStore.write(context, fresh)
            CacheStore.writeMeta(context, now)
            val filtered = if (ranks.isEmpty()) fresh else fresh.filter { (it.ccfRank ?: "").uppercase() in ranks }
            return@withContext filtered.sortedBy { it.deadlineMillis }.take(80)
        }

        // 3) 联网失败：回退旧缓存（如果有）
        if (cached != null) {
            val filtered = if (ranks.isEmpty()) cached else cached.filter { (it.ccfRank ?: "").uppercase() in ranks }
            return@withContext filtered.sortedBy { it.deadlineMillis }.take(80)
        }

        emptyList()
    }


    private fun parseConferenceYaml(url: String, now: Long): List<Conference> {
        val text = httpGet(url) ?: return emptyList()

        // 每个文件通常是一个 list（- title: ...）
        @Suppress("UNCHECKED_CAST")
        val root = yaml.load<Any>(text) as? List<Map<String, Any?>> ?: return emptyList()

        val results = mutableListOf<Conference>()

        for (entry in root) {
            val title = entry["title"] as? String ?: continue
            val desc = entry["description"] as? String
            val sub = entry["sub"] as? String
            val linkTop = entry["link"] as? String // 有些文件可能没有
            val rankMap = entry["rank"] as? Map<*, *>
            val ccfRank = rankMap?.get("ccf") as? String

            val confs = entry["confs"] as? List<*>
            if (confs.isNullOrEmpty()) continue

            for (confAny in confs) {
                val confMap = confAny as? Map<*, *> ?: continue
                val year = (confMap["year"] as? Int) ?: (confMap["year"] as? String)?.toIntOrNull()
                val id = (confMap["id"] as? String) ?: "${title.lowercase()}-${year ?: "na"}"
                val link = (confMap["link"] as? String) ?: linkTop

                val timezone = (confMap["timezone"] as? String) ?: "UTC"
                val timeline = confMap["timeline"] as? List<*> ?: continue

                // 取 “最近的未来 deadline” 作为该 conf 的 ddl
                val best = timeline
                    .mapNotNull { it as? Map<*, *> }
                    .mapNotNull { tl ->
                        val ddlStr = tl["deadline"] as? String ?: return@mapNotNull null
                        parseDeadlineMillis(ddlStr, timezone)
                    }
                    .filter { it > now }
                    .minOrNull() ?: continue

                val dday = (best - now) / (1000L * 60 * 60 * 24)

                results.add(
                    Conference(
                        id = id,
                        title = title,
                        year = year,
                        sub = sub,
                        ccfRank = ccfRank,
                        deadlineMillis = best,
                        dday = dday,
                        desc = desc,
                        link = link
                    )
                )
            }
        }
        return results
    }

    /**
     * ccf-deadlines 的 deadline 是 'yyyy-mm-dd hh:mm:ss' 或 'TBD'
     * timezone 可能是 UTC-8 / UTC+8 / AoE 等
     */
    private fun parseDeadlineMillis(deadline: String, timezone: String): Long? {
        val ddl = deadline.trim()
        if (ddl.equals("TBD", ignoreCase = true)) return null

        // 解析 "yyyy-MM-dd HH:mm:ss"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        // 处理 timezone
        // - "UTC+8" / "UTC-8" -> GMT+8 / GMT-8
        // - "AoE" -> 通常等价于 UTC-12（Anywhere on Earth）
        val tz = when {
            timezone.equals("AoE", ignoreCase = true) -> TimeZone.getTimeZone("GMT-12:00")
            timezone.startsWith("UTC+", true) || timezone.startsWith("UTC-", true) -> {
                val gmt = timezone.replace("UTC", "GMT", ignoreCase = true)
                TimeZone.getTimeZone(gmt)
            }
            timezone.startsWith("GMT+", true) || timezone.startsWith("GMT-", true) -> {
                TimeZone.getTimeZone(timezone)
            }
            else -> TimeZone.getTimeZone("UTC")
        }
        sdf.timeZone = tz

        return runCatching { sdf.parse(ddl)?.time }.getOrNull()
    }

    private fun httpGet(url: String): String? {
        val request = Request.Builder()
            .url(url)
            // 可选：加 UA，避免某些网络环境被挡
            .header("User-Agent", "Android-CCFDDL-Widget")
            .get()
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            return resp.body?.string()
        }
    }
}
