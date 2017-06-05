package moe.feng.gd.gaokaoquery.model

import com.google.gson.annotations.SerializedName

open class BaseMessage<out T>(
		@SerializedName("ret_code") val code: Int,
		val msg: String?,
		val data: T?
) {

	companion object {

		fun error(code : Int) = BaseMessage(code, null, null)

		fun error(msg : String) = BaseMessage(-1, msg, null)

	}

}