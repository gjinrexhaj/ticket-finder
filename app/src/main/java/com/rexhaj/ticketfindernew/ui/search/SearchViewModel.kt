package com.rexhaj.ticketfindernew.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.rexhaj.ticketfindernew.EventData
import com.rexhaj.ticketfindernew.RecyclerAdapter

private const val TAG = "SearchViewModel"

class SearchViewModel : ViewModel() {


    private val _text = MutableLiveData<String>().apply {
        value = "This is search Fragment"
    }
    val text: LiveData<String> = _text
}