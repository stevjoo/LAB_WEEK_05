package com.example.lab_week_05

import com.example.lab_week_05.model.ImageData
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.example.lab_week_05.api.CatApiService
import com.example.lab_week_05.model.CatBreedData
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {
    private val retrofit by lazy{
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    private val apiResponseView: TextView by lazy {
        findViewById(R.id.api_response)
    }

    private val imageResultView: ImageView by lazy {
        findViewById(R.id.image_result)
    }
    private val imageLoader: ImageLoader by lazy {
    GlideLoader(this)
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getCatImageResponse()
        getCatBreeds()
    }

    private fun getCatImageResponse() {
        val call = catApiService.searchImages(1, "full")
        call.enqueue(object: Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "Failed to get response", t)
            }
            override fun onResponse(call: Call<List<ImageData>>, response: Response<List<ImageData>>) {
                if(response.isSuccessful){
                    val image = response.body()
                    val firstImage = image?.firstOrNull()?.imageUrl.orEmpty()
                    if (firstImage.isNotBlank()) {
                        imageLoader.loadImage(firstImage, imageResultView)
                    } else {
                        Log.d(MAIN_ACTIVITY, "Missing image URL")
                    }
                    apiResponseView.text = getString(R.string.image_placeholder,
                        firstImage)
                }
                else{
                    Log.e(MAIN_ACTIVITY, "Failed to get response\n" +
                            response.errorBody()?.string().orEmpty()
                    )
                }
            }
        })
    }

    private fun getCatBreeds() {
        val call = catApiService.getBreeds()
        call.enqueue(object : Callback<List<CatBreedData>> {
            override fun onFailure(call: Call<List<CatBreedData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "Failed to get breeds", t)
            }

            override fun onResponse(call: Call<List<CatBreedData>>, response: Response<List<CatBreedData>>) {
                if (response.isSuccessful) {
                    val breeds = response.body().orEmpty()
                    if (breeds.isNotEmpty()) {
                        val firstBreed = breeds.first()
                        Log.d(MAIN_ACTIVITY, "First breed: ${firstBreed.name}, from ${firstBreed.origin}")
                        apiResponseView.text = "${firstBreed.name} (${firstBreed.origin})"
                    } else {
                        Log.d(MAIN_ACTIVITY, "No breeds found")
                    }
                } else {
                    Log.e(MAIN_ACTIVITY, "Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    companion object {
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
    }
}