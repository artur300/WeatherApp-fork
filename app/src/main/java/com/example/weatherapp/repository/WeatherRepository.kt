package com.example.weatherapp.repository

import retrofit2.Response
import com.example.weatherapp.api.WeatherAPI
import com.example.weatherapp.db.WeatherDatabase
import com.example.weatherapp.models.CityResponse
import com.example.weatherapp.models.WeatherResponse
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: WeatherAPI,
    private val db : WeatherDatabase
) {
    //---------------arthur code------------------
    suspend fun getCoordinates(city: String, country: String? = null): Response<List<CityResponse>> {
        val query = if (country.isNullOrEmpty()) city else "$city,$country"
        return api.getCoordinates(query)
    }
    //---------פתוח להרחבות בעתיד אפשר לחפש רק לפי עיר או גם עיר וגם מדינה----


    suspend fun getCitiesFromAPI(query: String): Response<List<CityResponse>> {
        return api.searchCities(query) // וודא שזה מעביר את הפרמטר
    }



    //--------------------------------------------

    suspend fun getWeatherData(lat: Double, lon: Double, unit: String) =
        api.getWeatherData(lat,lon,unit)

    suspend fun insertWeatherData(weatherResponse: WeatherResponse) =
        db.getWeatherDao().upsertWeatherData(weatherResponse)

    fun getSavedWeather() = db.getWeatherDao().getAllWeatherData()

    suspend fun deleteWeather(weatherResponse: WeatherResponse) =
        db.getWeatherDao().deleteWeatherData(weatherResponse)
}
