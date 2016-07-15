package moe.feng.gd.gkquery.view;

import android.content.Context;
import android.util.AttributeSet;

public class MaterialTextField extends com.github.florent37.materialtextfield.MaterialTextField {

	public MaterialTextField(Context context) {
		super(context);
	}

	public MaterialTextField(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MaterialTextField(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void toggle() {
		if (!expanded) {
			expand();
		}
	}

}
