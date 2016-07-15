package moe.feng.gd.gkquery.ui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

import java.io.IOException;

import moe.feng.gd.gkquery.Constants;
import moe.feng.gd.gkquery.R;
import moe.feng.gd.gkquery.api.QueryApi;
import moe.feng.gd.gkquery.model.Result;
import moe.feng.gd.gkquery.support.ClipboardUtils;
import moe.feng.gd.gkquery.view.MaterialTextField;
import moe.feng.material.statusbar.TranslucentSBActivity;

public class MainActivity extends TranslucentSBActivity {

	/** Views */
	private EditText mEditNumber, mEditBirth, mEditVerifyCode;
	private ImageView mVerifyImage;

	/** Formatted result message */
	private String message;

	/** Saved-data key */
	private final static String KEY_NUMBER = "number", KEY_BIRTH = "birth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

	    /** Init CrashReport */
	    CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
	    strategy.setAppPackageName(getPackageName());

	    String versionName = null;
	    int versionCode = 0;
	    try {
		    PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		    versionName = packageInfo.versionName;
		    versionCode = packageInfo.versionCode;
	    } catch (PackageManager.NameNotFoundException e) {
		    e.printStackTrace();
	    }
	    strategy.setAppVersion(versionName + "(" + versionCode + ")");
	    CrashReport.initCrashReport(getApplicationContext(), Constants.BUGLY_APP_ID, Constants.BUGLY_ENABLE_DEBUG, strategy);

	    /** Setup the toolbar */
        Toolbar toolbar = $(R.id.toolbar);
        setSupportActionBar(toolbar);

	    /** Setup MaterialTextFields */
	    MaterialTextField mtf0 = $(R.id.mtf0), mtf1 = $(R.id.mtf1);
	    mtf1.expand();
	    mtf0.expand();

	    /** Init views */
	    mEditNumber = $(R.id.et_number);
	    mEditBirth = $(R.id.et_birth);
	    mEditVerifyCode = $(R.id.et_verify);
	    mVerifyImage = $(R.id.iv_verify);

	    mVerifyImage.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    new GetVerifyCodeTask().execute();
		    }
	    });
	    $(R.id.fab).setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    if (TextUtils.isEmpty(mEditNumber.getText()) || TextUtils.isEmpty(mEditBirth.getText()) || TextUtils.isEmpty(mEditVerifyCode.getText())) {
				    createToast(R.string.toast_please_input, Toast.LENGTH_SHORT).show();
				    return;
			    }
			    new QueryTask().execute(
					    mEditNumber.getText().toString(),
					    mEditBirth.getText().toString(),
					    mEditVerifyCode.getText().toString()
			    );
			    mEditVerifyCode.setText("");
		    }
	    });

	    /** Load saved data */
	    SharedPreferences sp = getSharedPreferences("saved.xml", MODE_PRIVATE);
	    if (sp.contains(KEY_NUMBER)) {
		    mEditNumber.setText(sp.getString(KEY_NUMBER, ""));
	    }
	    if (sp.contains(KEY_BIRTH)) {
		    mEditBirth.setText(sp.getString(KEY_BIRTH, ""));
	    }

	    /** Get verify code */
	    new GetVerifyCodeTask().execute();
    }

	private  <T extends View> T $(@IdRes int id) {
		return (T) findViewById(id);
	}

	private Toast createToast(@StringRes int resId, int duration) {
		return Toast.makeText(this, resId, duration);
	}

	private Toast createToast(CharSequence charSequence, int duration) {
		return Toast.makeText(this, charSequence, duration);
	}

	@Override
	public void onStop() {
		super.onStop();
		/** Save forms data */
		SharedPreferences sp = getSharedPreferences("saved.xml", MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(KEY_NUMBER, mEditNumber.getText().toString());
		editor.putString(KEY_BIRTH, mEditBirth.getText().toString());
		editor.apply();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
	    if (id == R.id.action_settings) {
		    new AlertDialog.Builder(this)
				    .setTitle(R.string.action_settings)
				    .setMessage(R.string.dialog_about)
				    .setPositiveButton(android.R.string.ok, null)
				    .show();
		    return true;
	    }
        return super.onOptionsItemSelected(item);
    }

	private class GetVerifyCodeTask extends AsyncTask<Void, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Void... voids) {
			try {
				return QueryApi.getVerifyCode();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				mVerifyImage.setImageBitmap(bitmap);
			} else {
				createToast(R.string.toast_verify_code_error, Toast.LENGTH_SHORT).show();
			}
		}

	}

	private class QueryTask extends AsyncTask<String, Void, Result> {

		@Override
		protected Result doInBackground(String... keys) {
			return QueryApi.getResult(keys[0], keys[1], keys[2]);
		}

		@Override
		protected void onPostExecute(Result result) {
			new GetVerifyCodeTask().execute();
			if (result.flag != 1) {
				if (result.msg != null) {
					createToast(result.msg, Toast.LENGTH_LONG).show();
				} else {
					createToast(R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
				}
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(R.string.dialog_title);
				message = getString(
						R.string.dialog_message,
						result.result.number,
						result.result.name,
						result.result.orderName,
						result.result.typeName,
						result.result.schoolName
				);
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setNeutralButton(R.string.dialog_button_copy, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						ClipboardUtils.putString(MainActivity.this, message);
						createToast(R.string.toast_copy_successfully, Toast.LENGTH_SHORT).show();
					}
				});
				builder.show();
			}
		}

	}

}
