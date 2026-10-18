package cl.duoc.costostock.utils

private val REGEX_RUT = Regex("^\\d{7,8}[\\dK]$")

fun validarRUT(rut : String) : Boolean{
    val limpio = rut.replace(Regex("[.\\-\\s]"),"").uppercase()
    //En caso que exista una K de por medio.
    if (!REGEX_RUT.matches(limpio)) return false
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

// VALIDANDO TELÉFONO

private val REGEX_TELEFONO_CHILE = Regex("^9\\d{8}$")

fun validarTelefono(telefono: String) : String? {
    val limpio = telefono.trim().replace(Regex("[\\s\\-]"), "")
        .removePrefix("+56") //Le quitamos el +56 para validar que todo sea número.
    return if (REGEX_TELEFONO_CHILE.matches(limpio)) "+56$limpio" else null
    // Dejo el telefono en limpio, hago la limpieza con eso.
}