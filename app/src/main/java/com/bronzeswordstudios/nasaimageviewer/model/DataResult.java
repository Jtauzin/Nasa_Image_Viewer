package com.bronzeswordstudios.nasaimageviewer.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataResult {

	@SerializedName("collection")
	@Expose
	private Collection collection;



	public DataResult (Collection collection) {
		this.collection = collection;
	}



	public Collection getCollection () {
		return collection;
	}



	public void setCollection (Collection collection) {
		this.collection = collection;
	}



	private String adjustURL (String inputString) {
		String[] urlSplit = inputString.split("thumb");
		return urlSplit[0] + "large.jpg";
	}

}
