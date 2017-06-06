package moe.feng.gd.gaokaoquery.model

import com.google.gson.annotations.SerializedName

data class AdmissionResult(
		@SerializedName("cj_vo") val admission : List<Admission>
) : BaseResult()