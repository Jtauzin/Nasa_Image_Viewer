package com.bronzeswordstudios.nasaimageviewer.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Collection {

	@SerializedName("href")
	@Expose
	private String href;
	@SerializedName("items")
	@Expose
	private List<NasaImage> nasaImages;



	public Collection (String href, List<NasaImage> nasaImages) {
		this.href = href;
		this.nasaImages = nasaImages;
	}



	public String getHref () {
		return href;
	}



	public void setHref (String href) {
		this.href = href;
	}



	public Collection withHref (String href) {
		this.href = href;
		return this;
	}



	public List<NasaImage> getItems () {
		return nasaImages;
	}



	public void setItems (List<NasaImage> nasaImages) {
		this.nasaImages = nasaImages;
	}



	public Collection withItems (List<NasaImage> nasaImages) {
		this.nasaImages = nasaImages;
		return this;
	}

}
