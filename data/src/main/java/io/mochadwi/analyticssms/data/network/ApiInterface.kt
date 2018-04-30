package io.mochadwi.analyticssms.data.network

import io.mochadwi.analyticssms.domain.SmsEntity
import retrofit2.Call
import retrofit2.http.GET



/**
 * Created by mochadwi on 4/30/18.
 */
interface ApiInterface {

    @get:GET("inbox.json")
    var inbox: Call<List<SmsEntity>>

}