package com.example.weatherapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.weatherapp.R
import com.example.weatherapp.adapters.CityAutoCompleteAdapter
import com.example.weatherapp.databinding.FragmentCitySearchBinding
import com.example.weatherapp.ui.CitySearchViewModel
import com.example.weatherapp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class CitySearchFragment : Fragment() {

    private lateinit var binding: FragmentCitySearchBinding
    private val viewModel: CitySearchViewModel by viewModels()
    private lateinit var cityAdapter: CityAutoCompleteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCitySearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        observeWeatherData()
        observeCityList()

        binding.etCityName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    viewModel.searchCities(s.toString())
                }
            }
        })

        binding.etCityName.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = cityAdapter.getItem(position)
            binding.etCityName.setText(selectedCity)
            viewModel.getWeatherByCity(selectedCity)
        }

        binding.btnSearch.setOnClickListener {
            val enteredCity = binding.etCityName.text.toString()
            if (enteredCity.isNotEmpty()) {
                viewModel.getWeatherByCity(enteredCity)
            } else {
                Toast.makeText(requireContext(), getString(R.string.please_enter_city_country), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initAdapter() {
        cityAdapter = CityAutoCompleteAdapter(requireContext(), emptyList())
        binding.etCityName.setAdapter(cityAdapter)
    }

    private fun observeCityList() {
        viewModel.cityList.observe(viewLifecycleOwner) { cities ->
            cityAdapter.updateCities(cities)
        }
    }

    private fun observeWeatherData() {
        viewModel.weatherData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val weather = resource.data
                    hideLoading()
                    with(binding) {
                        tvCity.text = getString(R.string.label_city, weather?.name ?: getString(R.string.city_n_a))
                        tvCountry.text = getString(R.string.label_country, Locale("", weather?.sys?.country ?: "").displayCountry)
                        tvTemperature.text = getString(R.string.label_temp, weather?.main?.temp ?: "--")
                        tvWindSpeed.text = getString(R.string.label_wind, weather?.wind?.speed ?: "--")
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), resource.message ?: getString(R.string.error_fetch_weather), Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        }
    }

    private fun showLoading() {
        binding.tvCity.text = getString(R.string.loading)
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
}
