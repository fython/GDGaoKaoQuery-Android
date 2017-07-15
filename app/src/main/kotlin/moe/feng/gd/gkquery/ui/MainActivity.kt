package moe.feng.gd.gkquery.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import moe.feng.gd.gkquery.R
import moe.feng.gd.gkquery.api.CommonApi
import moe.feng.gd.gkquery.api.QueryApi
import moe.feng.gd.gkquery.model.AdmissionResult
import moe.feng.gd.gkquery.model.ScoreResult
import moe.feng.kotlinyan.common.AndroidExtensions
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import moe.feng.gd.gkquery.api.MoreApi
import moe.feng.kotlinyan.common.SupportDesignExtensions

const val KEY_REMEMBER = "remember"
const val KEY_NUMBER = "number"
const val KEY_BIRTH = "birth"

class MainActivity : AppCompatActivity(), AndroidExtensions, SupportDesignExtensions,
		AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {

	val toolbar by lazy { find<Toolbar>(R.id.toolbar) }
	val spinner by lazy { find<Spinner>(R.id.spinner_year) }
	val captchaImage by lazy { find<ImageView>(R.id.captcha_image) }
	val queryScoreBtn by lazy { find<Button>(R.id.btn_query_score) }
	val numberEdit by lazy { find<EditText>(R.id.edit_number) }
	val birthEdit by lazy { find<EditText>(R.id.edit_birth) }
	val captchaEdit by lazy { find<EditText>(R.id.edit_captcha) }
	val rootLayout by lazy { find<CoordinatorLayout>(R.id.root_layout) }
	val toggleRemeber by lazy { find<CheckBox>(R.id.toggle_remember) }

	val drawerLayout by lazy { find<DrawerLayout>(R.id.drawer_layout) }
	val navigationView by lazy { find<NavigationView>(R.id.navigation_view) }

	val apiList = listOf(
			mapOf("title" to "2017 高考录取 (Beta)", "key" to "2017gklq"),
			mapOf("title" to "2017 高考成绩 (Beta)", "key" to "2017gkcj"),
			mapOf("title" to "2016 高考成绩", "key" to "2016gkcj"),
			mapOf("title" to "2016 高考录取", "key" to "2016gklq")
	)

	var currentApi = "2017gklq"

	val sharedPref by lazy { getSharedPreferences("pref", Context.MODE_PRIVATE) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// 设置 Toolbar
		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)

		// 设置 NavigationView 事件监听
		navigationView.setNavigationItemSelectedListener(this)

		// 填充储存数据
		toggleRemeber.isChecked = sharedPref.getBoolean(KEY_REMEMBER, false)
		if (toggleRemeber.isChecked) {
			numberEdit.setText(sharedPref.getString(KEY_NUMBER, ""))
			birthEdit.setText(sharedPref.getString(KEY_BIRTH, ""))
		}

		queryScoreBtn.onClick {
			if (numberEdit.text.isNullOrEmpty()) {
				rootLayout.snackbar { messageRes = R.string.toast_input_number }.show()
				return@onClick
			}
			if (birthEdit.text.isNullOrEmpty()) {
				rootLayout.snackbar { messageRes = R.string.toast_input_birth }.show()
				return@onClick
			}
			if (captchaEdit.text.isNullOrEmpty()) {
				rootLayout.snackbar { messageRes = R.string.toast_input_captcha }.show()
				return@onClick
			}
			if (currentApi.contains("gkcj")) {
				queryScoreBtn.isEnabled = false
				doQueryScore()
			} else if (currentApi.contains("gklq")) {
				queryScoreBtn.isEnabled = false
				doQueryAdmission()
			} else {
				rootLayout.snackbar { messageRes = R.string.toast_unsupported_api }.show()
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

	override fun onStop() {
		super.onStop()
		sharedPref.edit().putBoolean(KEY_REMEMBER, toggleRemeber.isChecked).apply()
		if (toggleRemeber.isChecked) {
			sharedPref.edit()
					.putString(KEY_NUMBER, numberEdit.text.toString())
					.putString(KEY_BIRTH, birthEdit.text.toString())
					.apply()
		}
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
			CommonApi.getCaptchaNewApi(CommonApi.captchaReferer[currentApi])
		}
	}

	private fun doQueryScore() = doAsync {
		val captcha = captchaEdit.text.toString()
		uiThread { captchaEdit.setText("") }
		val result = QueryApi.queryScore(
				currentApi.filter(Char::isDigit).toInt(),
				numberEdit.text.toString().toInt(),
				birthEdit.text.toString(),
				captcha,
				null
		)
		uiThread {
			queryScoreBtn.isEnabled = true
			if (result?.code == 200) {
				showScoreResultDialog(result.data!!)
			} else {
				rootLayout.snackbar {
					message = result?.msg
					duration = Snackbar.LENGTH_INDEFINITE
					action(android.R.string.ok) {}
				}.show()
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
			} else {
				rootLayout.snackbar {
					message = result?.msg
					duration = Snackbar.LENGTH_INDEFINITE
					action(android.R.string.ok) {}
				}.show()
			}
			refreshCaptcha()
		}
	}

	private fun showScoreResultDialog(result: ScoreResult) {
		buildAlertDialog {
			titleRes = R.string.score_result_title
			message = buildString {
				append(resources.string[R.string.score_result_header]
						?.format(result.studentName, result.studentNumber, result.issueDate))
				result.scores
						.map { resources.string[R.string.score_result_item_format]?.format(it.name, it.point) }
						.forEach { append(it) }
				append(resources.string[R.string.score_result_footer])
			}
			positiveButton(android.R.string.ok) {_, _ ->}
		}.show()
	}

	private fun showAdmissionResultDialog(result: AdmissionResult) {
		buildAlertDialog {
			titleRes = R.string.score_result_title
			message = buildString {
				append(resources.string[R.string.admission_result_header]
						?.format(result.studentName, result.studentNumber, result.issueDate))
				result.admission
						.map {
							resources.string[R.string.admission_result_item_format]
									?.format(it.schoolNumber, it.batch, it.category, it.schoolName)
						}
						.forEach { append(it) }
				append(resources.string[R.string.admission_result_footer])
			}
			positiveButton(android.R.string.ok) {_, _ ->}
		}.show()
	}

	private fun showAboutDialog() {
		buildAlertDialog {
			titleRes = R.string.about_title
			messageRes = R.string.about_message
			neutralButton(R.string.about_github) {
				_, _ -> openUrl("https://github.com/fython/GDGaoKaoQuery-Android")}
			positiveButton(android.R.string.ok) {_, _ ->}
		}.show()
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
			android.R.id.home -> drawerLayout.openDrawer(Gravity.START)
			R.id.action_open_5184 -> openUrl("http://www.5184.com/")
			R.id.action_help -> showAboutDialog()
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.item_gdgydx_lq -> openUrl(MoreApi.GDGYDX_ADMISSION_QUERY)
		}
		return true
	}

}
