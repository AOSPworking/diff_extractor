package aosp.working.diffextractor

import com.beust.klaxon.Json

data class TopJson(
    @Json(name = "commits") val commitChangeFiles: Map<String, List<FileProperty>>
)

data class FileProperty(
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "methods") val methods: List<String>
)