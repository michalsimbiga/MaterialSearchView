package com.example.materialsearchbar

import android.annotation.SuppressLint
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
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.res.use
import kotlinx.android.synthetic.main.material_search_view.view.*

typealias onTextChangedCallback = (String) -> Unit
typealias onViewFocusedCallback = (Boolean) -> Unit
typealias onVoiceIconCallback = () -> Unit

class MaterialSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttrs, defStyleRes) {

    private var onTextChangedCallback: onTextChangedCallback? = null
    private var onViewFocusedCallback: onViewFocusedCallback? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.material_search_view, this)

        if (attrs != null)
            initializeCustomAttrs(context, attrs)

        setupListeners()
    }

    @SuppressLint("Recycle")
    private fun initializeCustomAttrs(context: Context, attrs: AttributeSet) {
        if (!isInEditMode) {
            context.obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, 0, 0).use {
                msw_edit_text.hint = it.getText(R.styleable.MaterialSearchView_hint)
                msw_search_icon.setImageDrawable(it.getDrawable(R.styleable.MaterialSearchView_search_icon))
                msw_back_icon.setImageDrawable(it.getDrawable(R.styleable.MaterialSearchView_back_icon))
                msw_voice.setImageDrawable(it.getDrawable(R.styleable.MaterialSearchView_voice_icon))
                msw_close.setImageDrawable(it.getDrawable(R.styleable.MaterialSearchView_clear_icon))
                msw_card_content_wrapper.background =
                    it.getDrawable(R.styleable.MaterialSearchView_card_content_background)
            }
        }
    }

    private fun setupListeners() {
        setupButtonListeners()
        setupTextListeners()
    }

    /*
    *   Callback Setup Section
    */

    fun setOnTextChangedCallbacks(newOnTextChangedCallback: onTextChangedCallback) {
        onTextChangedCallback = newOnTextChangedCallback
    }

    fun setOnViewFocusesCallback(newOnViewFocusedCallback: onViewFocusedCallback) {
        onViewFocusedCallback = newOnViewFocusedCallback
    }

    fun setOnVoiceIconCallback(newOnVoiceIconCallback: onVoiceIconCallback) {
        msw_voice.setOnClickListener { newOnVoiceIconCallback() }
    }

    /*
    *   Listeners Setup Section
    */

    private fun setupButtonListeners() {
        setupCloseButtonListener()
        searchIconClearsFocus()
        setupContainerButtonListener()
    }

    private fun setupContainerButtonListener() =
        msw_container.setOnClickListener { clearFocusFromView() }

    private fun setupCloseButtonListener() =
        msw_close.setOnClickListener {
            msw_edit_text.setText("")
            clearFocusFromView()
        }

    private fun searchIconClearsFocus() = msw_back_icon.setOnClickListener { clearFocusFromView() }

    /*
    *  Text Setup section
    */

    private fun setupTextListeners() {
        setupEditTextFocus()
        setTextWatcher()
    }

    private fun setupEditTextFocus() = msw_edit_text.setOnFocusChangeListener { _, hasFocus ->
        onLayoutFocus(hasFocus)
    }

    private fun setTextWatcher() {
        msw_edit_text.addTextChangedListener(onTextChangedTextWatcher { newText ->
            hideClearIconOnTextChanged(newText.isNotEmpty())
            swapEndButtonsOnTextInput(newText.isNotEmpty())
            onTextChangedCallback?.invoke(newText.toString())
        })
    }

    /*
    *   Suggestions Adapter Section
    */
    fun setSuggestionsAdapter(recyclerAdapterInterface: RecyclerAdapterAbstract) {
        msw_suggestions_recycler.adapter = recyclerAdapterInterface
    }

    /*
    *   Layout Manipulations Section
    */

    private fun showExtendedLayout(show: Boolean) {
        msw_suggestions_recycler.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun swapEndButtonsOnTextInput(notEmpty: Boolean) {
        msw_close.visibleElseGone(notEmpty)
        msw_voice.visibleElseGone(!notEmpty)
    }

    private fun swapStartIconsOnFocus(focused: Boolean) {
        msw_back_icon.visibleElseGone(focused)
        msw_search_icon.visibleElseGone(!focused)
    }

    private fun hideClearIconOnTextChanged(visible: Boolean) {
        msw_close.visibleElseGone(visible)
    }

    private fun onLayoutFocus(hasFocus: Boolean) =
        with(hasFocus) {
            swapStartIconsOnFocus(this)
            extendViewForSuggestions(this)
            showExtendedLayout(this)
            onViewFocusedCallback?.invoke(this)
            requestLayout()
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

    /*
    *   Voice Section
    */

    fun handleVoiceData(data: Intent) {
        val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!matches.isNullOrEmpty()) {
            val searchWord = matches.first()
            if (!TextUtils.isEmpty(searchWord)) {
                with(msw_edit_text) {
                    setText(searchWord)
                    setSelection(msw_edit_text.text.lastIndex + PROGRAMMER_COUNT_OFFSET)
                    requestFocus()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        onTextChangedCallback = null
        onViewFocusedCallback = null
        msw_voice.setOnClickListener(null)
    }

    companion object {
        private const val PROGRAMMER_COUNT_OFFSET = 1
    }
}