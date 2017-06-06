package moe.feng.gd.gaokaoquery.model

import com.google.gson.annotations.SerializedName

open class BaseResult {

	@SerializedName("ksh") lateinit var studentNumber: String
	@SerializedName("xm") lateinit var studentName: String
	@SerializedName("csrq") lateinit var birthDate : String
	@SerializedName("issue_date") lateinit var issueDate : String
	@SerializedName("data_type") lateinit var dataType : String

}