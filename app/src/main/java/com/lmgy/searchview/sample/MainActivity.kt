package com.lmgy.searchview.sample

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.lmgy.searchview.SearchView
import com.lmgy.searchview.SearchView.OnQueryTextListener

class MainActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        searchView = findViewById(R.id.searchView)

        setSupportActionBar(toolbar)
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Toast.makeText(this@MainActivity, query, Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

        })
    }

    override fun onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search_view, menu)
        val item = menu?.findItem(R.id.action_search)
        searchView.setMenuItem(item)
        return true
    }
}
