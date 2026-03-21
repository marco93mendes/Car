package com.example.myapitest.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val id: String,
    val value: ItemValue
) : Parcelable

@Parcelize
data class ItemValue(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: ItemLocation
) : Parcelable

@Parcelize
data class ItemLocation(
    val lat: Double,
    val long: Double
) : Parcelable
