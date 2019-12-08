package com.example.materialsearchbar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.app.ActivityCompat.startActivityForResult
import kotlinx.android.synthetic.main.material_search_view.view.*

typealias onTextChangedCallback = (String) -> Unit

class MaterialSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttrs, defStyleRes) {

    private var editText: EditText
    private var backButton: ImageButton
    private var voiceButton: ImageButton
    private var clearButton: ImageButton

    private var iconBack: Drawable? = null
    private var iconSearch: Drawable? = null

    private var onTextChangedCallback: onTextChangedCallback? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.material_search_view, this)

        editText = findViewById(R.id.msw_edit_text)
        backButton = findViewById(R.id.msw_search_icon)
        voiceButton = findViewById(R.id.msw_voice)
        clearButton = findViewById(R.id.msw_close)

        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.MaterialSearchView, 0, 0)

            iconSearch = typedArray.getDrawable(R.styleable.MaterialSearchView_search_icon)
            iconBack = typedArray.getDrawable(R.styleable.MaterialSearchView_back_icon)
            val hint = typedArray.getText(R.styleable.MaterialSearchView_hint)
            val voiceIcon = typedArray.getDrawable(R.styleable.MaterialSearchView_voice_icon)
            val clearIcon = typedArray.getDrawable(R.styleable.MaterialSearchView_clear_icon)

            editText.hint = hint
            backButton.setImageDrawable(iconSearch)
            voiceButton.setImageDrawable(voiceIcon)
            clearButton.setImageDrawable(clearIcon)

            typedArray.recycle()
        }

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        setupCloseButtonListener()
        setupVoiceButtonListener()
        searchIconClearsFocus()
        setupContainerButtonListener()
        setupEditTextFocus()
        setTextWatcher()
    }

    private fun setupContainerButtonListener() {
        msw_container.setOnClickListener { clearFocusFromView() }
    }

    fun setSuggestionsAdapter(suggestionsRecyclerViewAdapter: SuggestionsRecyclerViewAdapter) {
        msw_suggestions_recycler.adapter = suggestionsRecyclerViewAdapter
    }

    private fun setupVoiceButtonListener() {
        voiceButton.setOnClickListener { onVoiceSearchClicked() }
    }

    private fun setupCloseButtonListener() {
        clearButton.setOnClickListener {
            editText.setText("")
            clearFocusFromView()
        }
    }

    private fun swapEndButtonsOnTextInput(notEmpty: Boolean){
        clearButton.visibility = if (notEmpty) View.VISIBLE else View.GONE
        voiceButton.visibility = if (!notEmpty) View.VISIBLE else View.GONE
    }

    private fun searchIconClearsFocus() {
        backButton.setOnClickListener {
            clearFocusFromView()
        }
    }

    fun setOnTextChangedCallbacks(newOnTextChangedCallback: (String) -> Unit) {
        onTextChangedCallback = newOnTextChangedCallback
    }

    private fun setupEditTextFocus() {
        editText.setOnFocusChangeListener { _, hasFocus ->
            onLayoutFocus(hasFocus)
        }
    }

    private fun onLayoutFocus(hasFocus: Boolean) {
        swapSearchIconOnTextFocus(hasFocus)
        extendViewForSuggestions(hasFocus)
        showExtendedLayout(hasFocus)
    }

    private fun extendViewForSuggestions(extend: Boolean) =
        with(msw_container) {
            setBackgroundColor(if (extend) Color.WHITE else Color.TRANSPARENT)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                if (extend) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT
            )
        }

    private fun clearFocusFromView() {
        clearFocus()
        hideKeyboard()
    }

    private fun setTextWatcher() {
        editText.addTextChangedListener(onTextChangedListener { newText ->
            hideClearIconOnTextChanged(newText.isNotEmpty())
            swapEndButtonsOnTextInput(newText.isNotEmpty())
            onTextChangedCallback?.invoke(newText.toString())
        })
    }

    private fun onVoiceSearchClicked() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak an item name"
        )    // user hint
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        )    // setting recognition model, optimized for short phrases – search queries
        intent.putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            1
        )

        startActivityForResult(context as Activity, intent, VOICE_RECOGNITION_CODE, null)
    }

    private fun hideClearIconOnTextChanged(visible: Boolean) {
        clearButton.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showExtendedLayout(show: Boolean) {
        msw_suggestions_recycler.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun swapSearchIconOnTextFocus(focused: Boolean) =
        backButton.setImageDrawable(if (focused) iconBack else iconSearch)


    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun handleVoiceData(data: Intent) {
        val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!matches.isNullOrEmpty()) {
            val searchWord = matches.first()
            if (!TextUtils.isEmpty(searchWord)) {
                with(editText) {
                    setText(searchWord)
                    setSelection(editText.text.lastIndex + PROGRAMMER_COUNT_OFFSET)
                    requestFocus()
                }
            }
        }
    }

    companion object {
        private const val PROGRAMMER_COUNT_OFFSET = 1
        const val VOICE_RECOGNITION_CODE = 312
    }
}