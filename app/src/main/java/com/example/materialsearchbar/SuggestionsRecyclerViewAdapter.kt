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

class SuggestionsRecyclerViewAdapter :
    RecyclerAdapterAbstract() {

    private var attachmentList: List<Any> by Delegates.observable(emptyList()) { _, oldList, newList ->
        autoNotify(oldList, newList) { oldData, newData -> oldData == newData }
    }

    private lateinit var onSuggestionClick: onSuggestionClick

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        return when (holder) {
            is SuggestionHolder -> holder.bind(
                attachmentList[position] as AutocompletePrediction,
                onSuggestionClick
            )
            is HeaderHolder -> holder.bind(attachmentList[position] as Header)
            is LoadingHolder -> holder.bind()
            else -> holder.bind()
        }
    }

    override fun setOnSuggestionCallback(newOnSuggestionClick: onSuggestionClick) {
        onSuggestionClick = newOnSuggestionClick
    }

    override fun getItemViewType(position: Int): Int {
        return when (attachmentList[position]) {
            is Header -> TYPE_HEADER
            is AutocompletePrediction -> TYPE_SUGGESTION
            is Loading -> TYPE_LOADING
            else -> TYPE_SEPARATOR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_SUGGESTION -> {
                val binding =
                    MaterialSearchViewSuggestionLayoutBinding.inflate(inflater, parent, false)
                SuggestionHolder(binding)
            }
            TYPE_HEADER -> {
                val binding =
                    MaterialSearchViewSuggestionHeaderLayoutBinding.inflate(inflater, parent, false)
                HeaderHolder(binding)
            }
            TYPE_LOADING -> {
                val binding =
                    MaterialSearchViewSuggestionLoadingLayoutBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                LoadingHolder(binding)
            }
            else -> {
                val binding = MaterialSearchViewSuggestionSeparatorLayoutBinding.inflate(
                    inflater,
                    parent,
                    false
                )
                SeparatorHolder(binding)
            }
        }
    }

    override fun setSuggestions(list: List<AutocompletePrediction>) {
        val newList = mutableListOf<Any>()
        newList.add(Header("Suggestions"))
        for (suggestion in list) {
            newList.add(suggestion)
            newList.add(Any())
        }
        attachmentList = newList
    }

    override fun setLoading() {
        val newList = mutableListOf<Any>().apply { addAll(attachmentList) }
        newList.add(FISRT_INDEX, Loading())
        attachmentList = newList
    }

    override fun getItemCount() = attachmentList.size

    class SuggestionHolder(private val binding: MaterialSearchViewSuggestionLayoutBinding) :
        BaseViewHolder<AutocompletePrediction>(binding.root) {
        override fun bind(
            item: AutocompletePrediction?,
            onClickListener: ((AutocompletePrediction) -> Unit)?
        ) {
            with(binding) {
                item?.let {
                    searchItemHead.text = it.getPrimaryText(android.text.style.StyleSpan(android.graphics.Typeface.BOLD))
                    searchItemBody.text = it.getSecondaryText(android.text.style.StyleSpan(android.graphics.Typeface.NORMAL))
                    searchItemContainer.setOnClickListener { onClickListener?.invoke(item) }
                    executePendingBindings()
                }
            }
        }
    }

    class HeaderHolder(private val binding: MaterialSearchViewSuggestionHeaderLayoutBinding) :
        BaseViewHolder<Header>(binding.root) {

        override fun bind(item: Header?, onClickListener: ((Header) -> Unit)?) {
            with(binding) {
                searchItemText.text = item?.header
            }
        }
    }

    class SeparatorHolder(binding: MaterialSearchViewSuggestionSeparatorLayoutBinding) :
        BaseViewHolder<String>(binding.root) {
        override fun bind(item: String?, onClickListener: ((String) -> Unit)?) {}
    }

    class LoadingHolder(binding: MaterialSearchViewSuggestionLoadingLayoutBinding) :
        BaseViewHolder<Loading>(binding.root) {
        override fun bind(item: Loading?, onClickListener: ((Loading) -> Unit)?) {}
    }

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T? = null, onClickListener: ((T) -> Unit)? = null)
    }

    class Loading

    class Header(val header: String)

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_SUGGESTION = 2
        private const val TYPE_SEPARATOR = 3
        private const val TYPE_LOADING = 4
        private const val FISRT_INDEX = 0
    }
}