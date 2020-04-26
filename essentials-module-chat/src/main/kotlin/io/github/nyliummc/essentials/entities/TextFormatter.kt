/*
 * MIT License
 *
 * Copyright (c) NyliumMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.nyliummc.essentials.entities

object TextFormatter {
    fun formatString(text: String): String {
        var output = ""
        val textArray = text.toCharArray()
        val markers = IntArray(text.replace("[^&]+".toRegex(), "").length + 2)
        var j = 1
        for (i in textArray.indices) {
            if (textArray[i] == '&') {
                if ((i == 0 || textArray[i - 1] != '\\') && textArray.size > i + 1) {
                    markers[j] = i
                }
                j++
            }
        }
        markers[0] = 0
        markers[markers.size - 1] = text.length
        for (i in 0 until markers.size - 1) {
            output += getFormattedPart(text, markers[i], markers[i + 1])
        }
        return output
    }

    private fun getFormattedPart(text: String, firstIndex: Int, lastIndex: Int): String {
        val outputString = text.substring(firstIndex, lastIndex)
        return setParagraphs(outputString)
    }

    private fun setParagraphs(string: String): String {
        val builder = StringBuilder(string)
        for (i in builder.indices) {
            if (builder[i] == '&') {
                if (i == 0 || builder[i - 1] != '\\') {
                    if (i != builder.length - 1 && matches(builder[i + 1])) {
                        builder.setCharAt(i, '§')
                    }
                }
            }
        }
        return builder.toString()
    }

    private fun matches(c: Char): Boolean {
        return "b0931825467adcfeklmnor".contains(c.toString())
    }
}
