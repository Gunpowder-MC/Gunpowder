package io.github.nyliummc.essentials.ext

fun Double.precision(precision: Int) = "%.${precision}f".format(this)
