package com.iconflux.brokingbulls.myUtils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView

class CustomTextWatcherWithTVErr(private val editText: EditText, private val tvErr: TextView) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        tvErr.visibility = if (s.toString().trim { it <= ' ' } != "" && MyUtils.getEDTText(editText) == "" || s.toString().trim { it <= ' ' } == "" && MyUtils.getEDTText(editText) != "") View.VISIBLE else View.GONE
    }

    override fun afterTextChanged(s: Editable) {}

}