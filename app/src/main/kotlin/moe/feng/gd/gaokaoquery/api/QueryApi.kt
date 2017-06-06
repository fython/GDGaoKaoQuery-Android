package moe.feng.gd.gaokaoquery.api

import android.util.Log
import com.google.gson.Gson
import moe.feng.gd.gaokaoquery.model.AdmissionResult
import moe.feng.gd.gaokaoquery.model.BaseMessage
import moe.feng.gd.gaokaoquery.model.ScoreResult
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

object QueryApi {

	private val client = DefaultHttpClient()

	private val queryScoreMethods = hashMapOf<IntRange,
			(year: Int, no: Int, birth: String, captcha: String, tel: String?) -> ScoreResultMessage?>()

	private val queryAdmissionMethods = hashMapOf<IntRange,
			(year: Int, no: Int, birth: String, captcha: String, tel: String?) -> AdmissionResultMessage?>()

	const val CODE_YEAR_NOT_SUPPORTED = -1

	fun queryScore(year: Int, no: Int, birth: String, captcha: String, tel: String?) : BaseMessage<ScoreResult>? {
		val targetMethod = queryScoreMethods.filter { year in it.key }.toList().firstOrNull()
		if (targetMethod != null) {
			return targetMethod.second(year, no, birth, captcha, tel)
		} else {
			return BaseMessage.error(CODE_YEAR_NOT_SUPPORTED)
		}
	}

	fun queryAdmission(year: Int, no: Int, birth: String, captcha: String, tel: String?) : BaseMessage<AdmissionResult>? {
		val targetMethod = queryAdmissionMethods.filter { year in it.key }.toList().firstOrNull()
		if (targetMethod != null) {
			return targetMethod.second(year, no, birth, captcha, tel)
		} else {
			return BaseMessage.error(CODE_YEAR_NOT_SUPPORTED)
		}
	}

	init {
		queryScoreMethods[2016..2016] = {
			_, no, birth, captcha, _ ->
			val url = "http://query-score.5184.com/web/score?" +
					"verify_code=$captcha&issue_date=20160600&data_type=gk_cj&ksh=$no&csrq=$birth"
			HttpGet(url)
					.let {
						it.addHeader("Referer", "http://page-resoures.5184.com/cjquery/w/index.html?20160600gk_cj")
						it.addHeader("Host", "query-score.5184.com")
						it.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
								"AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
						it.setHeader("Cookie", CommonApi.cookie)
						it
					}
					.let { client.execute(it) }
					.let {
						if (it.statusLine.statusCode == 200) {
							val json = EntityUtils.toString(it.entity)
							Log.d(javaClass.simpleName, "Result 200: $json")
							return@let Gson().fromJson(json, ScoreResultMessage::class.java)
						} else {
							return@let null
						}
					}
		}

		queryAdmissionMethods[2016..2016] = {
			_, no, birth, captcha, _ ->
			val url = "http://query-score.5184.com/web/score?" +
					"verify_code=$captcha&issue_date=20160800&data_type=gk_lq&ksh=$no&csrq=$birth"
			HttpGet(url)
					.let {
						it.addHeader("Referer", "http://page-resoures.5184.com/cjquery/w/index.html?20160800gk_lq")
						it.addHeader("Host", "query-score.5184.com")
						it.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
								"AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
						it.setHeader("Cookie", CommonApi.cookie)
						it
					}
					.let { client.execute(it) }
					.let {
						if (it.statusLine.statusCode == 200) {
							val json = EntityUtils.toString(it.entity)
							Log.d(javaClass.simpleName, "Result 200: $json")
							return@let Gson().fromJson(json, AdmissionResultMessage::class.java)
						} else {
							return@let null
						}
					}
		}
	}

	private class ScoreResultMessage(code: Int, msg: String?, data: ScoreResult?)
		: BaseMessage<ScoreResult>(code, msg, data)

	private class AdmissionResultMessage(code: Int, msg: String?, data: AdmissionResult?)
		: BaseMessage<AdmissionResult>(code, msg, data)

}