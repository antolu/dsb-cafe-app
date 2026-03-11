package com.lua.dsbcafe.data.model
import androidx.annotation.Keep

@Keep
data class Person(val name: String = "", var coffeeCount: Int = 0, val badgeId: String = "")
