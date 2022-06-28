package com.bronzeswordstudios.nasaimageviewer.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GeneralData {

	@SerializedName("date_created")
	@Expose
	private String dateCreated;
	@SerializedName("center")
	@Expose
	private String center;
	@SerializedName("title")
	@Expose
	private String title;
	@SerializedName("description")
	@Expose
	private String description;



	public GeneralData (String dateCreated, String center, String title) {
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



	public String getDescription () { return description; }



	public void setDescription (final String description) { this.description = description; }
}
