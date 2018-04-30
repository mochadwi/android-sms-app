package io.mochadwi.analyticssms.data.network

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit



/**
 * Created by mochadwi on 4/30/18.
 */
class ApiClient {
    companion object {
        val BASE_URL = "https://api.androidhive.info/json/"
        private var retrofit: Retrofit? = null

        val client: Retrofit
            get() {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                }
                return retrofit!!
            }
    }
}