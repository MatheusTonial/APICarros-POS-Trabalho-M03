package com.tonial.apicarros.ui

import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.tonial.apicarros.R

fun ImageView.loadUrl(url: String) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_download)
        .error(R.drawable.ic_error)
        .transform(CircleTransform())
        .into(this)
}