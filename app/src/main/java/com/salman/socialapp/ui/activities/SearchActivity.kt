package com.salman.socialapp.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.salman.socialapp.R
import com.salman.socialapp.adapters.SearchAdapter
import com.salman.socialapp.model.User
import com.salman.socialapp.viewmodels.SearchViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    lateinit var searchViewModel: SearchViewModel
    lateinit var searchAdapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchViewModel = ViewModelProvider(this, ViewModelFactory()).get(SearchViewModel::class.java)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            super.onBackPressed()
        }

        setupRecyclerView()

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                searchUsers(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.length > 2) {
                    searchUsers(newText)
                } else {
                    defaultTV.visibility = View.VISIBLE
                    searchAdapter.updateList(emptyList())
                }
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(this)
        searchRV.layoutManager = LinearLayoutManager(this)
        searchRV.adapter = searchAdapter
    }

    private fun searchUsers(query: String) {
        val params = HashMap<String, String>()
        params.put("keyword", query)
        searchViewModel.search(params)?.observe(this, Observer { searchResponse ->
            if (searchResponse.status == 200) {
                defaultTV.visibility = View.GONE
                searchAdapter.updateList(searchResponse.user)
            } else {
                Toast.makeText(this, searchResponse.message, Toast.LENGTH_SHORT).show()
                defaultTV.visibility = View.GONE
            }
        })
    }
}