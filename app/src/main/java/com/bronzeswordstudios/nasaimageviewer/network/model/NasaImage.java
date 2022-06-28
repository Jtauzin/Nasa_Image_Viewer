package com.bronzeswordstudios.nasaimageviewer.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NasaImage implements Parcelable {

	@SerializedName("href")
	@Expose
	private String href;

	@SerializedName("data")
	@Expose
	private List<GeneralData> data;

	@SerializedName("links")
	@Expose
	private List<ImageData> imageData;



	public NasaImage (String href, List<GeneralData> data, List<ImageData> imageData) {
		super();
		this.href = href;
		this.data = data;
		this.imageData = imageData;
	}



	protected NasaImage (Parcel in) {
		href = in.readString();
	}



	public static final Creator<NasaImage> CREATOR = new Creator<NasaImage>() {
		@Override
		public NasaImage createFromParcel (Parcel in) {
			return new NasaImage(in);
		}



		@Override
		public NasaImage[] newArray (int size) {
			return new NasaImage[size];
		}
	};



	public String getHref () {
		return href;
	}



	public void setHref (String href) {
		this.href = href;
	}



	public List<GeneralData> getData () {
		return data;
	}



	public void setData (List<GeneralData> data) {
		this.data = data;
	}



	public List<ImageData> getImages () {
		return imageData;
	}



	public void setImages (List<ImageData> imageData) {
		this.imageData = imageData;
	}



	@Override
	public int describeContents () {
		return 0;
	}



	@Override
	public void writeToParcel (final Parcel parcel, final int i) {
		parcel.writeString(href);
	}
}
