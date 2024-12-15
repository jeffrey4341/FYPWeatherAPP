package com.jeffrey.fypweatherapp.weather.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.jeffrey.fypweatherapp.weather.MainActivity;

public class FontTextView extends androidx.appcompat.widget.AppCompatTextView{

	public FontTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(isInEditMode()){
			return ;
		}
//		setIncludeFontPadding(false);
		setTypeface(MainActivity.getTypeface(context));
	}

}
