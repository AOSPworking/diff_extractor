package aosp.working.diffextractor

import com.beust.klaxon.Json

data class TopJson(
    @Json(name = "commits") val commitChangeFiles: Map<String, List<FileProperty>>
)

data class FileProperty(
    @Json(name = "name", index = 0) val name: String,
    @Json(name = "methods", index = 1) val methods: List<String>
)