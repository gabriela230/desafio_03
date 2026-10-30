package com.udb.aprendeapp.controller

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.udb.aprendeapp.databinding.ActivityMainBinding
import com.udb.aprendeapp.model.*
import com.udb.aprendeapp.view.ResourceAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ResourceAdapter
    private var allResources: List<Resource> = emptyList()
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        currentUser = sessionManager.getUser()

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUI()
        setupAdapter()
    }

    override fun onResume() {
        super.onResume()
        loadResourcesFromApi()
    }

    private fun setupUI() {
        binding.tvUserHeader.text = "${currentUser?.username} (${currentUser?.role})"

        if (currentUser?.role == UserRole.DOCENTE) {
            binding.btnAddResource.visibility = View.VISIBLE
            binding.btnAddResource.setOnClickListener {
                startActivity(Intent(this, AddEditResourceActivity::class.java))
            }
        } else {
            binding.btnAddResource.visibility = View.GONE
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Spinner Setup
        val filterOptions = arrayOf("Todos", "Libro", "Video", "Articulo")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filterOptions)
        binding.spinnerFilter.adapter = spinnerAdapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.cbFavorites.setOnCheckedChangeListener { _, _ ->
            applyFilters()
        }
    }

    private fun setupAdapter() {
        adapter = ResourceAdapter(
            resources = emptyList(),
            favoriteIds = sessionManager.getFavorites(),
            onItemClick = { resource ->
                val intent = Intent(this, ResourceDetailActivity::class.java)
                intent.putExtra("RESOURCE", resource)
                startActivity(intent)
            },
            onFavClick = { resource ->
                sessionManager.toggleFavorite(resource.id)
                applyFilters()
            }
        )
        binding.rvResources.layoutManager = LinearLayoutManager(this)
        binding.rvResources.adapter = adapter
    }

    private fun loadResourcesFromApi() {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.instance.getResources().enqueue(object : Callback<List<Resource>> {
            override fun onResponse(call: Call<List<Resource>>, response: Response<List<Resource>>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    allResources = response.body()!!
                    applyFilters()
                } else {
                    Toast.makeText(this@MainActivity, "Error al cargar recursos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Resource>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Fallo de conexión API", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val selectedFilter = binding.spinnerFilter.selectedItem.toString()
        val showOnlyFavs = binding.cbFavorites.isChecked
        val favSet = sessionManager.getFavorites()

        val filtered = allResources.filter { resource ->
            val matchesQuery = resource.titulo.lowercase().contains(query) ||
                    resource.tipo.lowercase().contains(query) ||
                    resource.id.lowercase().contains(query)

            val matchesType = when (selectedFilter) {
                "Libro" -> resource.tipo.contains("libro", ignoreCase = true)
                "Video" -> resource.tipo.contains("video", ignoreCase = true)
                "Articulo" -> resource.tipo.contains("articulo", ignoreCase = true) || resource.tipo.contains("artículo", ignoreCase = true)
                else -> true
            }

            val matchesFav = if (showOnlyFavs) favSet.contains(resource.id) else true

            matchesQuery && matchesType && matchesFav
        }

        adapter.updateData(filtered, favSet)
    }
}
