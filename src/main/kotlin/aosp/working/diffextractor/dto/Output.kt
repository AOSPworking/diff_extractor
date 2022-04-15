package aosp.working.diffextractor.dto

import com.beust.klaxon.Json

data class TopJson(
    @Json(name = "id", index = 0) val commitHashId: String,
    @Json(name = "changeFiles", index = 1) val commitChangeFiles: List<FileProperty>
)

data class FileProperty(
    @Json(name = "name", index = 0) val name: String,
    @Json(name = "methods", index = 1) val methods: List<String>
)