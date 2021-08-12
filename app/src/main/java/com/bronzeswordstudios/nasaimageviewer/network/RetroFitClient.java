package com.bronzeswordstudios.nasaimageviewer.network;

import android.content.Context;

import com.readystatesoftware.chuck.ChuckInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroFitClient {

	OkHttpClient client;
	private static Retrofit retrofit;
	private static final String BASE_URL = "https://images-api.nasa.gov/";



	public RetroFitClient (Context context) {
		client = new OkHttpClient.Builder()
				.addInterceptor(new ChuckInterceptor(context))
				.build();
	}



	public Retrofit getRetrofitInstance () {
		if (retrofit == null) {
			retrofit = new retrofit2.Retrofit.Builder()
					.baseUrl(BASE_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.client(client)
					.build();
		}

		return retrofit;
	}
}
