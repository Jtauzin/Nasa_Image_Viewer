package com.bronzeswordstudios.nasaimageviewer.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NasaImage {

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

}
