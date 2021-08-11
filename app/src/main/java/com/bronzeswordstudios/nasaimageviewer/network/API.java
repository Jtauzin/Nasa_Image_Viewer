package com.bronzeswordstudios.nasaimageviewer.network;

import com.bronzeswordstudios.nasaimageviewer.model.DataResult;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface API {

	ArrayList<String> queryList = new ArrayList<String>(Arrays.asList(
			"galaxy", "solar", "asteroid", "UFO", "moon", "mars rover", "mercury", "venus", "earth", "mars", "jupiter", "saturn", "uranus", "neptune",
			"pluto"
	));

	@GET("search?media_type=image")
	Call<DataResult> getSearch (@Query("q") String query);

}
