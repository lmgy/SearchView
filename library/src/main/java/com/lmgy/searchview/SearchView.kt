package com.lmgy.searchview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*

/**
 * @author lmgy
 * @date 2019/9/19
 */
class SearchView : FrameLayout, Filter.FilterListener {


    private var mMenuItem: MenuItem? = null
    private var mIsSearchOpen = false
    private var mAnimationDuration: Int = 0
    private var mClearingFocus: Boolean = false
    private lateinit var mSearchLayout: View
    private var mTintView: View? = null
    private var mSuggestionsListView: ListView? = null
    private var mSearchSrcTextView: EditText? = null
    private var mBackBtn: ImageButton? = null
    private var mEmptyBtn: ImageButton? = null
    private lateinit var mSearchTopBar: RelativeLayout
    private var mOldQueryText: CharSequence? = null
    private var mUserQuery: CharSequence? = null
    private var mOnQueryChangeListener: OnQueryTextListener? = null
    private var mSearchViewListener: SearchViewListener? = null
    private var mAdapter: ListAdapter? = null
    private lateinit var mSavedState: SavedState
    private var submit = false
    private var ellipsize = false
    private lateinit var suggestionIcon: Drawable
    private var mContext: Context
    private val mOnClickListener = OnClickListener { v ->
        when (v) {
            mBackBtn -> closeSearch()
            mEmptyBtn -> mSearchSrcTextView?.text = null
            mSearchSrcTextView -> showSuggestions()
            mTintView -> closeSearch()
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs
    ) {
        mContext = context
        initiateView()
        initStyle(attrs, defStyleAttr)
    }

    @SuppressLint("PrivateResource")
    private fun initStyle(attrs: AttributeSet?, defStyleAttr: Int) {
        val a =
            mContext.obtainStyledAttributes(attrs, R.styleable.SearchView, defStyleAttr, 0)

        if (a.hasValue(R.styleable.SearchView_searchBackground)) {
            background = a.getDrawable(R.styleable.SearchView_searchBackground)
        }

        if (a.hasValue(R.styleable.SearchView_android_textColor)) {
            setTextColor(a.getColor(R.styleable.SearchView_android_textColor, 0))
        }

        if (a.hasValue(R.styleable.SearchView_android_textColorHint)) {
            setHintTextColor(
                a.getColor(
                    R.styleable.SearchView_android_textColorHint,
                    0
                )
            )
        }

        if (a.hasValue(R.styleable.SearchView_android_hint)) {
            setHint(a.getString(R.styleable.SearchView_android_hint))
        }

        if (a.hasValue(R.styleable.SearchView_searchCloseIcon)) {
            setCloseIcon(a.getDrawable(R.styleable.SearchView_searchCloseIcon))
        }

        if (a.hasValue(R.styleable.SearchView_searchBackIcon)) {
            setBackIcon(a.getDrawable(R.styleable.SearchView_searchBackIcon))
        }

        if (a.hasValue(R.styleable.SearchView_searchSuggestionBackground)) {
            setSuggestionBackground(a.getDrawable(R.styleable.SearchView_searchSuggestionBackground))
        }

        if (a.hasValue(R.styleable.SearchView_searchSuggestionIcon)) {
            setSuggestionIcon(a.getDrawable(R.styleable.SearchView_searchSuggestionIcon))
        }

        if (a.hasValue(R.styleable.SearchView_android_inputType)) {
            setInputType(
                a.getInt(
                    R.styleable.SearchView_android_inputType,
                    EditorInfo.TYPE_NULL
                )
            )
        }

        a.recycle()
    }

    private fun initiateView() {
        LayoutInflater.from(mContext).inflate(R.layout.search_view, this, true)
        mSearchLayout = findViewById(R.id.search_layout)

        mSearchTopBar = mSearchLayout.findViewById(R.id.search_top_bar)
        mSuggestionsListView = mSearchLayout.findViewById(R.id.suggestion_list)
        mSearchSrcTextView = mSearchLayout.findViewById(R.id.searchTextView)
        mBackBtn = mSearchLayout.findViewById(R.id.action_up_btn)
        mEmptyBtn = mSearchLayout.findViewById(R.id.action_empty_btn)
        mTintView = mSearchLayout.findViewById(R.id.transparent_view)

        mSearchSrcTextView?.setOnClickListener(mOnClickListener)
        mBackBtn?.setOnClickListener(mOnClickListener)
        mEmptyBtn?.setOnClickListener(mOnClickListener)
        mTintView?.setOnClickListener(mOnClickListener)

        initSearchView()

        mSuggestionsListView?.visibility = View.GONE
        setAnimationDuration(AnimationUtil.ANIMATION_DURATION_MEDIUM)
    }

    private fun initSearchView() {
        mSearchSrcTextView?.setOnEditorActionListener { _, _, _ ->
            onSubmitQuery()
            true
        }

        mSearchSrcTextView?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mUserQuery = s
                startFilter(s)
                this@SearchView.onTextChanged(s)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        mSearchSrcTextView?.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showKeyboard(mSearchSrcTextView!!)
                    showSuggestions()
                }
            }
    }

    private fun startFilter(s: CharSequence) {
        if (mAdapter is Filterable) {
            (mAdapter as Filterable).filter.filter(s, this@SearchView)
        }
    }

    private fun onTextChanged(newText: CharSequence) {
        val text = mSearchSrcTextView?.text
        mUserQuery = text
        val hasText = !TextUtils.isEmpty(text)
        if (hasText) {
            mEmptyBtn?.visibility = View.VISIBLE
        } else {
            mEmptyBtn?.visibility = View.GONE
        }

        if (!TextUtils.equals(newText, mOldQueryText)) {
            mOnQueryChangeListener?.onQueryTextChange(newText.toString())
        }
        mOldQueryText = newText.toString()
    }

    private fun onSubmitQuery() {
        val query = mSearchSrcTextView?.text
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (mOnQueryChangeListener != null) {
                if (!mOnQueryChangeListener!!.onQueryTextSubmit(query.toString())) {
                    closeSearch()
                    mSearchSrcTextView?.text = null
                }
            }
        }
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(view: View) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1 && view.hasFocus()) {
            view.clearFocus()
        }
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    override fun setBackground(background: Drawable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSearchTopBar.background = background
        } else {
            mSearchTopBar.setBackgroundDrawable(background)
        }
    }

    override fun setBackgroundColor(color: Int) {
        mSearchTopBar.setBackgroundColor(color)
    }

    fun setTextColor(color: Int) {
        mSearchSrcTextView?.setTextColor(color)
    }

    fun setHintTextColor(color: Int) {
        mSearchSrcTextView?.setHintTextColor(color)
    }

    fun setHint(hint: CharSequence?) {
        mSearchSrcTextView?.hint = hint
    }

    fun setCloseIcon(drawable: Drawable?) {
        mEmptyBtn?.setImageDrawable(drawable)
    }

    fun setBackIcon(drawable: Drawable?) {
        mBackBtn?.setImageDrawable(drawable)
    }

    fun setSuggestionIcon(drawable: Drawable?) {
        suggestionIcon = drawable!!
    }

    fun setInputType(inputType: Int) {
        mSearchSrcTextView?.inputType = inputType
    }

    fun setSuggestionBackground(background: Drawable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSuggestionsListView?.background = background
        } else {
            mSuggestionsListView?.setBackgroundDrawable(background)
        }
    }

    fun setCursorDrawable(drawable: Int) {
        try {
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            f.isAccessible = true
            f.set(mSearchSrcTextView, drawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun showSuggestions() {
        if (mAdapter != null && mAdapter!!.count > 0 && mSuggestionsListView?.visibility == View.GONE) {
            mSuggestionsListView?.visibility = View.VISIBLE
        }
    }

    fun setSubmitOnClick(submit: Boolean) {
        this.submit = submit
    }

    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener) {
        mSuggestionsListView?.onItemClickListener = listener
    }

    fun setAdapter(adapter: ListAdapter) {
        mAdapter = adapter
        mSuggestionsListView?.adapter = adapter
        startFilter(mSearchSrcTextView?.text ?: "")
    }

    fun setSuggestions(suggestions: Array<String>?) {
        if (suggestions != null && suggestions.isNotEmpty()) {
            mTintView?.visibility = View.VISIBLE
            val adapter = SearchAdapter(mContext, suggestions, suggestionIcon, ellipsize)
            setAdapter(adapter)

            setOnItemClickListener(AdapterView.OnItemClickListener { _, _, position, _ ->
                setQuery(
                    adapter.getItem(position), submit
                )
            })
        } else {
            mTintView?.visibility = View.GONE
        }
    }

    fun dismissSuggestions() {
        if (mSuggestionsListView?.visibility == View.VISIBLE) {
            mSuggestionsListView?.visibility = View.GONE
        }
    }

    fun setQuery(query: CharSequence?, submit: Boolean) {
        mSearchSrcTextView?.setText(query)
        if (query != null) {
            mSearchSrcTextView?.setSelection(mSearchSrcTextView!!.length())
            mUserQuery = query
        }
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery()
        }
    }

    fun setMenuItem(menuItem: MenuItem?) {
        this.mMenuItem = menuItem
        mMenuItem?.setOnMenuItemClickListener {
            showSearch()
            true
        }
    }

    fun isSearchOpen(): Boolean {
        return mIsSearchOpen
    }

    fun setAnimationDuration(duration: Int) {
        mAnimationDuration = duration
    }

    fun showSearch() {
        showSearch(true)
    }

    fun showSearch(animate: Boolean) {
        if (isSearchOpen()) {
            return
        }

        //Request Focus
        mSearchSrcTextView?.text = null
        mSearchSrcTextView?.requestFocus()

        if (animate) {
            setVisibleWithAnimation()

        } else {
            mSearchLayout.visibility = View.VISIBLE
            mSearchViewListener?.onSearchViewShown()
        }
        mIsSearchOpen = true
    }

    private fun setVisibleWithAnimation() {
        val animationListener = object : AnimationUtil.AnimationListener {
            override fun onAnimationStart(view: View): Boolean {
                return false
            }

            override fun onAnimationEnd(view: View): Boolean {
                mSearchViewListener?.onSearchViewShown()
                return false
            }

            override fun onAnimationCancel(view: View): Boolean {
                return false
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSearchLayout.visibility = View.VISIBLE
            AnimationUtil.reveal(mSearchTopBar, animationListener)

        } else {
            AnimationUtil.fadeInView(mSearchLayout, mAnimationDuration, animationListener)
        }
    }

    /**
     * Close search view.
     */
    fun closeSearch() {
        if (!isSearchOpen()) {
            return
        }

        mSearchSrcTextView?.text = null
        dismissSuggestions()
        clearFocus()

        mSearchLayout.visibility = View.GONE
        mSearchViewListener?.onSearchViewClosed()

        mIsSearchOpen = false

    }

    /**
     * Set this listener to listen to Query Change events.
     *
     * @param listener
     */
    fun setOnQueryTextListener(listener: OnQueryTextListener) {
        mOnQueryChangeListener = listener
    }

    /**
     * Set this listener to listen to Search View open and close events
     *
     * @param listener
     */
    fun setOnSearchViewListener(listener: SearchViewListener) {
        mSearchViewListener = listener
    }

    /**
     * Ellipsize suggestions longer than one line.
     *
     * @param ellipsize
     */
    fun setEllipsize(ellipsize: Boolean) {
        this.ellipsize = ellipsize
    }

    override fun onFilterComplete(count: Int) {
        if (count > 0) {
            showSuggestions()
        } else {
            dismissSuggestions()
        }
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        // Don't accept focus if in the middle of clearing focus
        if (mClearingFocus) return false
        // Check if SearchView is focusable.
        return if (!isFocusable) false else mSearchSrcTextView!!.requestFocus(
            direction,
            previouslyFocusedRect
        )
    }

    override fun clearFocus() {
        mClearingFocus = true
        hideKeyboard(this)
        super.clearFocus()
        mSearchSrcTextView?.clearFocus()
        mClearingFocus = false
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        mSavedState = SavedState(superState)
        mSavedState.query = if (mUserQuery != null) mUserQuery.toString() else null
        mSavedState.isSearchOpen = this.mIsSearchOpen
        return mSavedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        mSavedState = state

        if (mSavedState.isSearchOpen) {
            showSearch(false)
            setQuery(mSavedState.query, false)
        }

        super.onRestoreInstanceState(mSavedState.getSuperState())
    }

    internal class SavedState : BaseSavedState {
        internal var query: String? = null
        var isSearchOpen: Boolean = false

        constructor(superState: Parcelable?) : super(superState)


        private constructor(`in`: Parcel) : super(`in`) {
            this.query = `in`.readString()
            this.isSearchOpen = `in`.readInt() == 1
        }


        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(query)
            out.writeInt(if (isSearchOpen) 1 else 0)
        }
    }

    interface OnQueryTextListener {

        /**
         * Called when the user submits the query. This could be due to a key press on the
         * keyboard or due to pressing a submit button.
         * The listener can override the standard behavior by returning true
         * to indicate that it has handled the submit request. Otherwise return false to
         * let the SearchView handle the submission by launching any associated intent.
         *
         * @param query the query text that is to be submitted
         * @return true if the query has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
        fun onQueryTextSubmit(query: String): Boolean

        /**
         * Called when the query text is changed by the user.
         *
         * @param newText the new content of the query text field.
         * @return false if the SearchView should perform the default action of showing any
         * suggestions if available, true if the action was handled by the listener.
         */
        fun onQueryTextChange(newText: String): Boolean
    }

    interface SearchViewListener {
        fun onSearchViewShown()

        fun onSearchViewClosed()
    }

}