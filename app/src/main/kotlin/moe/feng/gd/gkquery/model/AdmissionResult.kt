package moe.feng.gd.gkquery.model

import com.google.gson.annotations.SerializedName

data class AdmissionResult(
		@SerializedName("cj_vo") val admission : List<Admission>
) : BaseResult()