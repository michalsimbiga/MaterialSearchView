package com.example.materialsearchbar

import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction

abstract class RecyclerAdapterAbstract: RecyclerView.Adapter<SuggestionsRecyclerViewAdapter.BaseViewHolder<*>>() {

    abstract fun setSuggestions(list: List<AutocompletePrediction>)
    abstract fun setLoading()
    abstract fun setOnSuggestionCallback(newOnSuggestionClick: onSuggestionClick)
}