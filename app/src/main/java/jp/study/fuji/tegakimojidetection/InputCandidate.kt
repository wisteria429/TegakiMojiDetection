package jp.study.fuji.tegakimojidetection

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InputCandidate(@Json(name = "candidate") val candidate: List<String>)

