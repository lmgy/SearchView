package com.lmgy.searchview.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.e
import android.view.Menu
import android.view.View
import com.lmgy.searchview.SearchView
import com.lmgy.searchview.SearchView.OnQueryTextListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        searchView.setOnQueryTextListener(object : OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                e("asd", query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                e("asd", newText)
                return false
            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search_view, menu)
        val item = menu?.findItem(R.id.action_search)
        searchView.setMenuItem(item)
        return true
    }
}
