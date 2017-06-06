package moe.feng.gd.gaokaoquery.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import moe.feng.gd.gaokaoquery.R
import moe.feng.gd.gaokaoquery.api.CommonApi
import moe.feng.gd.gaokaoquery.api.QueryApi
import moe.feng.gd.gaokaoquery.model.AdmissionResult
import moe.feng.gd.gaokaoquery.model.ScoreResult
import moe.feng.kotlinyan.common.AndroidExtensions
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : AppCompatActivity(), AndroidExtensions, AdapterView.OnItemSelectedListener {

	val toolbar by lazy { find<Toolbar>(R.id.toolbar) }
	val spinner by lazy { find<Spinner>(R.id.spinner_year) }
	val captchaImage by lazy { find<ImageView>(R.id.captcha_image) }
	val queryScoreBtn by lazy { find<Button>(R.id.btn_query_score) }
	val numberEdit by lazy { find<EditText>(R.id.edit_number) }
	val birthEdit by lazy { find<EditText>(R.id.edit_birth) }
	val captchaEdit by lazy { find<EditText>(R.id.edit_captcha) }
	val rootLayout by lazy { find<CoordinatorLayout>(R.id.root_layout) }

	val apiList = listOf(
			mapOf("title" to "2016 高考成绩", "key" to "2016gkcj"),
			mapOf("title" to "2016 高考录取", "key" to "2016gklq")
	)

	var currentApi = "2016gkcj"

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(toolbar)

		queryScoreBtn.onClick {
			// TODO 检查输入框内容是否完整
			if (currentApi.contains("gkcj")) {
				doQueryScore()
			} else if (currentApi.contains("gklq")) {
				doQueryAdmission()
			} else {
				Snackbar.make(rootLayout, R.string.toast_unsupported_api, Snackbar.LENGTH_LONG).show()
			}
		}
		captchaImage.onClick { refreshCaptcha() }
		spinner.adapter = SimpleAdapter(this, apiList,
				android.R.layout.simple_list_item_1, arrayOf("title"), intArrayOf(android.R.id.text1))
		spinner.onItemSelectedListener = this
		numberEdit.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(s: Editable?) {}
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
				if (!s.isNullOrEmpty() && captchaImage.image == null) {
					refreshCaptcha()
				}
			}
		})
	}

	override fun onNothingSelected(parent: AdapterView<*>?) {}

	override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
		currentApi = apiList[position]["key"]!!
		captchaImage.image = null
		refreshCaptcha()
	}

	private fun refreshCaptcha() {
		if (numberEdit.text.isNullOrEmpty()) {
			return
		}
		captchaImage.loadBitmap {
			if (currentApi.startsWith("2016")) {
				CommonApi.getCaptchaNewApi(CommonApi.captchaReferer[currentApi])
			} else {
				null
			}
		}
	}

	private fun doQueryScore() = doAsync {
		val result = QueryApi.queryScore(2016, numberEdit.text.toString().toInt(),
				birthEdit.text.toString(), captchaEdit.text.toString(), null)
		uiThread {
			if (result?.code == 200) {
				showScoreResultDialog(result.data!!)
			}
			refreshCaptcha()
		}
	}

	private fun doQueryAdmission() = doAsync {
		val result = QueryApi.queryAdmission(2016, numberEdit.text.toString().toInt(),
				birthEdit.text.toString(), captchaEdit.text.toString(), null)
		uiThread {
			if (result?.code == 200) {
				showAdmissionResultDialog(result.data!!)
			}
			refreshCaptcha()
		}
	}

	private fun showScoreResultDialog(result: ScoreResult) {
		AlertDialog.Builder(this)
				.setTitle(R.string.score_result_title)
				.setMessage(buildString {
					append(resources.string[R.string.score_result_header]
							?.format(result.studentName, result.studentNumber, result.issueDate))
					result.scores
							.map { resources.string[R.string.score_result_item_format]?.format(it.name, it.point) }
							.forEach { append(it) }
					append(resources.string[R.string.score_result_footer])
				})
				.setPositiveButton(android.R.string.ok) {_, _ ->}
				.show()
	}

	private fun showAdmissionResultDialog(result: AdmissionResult) {
		AlertDialog.Builder(this)
				.setTitle(R.string.score_result_title)
				.setMessage(buildString {
					append(resources.string[R.string.admission_result_header]
							?.format(result.studentName, result.studentNumber, result.issueDate))
					result.admission
							.map {
								resources.string[R.string.admission_result_item_format]
										?.format(it.schoolNumber, it.batch, it.category, it.schoolName)
							}
							.forEach { append(it) }
					append(resources.string[R.string.admission_result_footer])
				})
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
