package moe.feng.gd.gkquery.model

import com.google.gson.annotations.SerializedName

data class Admission(
		@SerializedName("yx_h") val schoolNumber: String,
        @SerializedName("pc") val batch: String,
        @SerializedName("jhlb") val category: String,
        @SerializedName("yx_mc") val schoolName: String
)