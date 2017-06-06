package moe.feng.gd.gaokaoquery.ui

import android.app.AlertDialog
import android.net.Uri
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
import android.support.customtabs.CustomTabsIntent

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
			if (numberEdit.text.isNullOrEmpty()) {
				snackbar(R.string.toast_input_number)
				return@onClick
			}
			if (birthEdit.text.isNullOrEmpty()) {
				snackbar(R.string.toast_input_birth)
				return@onClick
			}
			if (captchaEdit.text.isNullOrEmpty()) {
				snackbar(R.string.toast_input_captcha)
				return@onClick
			}
			if (currentApi.contains("gkcj")) {
				queryScoreBtn.isEnabled = false
				doQueryScore()
			} else if (currentApi.contains("gklq")) {
				queryScoreBtn.isEnabled = false
				doQueryAdmission()
			} else {
				snackbar(R.string.toast_unsupported_api)
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
		val captcha = captchaEdit.text.toString()
		uiThread { captchaEdit.setText("") }
		val result = QueryApi.queryScore(2016, numberEdit.text.toString().toInt(),
				birthEdit.text.toString(), captcha, null)
		uiThread {
			queryScoreBtn.isEnabled = true
			if (result?.code == 200) {
				showScoreResultDialog(result.data!!)
			}
			refreshCaptcha()
		}
	}

	private fun doQueryAdmission() = doAsync {
		val captcha = captchaEdit.text.toString()
		uiThread { captchaEdit.setText("") }
		val result = QueryApi.queryAdmission(2016, numberEdit.text.toString().toInt(),
				birthEdit.text.toString(), captcha, null)
		uiThread {
			queryScoreBtn.isEnabled = true
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

	private fun showAboutDialog() {
		AlertDialog.Builder(this)
				.setTitle(R.string.about_title)
				.setMessage(R.string.about_message)
				.setNeutralButton(R.string.about_github) {_, _
					-> openUrl("https://github.com/fython/GDGaoKaoQuery-Android")}
				.setPositiveButton(android.R.string.ok) {_, _ ->}
				.show()
	}

	private fun snackbar(message: CharSequence) {
		Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show()
	}

	private fun snackbar(message: Int) {
		Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show()
	}

	private fun openUrl(url: String) {
		val builder = CustomTabsIntent.Builder()
		builder.setToolbarColor(resources.color[R.color.colorPrimary])
		builder.build().launchUrl(this, Uri.parse(url))
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_main, menu)
		menu.tintItemsColor(resources.color[R.color.primary_dark_text])
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_open_5184 -> openUrl("http://www.5184.com/")
			R.id.action_help -> showAboutDialog()
		}
		return super.onOptionsItemSelected(item)
	}
}
