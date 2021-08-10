package com.bronzeswordstudios.nasaimageviewer.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Item {

	@SerializedName("href")
	@Expose
	private String href;

	@SerializedName("data")
	@Expose
	private List<Datum> data = null;

	@SerializedName("links")
	@Expose
	private List<Link> links = null;



	public Item (String href, List<Datum> data, List<Link> links) {
		super();
		this.href = href;
		this.data = data;
		this.links = links;
	}



	public String getHref () {
		return href;
	}



	public void setHref (String href) {
		this.href = href;
	}



	public List<Datum> getData () {
		return data;
	}



	public void setData (List<Datum> data) {
		this.data = data;
	}



	public List<Link> getLinks () {
		return links;
	}



	public void setLinks (List<Link> links) {
		this.links = links;
	}

}
