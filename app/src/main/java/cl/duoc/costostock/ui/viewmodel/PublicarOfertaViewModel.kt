package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.OfertaRepository
import cl.duoc.costostock.data.room.OfertaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Todo lo que el usuario escribio en el formulario, mas el error de
 * cada campo.
 *
 * Tener el estado completo en una sola data class permite copiarlo con
 * copy() y que Compose redibuje solo lo que cambio.
 */
data class FormularioOferta(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Abarrotes",
    val precioOriginal: String = "",
    val precioOferta: String = "",
    val stock: String = "",
    val horasVigencia: String = "4",
    val comercio: String = "",
    val direccion: String = "",
    val comuna: String = "Vina del Mar",
    val pesoKg: String = "",

    // null = campo correcto
    val errorTitulo: String? = null,
    val errorDescripcion: String? = null,
    val errorPrecioOriginal: String? = null,
    val errorPrecioOferta: String? = null,
    val errorStock: String? = null,
    val errorHoras: String? = null,
    val errorComercio: String? = null,
    val errorDireccion: String? = null,
    val errorPeso: String? = null
) {
    private val errores: List<String?>
        get() = listOf(
            errorTitulo, errorDescripcion, errorPrecioOriginal,
            errorPrecioOferta, errorStock, errorHoras,
            errorComercio, errorDireccion, errorPeso
        )

    /** true cuando ningun campo tiene error. */
    val esValido: Boolean get() = errores.all { it == null }

    /** Cuantos campos quedan por corregir. */
    val cantidadErrores: Int get() = errores.count { it != null }
}

/**
 * PR-03 Publicar oferta.
 *
 * Toda la validacion vive aqui, desacoplada de la interfaz: la pantalla
 * solo dibuja los errores que este ViewModel calcula. Eso permite probar
 * las reglas sin levantar la UI y cumple con el patron MVVM.
 */
class PublicarOfertaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = OfertaRepository(application)

    private val _formulario = MutableStateFlow(FormularioOferta())
    val formulario: StateFlow<FormularioOferta> = _formulario

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    /** Pasa a true cuando la oferta quedo guardada en Room. */
    private val _publicada = MutableStateFlow(false)
    val publicada: StateFlow<Boolean> = _publicada

    /**
     * Mensaje general del formulario, para lo que no corresponde a un
     * campo en particular: campos pendientes o producto duplicado.
     */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    /** La pantalla la llama despues de mostrar el mensaje. */
    fun limpiarMensaje() {
        _mensaje.value = null
    }

    // --- Cambios de cada campo. Validan a medida que se escribe ---

    fun cambiarTitulo(valor: String) {
        _formulario.value = _formulario.value.copy(
            titulo = valor,
            errorTitulo = validarTexto(valor, "El titulo", minimo = 5)
        )
    }

    fun cambiarDescripcion(valor: String) {
        _formulario.value = _formulario.value.copy(
            descripcion = valor,
            errorDescripcion = validarTexto(valor, "La descripcion", minimo = 10)
        )
    }

    fun cambiarCategoria(valor: String) {
        _formulario.value = _formulario.value.copy(categoria = valor)
    }

    fun cambiarComuna(valor: String) {
        _formulario.value = _formulario.value.copy(comuna = valor)
    }

    fun cambiarPrecioOriginal(valor: String) {
        val limpio = valor.filter { it.isDigit() }
        val actual = _formulario.value.copy(precioOriginal = limpio)

        _formulario.value = actual.copy(
            errorPrecioOriginal = validarPrecio(limpio, "El precio original"),
            // Al cambiar el original hay que revalidar el de oferta,
            // porque la regla los compara entre si
            errorPrecioOferta = validarPrecioOferta(actual.precioOferta, limpio)
        )
    }

    fun cambiarPrecioOferta(valor: String) {
        val limpio = valor.filter { it.isDigit() }
        _formulario.value = _formulario.value.copy(
            precioOferta = limpio,
            errorPrecioOferta = validarPrecioOferta(
                limpio,
                _formulario.value.precioOriginal
            )
        )
    }

    fun cambiarStock(valor: String) {
        val limpio = valor.filter { it.isDigit() }
        _formulario.value = _formulario.value.copy(
            stock = limpio,
            errorStock = validarEntero(limpio, "El stock", minimo = 1, maximo = 999)
        )
    }

    fun cambiarHoras(valor: String) {
        val limpio = valor.filter { it.isDigit() }
        _formulario.value = _formulario.value.copy(
            horasVigencia = limpio,
            errorHoras = validarEntero(limpio, "La duracion", minimo = 1, maximo = 72)
        )
    }

    fun cambiarComercio(valor: String) {
        _formulario.value = _formulario.value.copy(
            comercio = valor,
            errorComercio = validarTexto(valor, "El comercio", minimo = 3)
        )
    }

    fun cambiarDireccion(valor: String) {
        _formulario.value = _formulario.value.copy(
            direccion = valor,
            errorDireccion = validarTexto(valor, "La direccion", minimo = 5)
        )
    }

    fun cambiarPeso(valor: String) {
        val limpio = valor.filter { it.isDigit() || it == '.' }
        _formulario.value = _formulario.value.copy(
            pesoKg = limpio,
            errorPeso = validarPeso(limpio)
        )
    }

    // --- Reglas de validacion ---

    /** @param campo nombre con su articulo, para que concuerde el genero */
    private fun validarTexto(valor: String, campo: String, minimo: Int): String? = when {
        valor.isBlank() -> "$campo es un campo obligatorio"
        valor.trim().length < minimo -> "$campo debe tener al menos $minimo caracteres"
        else -> null
    }

    private fun validarPrecio(valor: String, campo: String): String? {
        val numero = valor.toIntOrNull()
        return when {
            valor.isBlank() -> "$campo es un campo obligatorio"
            numero == null -> "Ingresa solo numeros"
            numero <= 0 -> "$campo debe ser mayor a cero"
            numero > 9_999_999 -> "$campo es demasiado alto"
            else -> null
        }
    }

    /**
     * Regla de negocio: el precio de liquidacion tiene que ser menor
     * al original. Si no, no es una oferta.
     */
    private fun validarPrecioOferta(valor: String, original: String): String? {
        val base = validarPrecio(valor, "El precio de oferta")
        if (base != null) return base

        val oferta = valor.toIntOrNull() ?: return null
        val precioOriginal = original.toIntOrNull() ?: return null

        return when {
            oferta >= precioOriginal ->
                "Debe ser menor al precio original"
            else -> null
        }
    }

    private fun validarEntero(
        valor: String,
        campo: String,
        minimo: Int,
        maximo: Int
    ): String? {
        val numero = valor.toIntOrNull()
        return when {
            valor.isBlank() -> "$campo es un campo obligatorio"
            numero == null -> "Ingresa solo numeros"
            numero < minimo -> "$campo debe ser al menos $minimo"
            numero > maximo -> "$campo no puede superar $maximo"
            else -> null
        }
    }

    private fun validarPeso(valor: String): String? {
        val numero = valor.toDoubleOrNull()
        return when {
            valor.isBlank() -> "El peso es obligatorio para calcular el CO2"
            numero == null -> "Ingresa un numero valido"
            numero <= 0 -> "El peso debe ser mayor a cero"
            numero > 100 -> "El peso no puede superar 100 kg"
            else -> null
        }
    }

    /**
     * Revisa todos los campos de una vez. Se llama al tocar Publicar,
     * porque un campo que nunca se toco no tiene error registrado.
     */
    private fun validarTodo(): FormularioOferta {
        val f = _formulario.value
        return f.copy(
            errorTitulo = validarTexto(f.titulo, "El titulo", 5),
            errorDescripcion = validarTexto(f.descripcion, "La descripcion", 10),
            errorPrecioOriginal = validarPrecio(f.precioOriginal, "El precio original"),
            errorPrecioOferta = validarPrecioOferta(f.precioOferta, f.precioOriginal),
            errorStock = validarEntero(f.stock, "El stock", 1, 999),
            errorHoras = validarEntero(f.horasVigencia, "La duracion", 1, 72),
            errorComercio = validarTexto(f.comercio, "El comercio", 3),
            errorDireccion = validarTexto(f.direccion, "La direccion", 5),
            errorPeso = validarPeso(f.pesoKg)
        )
    }

    /** Guarda la oferta en Room si el formulario es valido. */
    fun publicar() {
        val validado = validarTodo()
        _formulario.value = validado

        // Si hay errores, avisamos cuantos: los campos con problema
        // pueden estar fuera de la pantalla y el usuario no los ve
        if (!validado.esValido) {
            val pendientes = validado.cantidadErrores
            _mensaje.value =
                if (pendientes == 1) "Falta corregir 1 campo"
                else "Faltan corregir $pendientes campos"
            return
        }

        viewModelScope.launch {
            _guardando.value = true

            // Regla de negocio: no publicar dos veces el mismo producto
            // en el mismo comercio mientras la primera siga vigente
            if (repositorio.existeDuplicada(validado.titulo, validado.comercio)) {
                _guardando.value = false
                _mensaje.value =
                    "Ya tienes una oferta vigente de este producto en ese comercio"
                _formulario.value = validado.copy(
                    errorTitulo = "Este producto ya esta publicado"
                )
                return@launch
            }

            val peso = validado.pesoKg.toDoubleOrNull() ?: 0.0
            val horas = validado.horasVigencia.toIntOrNull() ?: 4
            val coordenadas = COORDENADAS[validado.comuna] ?: (-33.0245 to -71.5518)

            val oferta = OfertaEntity(
                titulo = validado.titulo.trim(),
                descripcion = validado.descripcion.trim(),
                categoria = validado.categoria,
                precioOriginal = validado.precioOriginal.toInt(),
                precioOferta = validado.precioOferta.toInt(),
                stockActual = validado.stock.toInt(),
                finVigencia = System.currentTimeMillis() + horas * 3_600_000L,
                comercio = validado.comercio.trim(),
                direccion = validado.direccion.trim(),
                comuna = validado.comuna,
                latitud = coordenadas.first,
                longitud = coordenadas.second,
                co2Evitado = calcularCo2(validado.categoria, peso),
                imagenUrl = ""
            )

            repositorio.publicar(oferta)

            _guardando.value = false
            _publicada.value = true
        }
    }

    /**
     * CO2 evitado = peso en kg x factor de emision de la categoria.
     *
     * Los factores son referenciales para el prototipo academico. En
     * produccion saldrian de una tabla en el backend con fuente citada.
     */
    private fun calcularCo2(categoria: String, pesoKg: Double): Double {
        val factor = FACTORES_CO2[categoria] ?: 1.0
        return (pesoKg * factor * 10).toInt() / 10.0
    }

    /** Vista previa del CO2 mientras el usuario escribe el peso. */
    fun co2Estimado(): Double {
        val f = _formulario.value
        val peso = f.pesoKg.toDoubleOrNull() ?: return 0.0
        return calcularCo2(f.categoria, peso)
    }

    companion object {
        /** Kilos de CO2 evitados por cada kilo de producto rescatado. */
        private val FACTORES_CO2 = mapOf(
            "Abarrotes" to 1.1,
            "Refrigerados" to 2.5,
            "Bebidas" to 0.8,
            "Licores" to 1.3,
            "Hogar" to 0.6
        )

        /** Comunas de la V Region donde opera Costo Stock. */
        val COMUNAS = listOf(
            "Vina del Mar", "Valparaiso", "Quilpue",
            "Villa Alemana", "Quillota", "Concon", "Casablanca"
        )

        private val COORDENADAS = mapOf(
            "Vina del Mar" to (-33.0245 to -71.5518),
            "Valparaiso" to (-33.0472 to -71.6127),
            "Quilpue" to (-33.0472 to -71.4420),
            "Villa Alemana" to (-33.0423 to -71.3733),
            "Quillota" to (-32.8805 to -71.2489),
            "Concon" to (-32.9220 to -71.5189),
            "Casablanca" to (-33.3194 to -71.4103)
        )

        val CATEGORIAS = listOf(
            "Abarrotes", "Refrigerados", "Bebidas", "Licores", "Hogar"
        )
    }
}
