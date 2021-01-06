package com.example.criminalintent;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;

public class PhotoFragment extends DialogFragment {


	public static  final String ARG_FILE="file";

	private File mPhotoFile;
	private ImageView mPhotoImageView;

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

		View view= LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo,null);
		mPhotoFile=(File) getArguments().getSerializable(ARG_FILE);
		mPhotoImageView=(ImageView)view.findViewById(R.id.dialog_photo_imgview);

		//Bitmap bitmap=PictureUtils.getScaledBitmap(mPhotoFile.getPath(),getActivity());
		//mPhotoImageView.setImageBitmap(bitmap);

		mPhotoImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
			@Override
			public void onGlobalLayout() {
				Bitmap bitmap=PictureUtils.getScaledBitmap(mPhotoFile.getPath(),mPhotoImageView.getWidth(),mPhotoImageView.getHeight());
				mPhotoImageView.setImageBitmap(bitmap);
			}
		});

		return new AlertDialog.Builder(getActivity())
				.setView(view)
				.create();

	}

	public static PhotoFragment newInstance(File photoFile)
	{
		Bundle args=new Bundle();
		args.putSerializable(ARG_FILE,photoFile);

		PhotoFragment photoFragment=new PhotoFragment();
		photoFragment.setArguments(args);
		return  photoFragment;
	}
}
