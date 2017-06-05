package moe.feng.gd.gaokaoquery.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.google.gson.Gson
import moe.feng.gd.gaokaoquery.R
import moe.feng.gd.gaokaoquery.api.CommonApi
import moe.feng.gd.gaokaoquery.api.QueryApi
import moe.feng.gd.gaokaoquery.model.ScoreResult
import moe.feng.kotlinyan.common.AndroidExtensions
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity(), AndroidExtensions {

	val toolbar by lazy { find<Toolbar>(R.id.toolbar) }
	val captchaImage by lazy { find<ImageView>(R.id.captcha_image) }
	val queryScoreBtn by lazy { find<Button>(R.id.btn_query_score) }
	val numberEdit by lazy { find<EditText>(R.id.edit_number) }
	val birthEdit by lazy { find<EditText>(R.id.edit_birth) }
	val captchaEdit by lazy { find<EditText>(R.id.edit_captcha) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(toolbar)

		queryScoreBtn.onClick {
			// TODO 检查输入框内容是否完整
			doQueryScore()
		}
		captchaImage.onClick { refreshCaptcha() }
		captchaEdit.onClick { it?.hideKeyboard() }
		refreshCaptcha()
	}

	private fun refreshCaptcha() {
		captchaImage.loadBitmap { CommonApi.getCaptchaNewApi()!! }
	}

	private fun doQueryScore() = doAsync {
		val result = QueryApi.queryScore(2016, numberEdit.text.toString().toInt(),
				birthEdit.text.toString(), captchaEdit.text.toString(), null)
		uiThread {
			toast(Gson().toJson(result))
			if (result?.code == 200) {
				showScoreResultDialog(result.data!!)
			}
			refreshCaptcha()
		}
	}

	private fun showScoreResultDialog(result: ScoreResult) {
		var msg = resources.string[R.string.score_result_header]
				?.format(result.studentName, result.studentNumber, result.issueDate)
		result.scores
				.map { resources.string[R.string.score_result_item_format]?.format(it.name, it.point) }
				.forEach { msg += it }
		msg += resources.string[R.string.score_result_footer]
		AlertDialog.Builder(this)
				.setTitle(R.string.score_result_title)
				.setMessage(msg)
				.setPositiveButton(android.R.string.ok) {_, _ ->}
				.show()
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_main, menu)
		menu.tintItemsColor(Color.WHITE)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_help -> {}
		}
		return super.onOptionsItemSelected(item)
	}
}
