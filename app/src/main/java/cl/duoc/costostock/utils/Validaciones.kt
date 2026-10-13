package cl.duoc.costostock.utils

private val REGEX_RUT = Regex("^\\d{7,8}[\\dK]$")

fun validarRUT(rut : String) : Boolean{
    val limpio = rut.replace(Regex("[.\\-\\s]"),"").uppercase()
    //En caso que exista una K de por medio.

    if (REGEX_RUT.matches(limpio)) return false

    val cuerpo = limpio.dropLast(1)
    val dv = limpio.last()

    var suma = 0
    var factor = 2

    for (i in cuerpo.reversed()) {
        suma += i.digitToInt() * factor
        factor = if (factor == 7) 2 else factor + 1
    }

    val res = 11 - (suma % 11)
    val esperado = when(res) {
        11 -> '0'
        10 -> 'K'
        else -> res.digitToChar()
    }
    return dv == esperado


}