package com.skycore.foodplace.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.skycore.foodplace.databinding.DialogNoInternetBinding

class NoInternet(context: Context, themeResId: Int) : Dialog(context, themeResId) {
    private lateinit var title: String
    private lateinit var message: String
    private var onCloseClick: View.OnClickListener? = null //get click event action
    private lateinit var binding: DialogNoInternetBinding

    constructor(context: Context, themeResId: Int, title: String, message: String) : this(
        context, themeResId
    ) {
        this.title = title
        this.message = message
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogNoInternetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val txtTitle = binding.tvTitle
        val txtMessage = binding.tvMessage
        val imgClose = binding.imgClose

        imgClose.setOnClickListener {
            onCloseClick?.onClick(it)
        }

        txtTitle.text = title
        txtMessage.text = message
    }

    /**
     * Trigger click event and notify
     */
    fun onCloseClicked(onClickListener: View.OnClickListener) {
        this.onCloseClick = onClickListener
    }
}