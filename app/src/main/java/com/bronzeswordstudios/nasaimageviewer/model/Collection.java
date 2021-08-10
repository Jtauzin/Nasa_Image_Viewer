package com.bronzeswordstudios.nasaimageviewer.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Collection {

	@SerializedName("href")
	@Expose
	private String href;
	@SerializedName("items")
	@Expose
	private List<Item> items = null;



	public Collection (String href, List<Item> items) {
		this.href = href;
		this.items = items;
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



	public List<Item> getItems () {
		return items;
	}



	public void setItems (List<Item> items) {
		this.items = items;
	}



	public Collection withItems (List<Item> items) {
		this.items = items;
		return this;
	}

}
