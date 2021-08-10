package com.bronzeswordstudios.nasaimageviewer.network;

import com.bronzeswordstudios.nasaimageviewer.model.DataResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface API {

	@GET("search?q=galaxy&media_type=image")
	Call<DataResult> getVariety0 ();

	@GET("search?q=solar&media_type=image")
	Call<DataResult> getVariety1 ();

	@GET("search?q=asteroid&media_type=image")
	Call<DataResult> getVariety2 ();

	@GET("search?q=UFO&media_type=image")
	Call<DataResult> getVariety3 ();

	@GET("search?q=moon&media_type=image")
	Call<DataResult> getVariety4 ();

	@GET("search?q=mars+rover&media_type=image")
	Call<DataResult> getVariety5 ();

	@GET("search?q=mercury&media_type=image")
	Call<DataResult> getVariety6 ();

	@GET("search?q=venus&media_type=image")
	Call<DataResult> getVariety7 ();

	@GET("search?q=earth&media_type=image")
	Call<DataResult> getVariety8 ();

	@GET("search?q=mars&media_type=image")
	Call<DataResult> getVariety9 ();

	@GET("search?q=jupiter&media_type=image")
	Call<DataResult> getVariety10 ();

	@GET("search?q=saturn&media_type=image")
	Call<DataResult> getVariety11 ();

	@GET("search?q=uranus&media_type=image")
	Call<DataResult> getVariety12 ();

	@GET("search?q=neptune&media_type=image")
	Call<DataResult> getVariety13 ();

	@GET("search?q=pluto&media_type=image")
	Call<DataResult> getVariety14 ();

	@GET("search")
	Call<DataResult> getSearch (@Query("q") String query, @Query("media_type") String imageType);

}
