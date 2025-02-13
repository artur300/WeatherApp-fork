package com.example.weatherapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.R
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.example.weatherapp.models.CityResponse
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val app: Application
) : AndroidViewModel(app) {

    val weatherData = MutableLiveData<Resource<WeatherResponse>>()
    val cityList = MutableLiveData<List<CityResponse>>()

    fun getWeatherByCity(city: String) {
        weatherData.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val response = repository.getCoordinates(city)
                if (response.isSuccessful) {
                    val firstCity = response.body()?.firstOrNull()

                    firstCity?.let {
                        val weatherResponse = repository.getWeatherData(it.lat, it.lon, "metric")
                        handleWeatherResponse(weatherResponse)
                    } ?: weatherData.postValue(Resource.Error(app.getString(R.string.error_city_not_found)))

                } else {
                    weatherData.postValue(Resource.Error(app.getString(R.string.error_fetch_coordinates)))
                }
            } catch (e: Exception) {
                weatherData.postValue(Resource.Error(app.getString(R.string.error_fetch_weather)))
                Log.e("CitySearchViewModel", "Error fetching weather", e)
            }
        }
    }

    fun searchCities(query: String) {
        if (query.length < 2) return

        viewModelScope.launch {
            try {
                val response = repository.getCitiesFromAPI(query)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    cityList.postValue(response.body())
                } else {
                    cityList.postValue(emptyList())
                }
            } catch (e: Exception) {
                cityList.postValue(emptyList())
                Log.e("CitySearchViewModel", "Error searching cities", e)
            }
        }
    }

    private fun handleWeatherResponse(response: retrofit2.Response<WeatherResponse>) {
        if (response.isSuccessful) {
            response.body()?.let {
                weatherData.postValue(Resource.Success(it))
            } ?: weatherData.postValue(Resource.Error(app.getString(R.string.error_no_data)))
        } else {
            weatherData.postValue(Resource.Error(app.getString(R.string.error_fetch_weather)))
        }
    }
}
