package moe.feng.gd.gaokaoquery.model

import com.google.gson.annotations.SerializedName

data class ScoreResult(
        @SerializedName("cj_vo") val scores : List<Score>
) : BaseResult()