package moe.feng.gd.gkquery.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import moe.feng.gd.gkquery.model.Result;

public class QueryApi {

	private static String cookie = null;
	private static HttpClient client = new DefaultHttpClient();

	private static final String VERIFY_CODE_URL = "http://www.5184.com/gk/common/checkcode.php";
	private static final String QUERY_URL = "http://www.5184.com/gk/common/get_lq_edg.php";

	private static final String TAG = QueryApi.class.getSimpleName();

	public static Bitmap getVerifyCode() throws IOException {
		HttpPost httpPost = new HttpPost(VERIFY_CODE_URL);
		HttpResponse response = client.execute(httpPost);
		cookie = convertCookieToString(((AbstractHttpClient) client).getCookieStore());
		Log.i(TAG, "cookie:" + cookie);
		byte[] bytes = EntityUtils.toByteArray(response.getEntity());
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	public static Result getResult(String number, String birth, String verifyCode) {
		HttpPost httpPost = new HttpPost(QUERY_URL + "?t=" + System.currentTimeMillis());
		httpPost.setHeader("Cookie", cookie);

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("csny", birth));
		params.add(new BasicNameValuePair("zkzh", number));
		params.add(new BasicNameValuePair("yzm", verifyCode));

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse response = client.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				String json = EntityUtils.toString(response.getEntity());
				Log.i(TAG, "result:" + json);
				return Result.parse(json);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new Result();
	}

	private static String convertCookieToString(CookieStore cs) {
		StringBuffer sb = new StringBuffer();
		List<Cookie> list = cs.getCookies();
		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i).getName())
					.append("=")
					.append(list.get(i).getValue())
					.append(i != list.size() - 1 ? "; " : "");
		}
		return sb.toString();
	}

}
