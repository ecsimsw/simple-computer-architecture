package computer.architecture.utils

fun Int.toBinaryString(digits: Int): String {
    val binaryString = Integer.toBinaryString(this)
    println(binaryString)
    return binaryString.fillDigitsWith(digits, 0)
}

fun String.fillDigitsWith(digits: Int, num: Int): String {
    var newBinary = this
    if (newBinary.length < digits) {
        for (i in 0 until digits - this.length) {
            newBinary = num.toString() + newBinary
        }
    }
    return newBinary
}

fun String.readAsBinary(): Int {
    return Integer.parseInt(this, 2)
}

fun Byte.toBinary(digits: Int): String {
    return Integer.parseInt(String.format("%02X", this), 16).toBinaryString(digits)
}

class IntegerUtils {

}