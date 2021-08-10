package com.bronzeswordstudios.nasaimageviewer.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

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



	public ArrayList<NasaImage> getNasaImages () {
		ArrayList<NasaImage> nasaImages = new ArrayList<NasaImage>();
		for (Item item : this.collection.getItems()) {
			try {
				NasaImage nasaImage = new NasaImage(
						adjustURL(item.getLinks().get(0).getHref()),
						item.getData().get(0).getTitle(),
						item.getData().get(0).getCenter(),
						item.getData().get(0).getDateCreated(),
						item.getLinks().get(0).getHref());
				nasaImages.add(nasaImage);
			} catch (Exception e) {
				// Strange error when trying to call a log message here
			}
		}
		return nasaImages;
	}



	private String adjustURL (String inputString) {
		String[] urlSplit = inputString.split("thumb");
		return urlSplit[0] + "large.jpg";
	}

}
