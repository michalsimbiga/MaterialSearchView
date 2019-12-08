package com.example.materialsearchbar

import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.materialsearchbar.databinding.*
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlin.properties.Delegates

typealias onSuggestionClick = (AutocompletePrediction) -> Unit

class SuggestionsRecyclerViewAdapter: RecyclerView.Adapter<SuggestionsRecyclerViewAdapter.BaseViewHolder<*>>() {

    private var attachmentList: List<Any> by Delegates.observable(emptyList()) {_, oldList, newList ->
        autoNotify(oldList, newList) {oldData, newData -> oldData == newData}
    }

    private lateinit var onSuggestionClick: onSuggestionClick

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        return when(holder){
            is SuggestionHolder ->  holder.bind(attachmentList[position] as AutocompletePrediction, onSuggestionClick)
            is HeaderHolder ->  holder.bind("Suggestions")
            else -> holder.bind()
        }
    }

    fun setOnSuggestionCallback(newOnSuggestionClick: onSuggestionClick) {
        onSuggestionClick = newOnSuggestionClick
    }

    override fun getItemViewType(position: Int): Int {
        return when(attachmentList[position]){
            is String -> TYPE_STRING
            is AutocompletePrediction -> TYPE_SUGGESTION
            else -> TYPE_SEPARATOR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType){
            TYPE_SUGGESTION -> {
                val binding = SearchItemLayoutBinding.inflate(inflater, parent, false)
                SuggestionHolder(binding)
            }
            TYPE_STRING -> {
                val binding = SearchHeaderLayoutBinding.inflate(inflater, parent, false)
                HeaderHolder(binding)
            }
            else -> {
                val binding = SearchSeparatorLayoutBinding.inflate(inflater, parent, false)
                return SeparatorHolder(binding)}
        }
    }

    fun setSuggestions(list: List<AutocompletePrediction>){
        val newList = mutableListOf<Any>()
        newList.add("Suggestions")
        for (suggestion in list){
            newList.add(suggestion)
            newList.add(Any())
        }
        attachmentList = newList
    }


    override fun getItemCount() = attachmentList.size

    class SuggestionHolder(private val binding: SearchItemLayoutBinding): BaseViewHolder<AutocompletePrediction>(binding.root) {
        override fun bind(item: AutocompletePrediction?, onClickListener: ((AutocompletePrediction) -> Unit)?){
            with(binding) {
                item?.let {
                    searchItemHead.text = it.getPrimaryText(StyleSpan(Typeface.BOLD))
                    searchItemBody.text = it.getSecondaryText(StyleSpan(Typeface.NORMAL))
                    searchItemContainer.setOnClickListener { onClickListener?.invoke(item) }
                    executePendingBindings()
                }
            }
        }
    }

    class HeaderHolder(private val binding: SearchHeaderLayoutBinding): BaseViewHolder<String>(binding.root){
        override fun bind(item: String?, onClickListener: ((String) -> Unit)?) {
            with(binding){
                searchItemText.text = item
            }
        }
    }

    class SeparatorHolder(binding: SearchSeparatorLayoutBinding): BaseViewHolder<String>(binding.root){
        override fun bind(item: String?, onClickListener: ((String) -> Unit)?) {}
    }

    abstract class BaseViewHolder<T>(itemView : View): RecyclerView.ViewHolder(itemView){
        abstract fun bind(item: T? = null, onClickListener: ((T) -> Unit)? = null)
    }

    companion object {
        private const val TYPE_STRING = 1
        private const val TYPE_SUGGESTION = 2
        private const val TYPE_SEPARATOR = 3
    }
}