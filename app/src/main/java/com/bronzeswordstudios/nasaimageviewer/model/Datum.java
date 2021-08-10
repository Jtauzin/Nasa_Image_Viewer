package com.bronzeswordstudios.nasaimageviewer.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {

	@SerializedName("date_created")
	@Expose
	private String dateCreated;
	@SerializedName("center")
	@Expose
	private String center;
	@SerializedName("title")
	@Expose
	private String title;



	public Datum (String dateCreated, String center, String title) {
		this.dateCreated = dateCreated;
		this.center = center;
		this.title = title;
	}



	public String getDateCreated () {
		return dateCreated;
	}



	public void setDateCreated (String dateCreated) {
		this.dateCreated = dateCreated;
	}



	public String getCenter () {
		return center;
	}



	public void setCenter (String center) {
		this.center = center;
	}



	public String getTitle () {
		return title;
	}



	public void setTitle (String title) {
		this.title = title;
	}
}
