package moe.feng.gd.gkquery.model

import com.google.gson.annotations.SerializedName

data class Score(
		@SerializedName("km_h") private val no : String,
        @SerializedName("km_mc") val name : String,
        @SerializedName("km_cj") val point : String
)