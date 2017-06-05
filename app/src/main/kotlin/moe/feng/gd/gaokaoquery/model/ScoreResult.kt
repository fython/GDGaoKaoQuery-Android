package moe.feng.gd.gaokaoquery.model

import com.google.gson.annotations.SerializedName

data class ScoreResult(
		@SerializedName("ksh") val studentNumber: String,
		@SerializedName("xm") val studentName: String,
        @SerializedName("csrq") val birthDate : String,
        @SerializedName("issue_date") val issueDate : String,
        @SerializedName("data_type") val dataType : String,
        @SerializedName("cj_vo") val scores : List<Score>
)