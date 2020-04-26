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
