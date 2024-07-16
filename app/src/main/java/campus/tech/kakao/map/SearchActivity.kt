package campus.tech.kakao.map

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import campus.tech.kakao.map.model.RecentSearchWord
import campus.tech.kakao.map.databinding.ActivitySearchBinding
import campus.tech.kakao.map.viewModel.MapRepository
import campus.tech.kakao.map.viewModel.PlacesViewModel
import campus.tech.kakao.map.viewModel.PlacesViewModelFactory

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: PlacesViewModel
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = MapRepository(this)
        val viewModelFactory = PlacesViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PlacesViewModel::class.java)

        setUpSearchHistoryAdapter()
        setUpPlacesAdapter()
        setUpViewModelObservers()

        binding.searchInput.addTextChangedListener { text ->
            viewModel.searchPlaces(text.toString())
        }

        binding.deleteInput.setOnClickListener {
            binding.searchInput.text.clear()
        }
    }

    private fun setUpSearchHistoryAdapter() {
        searchHistoryAdapter = SearchHistoryAdapter(
            onDeleteClick = { position: Int ->
                viewModel.delSearch(position)
            },
            onTextClick = { position: Int ->
                val itemName = searchHistoryAdapter.getItemName(position)
                binding.searchInput.setText(itemName)
            })
        binding.searchHistory.adapter = searchHistoryAdapter
    }

    private fun setUpPlacesAdapter() {
        placesAdapter = PlacesAdapter { position: Int ->
            val itemName = placesAdapter.getItemName(position)
            viewModel.insertSearch(position, itemName)
        }
        binding.placesRView.adapter = placesAdapter
        binding.placesRView.layoutManager = LinearLayoutManager(this)
    }

    private fun setUpViewModelObservers() {
        viewModel.places.observe(this, Observer { places ->
            placesAdapter.updateList(places)
            placesAdapter.notifyDataSetChanged()
            binding.textView.visibility =
                if (placesAdapter.itemCount <= 0) View.VISIBLE else View.GONE
        })

        viewModel.searchHistoryData.observe(this, Observer {  searchHistoryData ->
            searchHistoryAdapter.submitList(searchHistoryData.toList())
            binding.searchHistory.isVisible = searchHistoryData.isNotEmpty()
        })
    }
}
