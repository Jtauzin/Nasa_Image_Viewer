package com.bronzeswordstudios.nasaimageviewer.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Link {

	@SerializedName("href")
	@Expose
	private String href;



	public Link (String href) {
		super();
		this.href = href;
	}



	public String getHref () {
		return href;
	}



	public void setHref (String href) {
		this.href = href;
	}

}
