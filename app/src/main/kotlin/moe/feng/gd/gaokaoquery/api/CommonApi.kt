package moe.feng.gd.gaokaoquery.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.apache.http.client.CookieStore
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.util.*

object CommonApi {

	private val client = DefaultHttpClient()
	var cookie : String? = null

	fun getCaptchaNewApi(random : Double = Random().nextDouble()): Bitmap?
			= HttpGet("http://query-score.5184.com/web/captcha?random=$random")
			.let {
				it.addHeader("Referer", "http://page-resoures.5184.com/cjquery/w/index.html?20160600gk_cj")
				it.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
						" AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
				it.addHeader("Host", "query-score.5184.com")
				it
			}
			.let { client.execute(it) }
			.let { saveCookie(client.cookieStore); it.entity }
			.let { EntityUtils.toByteArray(it) }
			.let {
				try {
					BitmapFactory.decodeByteArray(it, 0, it.size)
				} catch (e: Exception) {
					null
				}
			}

	fun saveCookie(cs: CookieStore) {
		cookie = cs.cookies.map {"${it.name}=${it.value}"}.reduce { total, next -> "$total; $next" }
	}

}