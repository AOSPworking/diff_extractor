package aosp.working.diffextractor.dto

import com.beust.klaxon.Json

data class TopJson(
    @Json(name = "dst", index = 0) val dstCommitHashId: String,
    @Json(name = "src", index = 1) val srcCommitHashId: String,
    @Json(name = "changeFiles", index = 2) val commitChangeFiles: List<FileProperty>
)

data class FileProperty(
    @Json(name = "name", index = 0) val name: String,
    @Json(name = "methods", index = 1) val methods: List<String>
)