package com.example.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class PictureUtils {

	//Метод масштабирования с консервативной оценкой
	public static Bitmap getScaledBitmap(String path, Activity activity) {
		Point size=new Point();
		activity.getWindowManager().getDefaultDisplay().getSize(size);

		return getScaledBitmap(path,size.x,size.y);
	}

	public static Bitmap getScaledBitmap (String path, int destWidth, int destHeight)
	{
		// Чтение размеров изображения на диске
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(path,options);

		float srcWidth=options.outWidth;
		float srcHeight=options.outHeight;

		// Вычисление степени масштабирования
		int inSampleSize=1;  //если =2, то количество пикселов в изображении составляет четверть от количествапикселов оригинала
		if(srcHeight>destHeight||srcWidth>destWidth) {
			float heightScale=srcHeight/destHeight;
			float widthScale=srcWidth/destWidth;
			inSampleSize=Math.round(heightScale>widthScale?heightScale:widthScale);
		}

		options=new BitmapFactory.Options();
		options.inSampleSize=inSampleSize;

		// Чтение данных и создание итогового изображения
		return BitmapFactory.decodeFile(path, options);
	}

}
