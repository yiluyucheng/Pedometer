package com.fyspring.stepcounter;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


public class ViewVibration
{
	public static void SetViewVibration(Context context, View view)
	{
		if (view == null)
			return;
		Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
		view.startAnimation(shake);
	}
}
