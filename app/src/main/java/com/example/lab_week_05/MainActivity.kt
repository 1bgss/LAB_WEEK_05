package com.example.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_week_05.api.CatApiService
import com.example.lab_week_05.model.ImageData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    companion object {
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    private val imageResultView: ImageView by lazy { findViewById(R.id.image_result) }
    private val breedNameView: TextView by lazy { findViewById(R.id.breed_name) }
    private val breedTempView: TextView by lazy { findViewById(R.id.breed_temp) }

    private val imageLoader: ImageLoader by lazy { GlideLoader(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getCatImageResponse()
    }

    private fun getCatImageResponse() {
        val call = catApiService.searchImages(1, "full")
        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "Failed to get response", t)
                breedNameView.text = "Request failed"
                breedTempView.text = ""
            }

            override fun onResponse(
                call: Call<List<ImageData>>,
                response: Response<List<ImageData>>
            ) {
                if (response.isSuccessful) {
                    val image = response.body()?.firstOrNull()
                    if (image != null) {
                        imageLoader.loadImage(image.imageUrl, imageResultView)

                        val breed = image.breeds?.firstOrNull()
                        val breedName = breed?.name ?: "Unknown"
                        val temperament = breed?.temperament ?: "-"

                        breedNameView.text = "Breed: $breedName"
                        breedTempView.text = "Temperament: $temperament"
                    } else {
                        breedNameView.text = "No image found"
                        breedTempView.text = ""
                    }
                } else {
                    Log.e(
                        MAIN_ACTIVITY,
                        "Failed to get response\n${response.errorBody()?.string().orEmpty()}"
                    )
                    breedNameView.text = "Response error"
                    breedTempView.text = ""
                }
            }
        })
    }
}
