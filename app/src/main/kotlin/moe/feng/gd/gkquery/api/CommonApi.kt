package moe.feng.gd.gkquery.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.apache.http.HttpResponse
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.util.*

object CommonApi {

	private val client = DefaultHttpClient()
	var cookie : String? = null

	val captchaReferer = mapOf(
			"2016gkcj" to "http://page-resoures.5184.com/cjquery/w/index.html?20160600gk_cj",
			"2016gklq" to "http://page-resoures.5184.com/cjquery/w/index.html?20160800gk_lq"
	)

	fun getCaptchaNewApi(referer: String?, random : Double = Random().nextDouble()): Bitmap? {
		val request = HttpGet("http://query-score.5184.com/web/captcha?random=$random")
		request.addHeader("Referer", referer)
		request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
				" AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
		request.addHeader("Host", "query-score.5184.com")
		val result : HttpResponse
		try {
			result = client.execute(request)
		} catch (e: Exception) {
			return null
		}
		saveCookie(client.cookieStore)
		val bytes = EntityUtils.toByteArray(result.entity)
		try {
			return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
		} catch (e: Exception) {
			return null
		}
	}

	fun saveCookie(cs: CookieStore?) {
		cs?.cookies?.map {"${it.name}=${it.value}"}?.reduce { total, next -> "$total; $next" }?.let { cookie = it }
	}

}