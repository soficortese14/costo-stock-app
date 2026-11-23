package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.AlmacenImagenes
import cl.duoc.costostock.data.OfertaRepository
import cl.duoc.costostock.data.ProductoExterno
import cl.duoc.costostock.data.ProductoExternoRepository
import cl.duoc.costostock.data.ResultadoBusqueda
import cl.duoc.costostock.domain.CalculadoraCO2
import cl.duoc.costostock.domain.Configuracion
import cl.duoc.costostock.domain.FactoresCO2
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

    /** Ruta de la foto ya copiada a la app. Vacio significa sin foto. */
    val imagenUrl: String = "",

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

    /**
     * La calculadora de CO2, con los factores que esten vigentes.
     *
     * Antes la formula y los factores estaban escritos aca adentro, lo
     * que tenia dos problemas: no se podia probar sin emulador y nadie
     * mas podia usarlos. Ahora viven en domain/ y los factores vienen
     * del archivo de datos.
     */
    private val calculadora = CalculadoraCO2()

    private val productosExternos = ProductoExternoRepository()

    /** Codigo de barras que el proveedor escribe para buscar. */
    private val _codigoBarras = MutableStateFlow("")
    val codigoBarras: StateFlow<String> = _codigoBarras

    /**
     * En que va la consulta a la API externa.
     *
     * null mientras nadie ha buscado: la tarjeta de resultado no se
     * dibuja hasta que haya algo que decir.
     */
    private val _resultadoApi = MutableStateFlow<ResultadoBusqueda?>(null)
    val resultadoApi: StateFlow<ResultadoBusqueda?> = _resultadoApi

    private val _buscandoEnApi = MutableStateFlow(false)
    val buscandoEnApi: StateFlow<Boolean> = _buscandoEnApi

    fun cambiarCodigoBarras(valor: String) {
        // Solo digitos: un codigo de barras no lleva letras ni guiones
        _codigoBarras.value = valor.filter { it.isDigit() }.take(14)
        if (_resultadoApi.value != null) _resultadoApi.value = null
    }

    /**
     * Consulta el producto en Open Food Facts.
     *
     * No rellena el formulario por su cuenta: muestra lo que encontro y
     * espera a que el proveedor lo acepte. Es a proposito -- los datos
     * de la API son colaborativos y pueden estar mal, y pisar en
     * silencio lo que alguien escribio seria peor que no ofrecer nada.
     */
    fun buscarEnApi() {
        val codigo = _codigoBarras.value
        if (codigo.isBlank()) return

        viewModelScope.launch {
            _buscandoEnApi.value = true
            _resultadoApi.value = productosExternos.buscarPorCodigo(codigo)
            _buscandoEnApi.value = false
        }
    }

    /**
     * Vuelca el producto de la API en el formulario.
     *
     * Solo completa los campos que estan vacios y los que la API trajo
     * con dato. Lo que el proveedor ya escribio se respeta: el manda,
     * la API solo sugiere.
     *
     * El peso es el campo que mas importa de los tres, porque de el
     * sale el CO2 evitado. Hasta ahora ese numero lo escribia el
     * proveedor a mano.
     */
    fun usarProductoDeApi(producto: ProductoExterno) {
        val actual = _formulario.value

        _formulario.value = actual.copy(
            titulo = if (actual.titulo.isBlank()) producto.tituloSugerido
            else actual.titulo,
            pesoKg = producto.pesoKg?.toString() ?: actual.pesoKg,
            imagenUrl = producto.imagenUrl ?: actual.imagenUrl,
            errorTitulo = null,
            errorPeso = null
        )

        _mensaje.value = "Datos cargados desde Open Food Facts"
        _resultadoApi.value = null
        _codigoBarras.value = ""
    }

    /** Descarta el resultado sin usarlo. */
    fun descartarResultadoApi() {
        _resultadoApi.value = null
    }

    private val _formulario = MutableStateFlow(FormularioOferta())
    val formulario: StateFlow<FormularioOferta> = _formulario

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    /** Pasa a true cuando la oferta quedo guardada en Room. */
    private val _publicada = MutableStateFlow(false)
    val publicada: StateFlow<Boolean> = _publicada

    /**
     * Id de la oferta que se esta editando, o null si se esta publicando
     * una nueva. Es lo unico que distingue los dos modos: el formulario,
     * las validaciones y la pantalla son exactamente los mismos.
     */
    private val _editandoId = MutableStateFlow<Long?>(null)
    val editandoId: StateFlow<Long?> = _editandoId

    /** Se marca cuando se borro la oferta, para que la pantalla salga. */
    private val _eliminada = MutableStateFlow(false)
    val eliminada: StateFlow<Boolean> = _eliminada

    /**
     * Mensaje general del formulario, para lo que no corresponde a un
     * campo en particular: campos pendientes o producto duplicado.
     */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    /** La pantalla la llama despues de mostrar el mensaje. */
    /** Deja un aviso para que la pantalla lo muestre en el snackbar. */
    fun avisar(texto: String) {
        _mensaje.value = texto
    }

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

    /**
     * Guarda la foto recien tomada o elegida.
     *
     * Si ya habia una, se borra: el usuario la reemplazo y dejarla
     * ocuparia espacio sin que nadie pueda volver a verla.
     */
    fun cambiarImagen(nuevaRuta: String) {
        val anterior = _formulario.value.imagenUrl

        if (anterior.isNotBlank() && anterior != nuevaRuta) {
            AlmacenImagenes.borrar(anterior)
        }

        _formulario.value = _formulario.value.copy(imagenUrl = nuevaRuta)
    }

    /** Quita la foto de la oferta. */
    fun quitarImagen() {
        val actual = _formulario.value.imagenUrl
        if (actual.isNotBlank()) AlmacenImagenes.borrar(actual)

        _formulario.value = _formulario.value.copy(imagenUrl = "")
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

    /**
     * Trae una oferta de Room y la vuelca en el formulario.
     *
     * El camino de vuelta no es simetrico: la entidad guarda el CO2 ya
     * calculado y el instante en que termina, mientras que el formulario
     * pide el peso y las horas que faltan. Las dos se reconstruyen.
     */
    fun cargarParaEditar(ofertaId: Long) {
        // Si ya esta cargada, no se pisa lo que el usuario alcanzo a escribir
        if (_editandoId.value == ofertaId) return

        viewModelScope.launch {
            val oferta = repositorio.obtenerPorId(ofertaId) ?: return@launch

            _editandoId.value = ofertaId
            _formulario.value = FormularioOferta(
                titulo = oferta.titulo,
                descripcion = oferta.descripcion,
                categoria = oferta.categoria,
                precioOriginal = oferta.precioOriginal.toString(),
                precioOferta = oferta.precioOferta.toString(),
                stock = oferta.stockActual.toString(),
                horasVigencia = horasRestantes(oferta.finVigencia).toString(),
                comercio = oferta.comercio,
                direccion = oferta.direccion,
                comuna = oferta.comuna,
                pesoKg = oferta.pesoKg.toString(),
                imagenUrl = oferta.imagenUrl
            )
        }
    }

    /**
     * Horas que le quedan a la oferta, con minimo 1.
     *
     * Una oferta ya expirada que se edita vuelve a quedar vigente: se
     * entiende que el proveedor la esta reactivando.
     */
    private fun horasRestantes(finVigencia: Long): Int {
        val restante = finVigencia - System.currentTimeMillis()
        return (restante / 3_600_000L).toInt().coerceAtLeast(1)
    }

    /** Borra la oferta que se esta editando. */
    fun eliminar() {
        val id = _editandoId.value ?: return

        viewModelScope.launch {
            _guardando.value = true
            repositorio.eliminar(id)
            _guardando.value = false
            _eliminada.value = true
        }
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

            val id = _editandoId.value

            // Regla de negocio: no publicar dos veces el mismo producto
            // en el mismo comercio mientras la primera siga vigente.
            // Al editar se excluye la propia oferta de la comparacion.
            val duplicada = repositorio.existeDuplicada(
                titulo = validado.titulo,
                comercio = validado.comercio,
                excluirId = id ?: OfertaRepository.SIN_EXCLUIR
            )

            if (duplicada) {
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
            val coordenadas = Configuracion.vigente().coordenadasDe(validado.comuna)

            val oferta = OfertaEntity(
                // Con id 0, Room genera uno nuevo. Con el id de una oferta
                // existente, actualizar() reemplaza esa misma fila.
                id = id ?: 0L,
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
                pesoKg = peso,
                imagenUrl = validado.imagenUrl
            )

            if (id == null) repositorio.publicar(oferta)
            else repositorio.actualizar(oferta)

            _guardando.value = false
            _publicada.value = true
        }
    }

    /** Vista previa del CO2 mientras el usuario escribe el peso. */
    fun co2Estimado(): Double {
        val f = _formulario.value
        val peso = f.pesoKg.toDoubleOrNull() ?: return 0.0
        return calculadora.calcular(f.categoria, peso)
    }

    /**
     * La cuenta escrita, para mostrarla bajo la vista previa.
     *
     * Ej: "3,0 kg x 2,0 kg CO2e/kg = 6,0 kg CO2e". Antes la pantalla
     * mostraba solo el resultado y no habia forma de saber de donde
     * salia ese numero.
     */
    fun co2Explicado(): String {
        val f = _formulario.value
        val peso = f.pesoKg.toDoubleOrNull() ?: return ""
        return calculadora.explicacion(f.categoria, peso)
    }

    /** La fuente del factor que se esta aplicando, para que sea citable. */
    fun fuenteDelFactor(): String =
        calculadora.factorDe(_formulario.value.categoria).fuente

    companion object {

        /*
         * Ni las comunas ni las categorias se escriben aca.
         *
         * Las comunas, con sus coordenadas, salen de
         * assets/config.json: dependen de donde opere el cliente.
         *
         * Las categorias salen de las llaves de
         * assets/factores_co2.json, porque toda categoria necesita un
         * factor de CO2 para existir. Derivarlas de ahi hace imposible
         * agregar una sin darle su factor, que es lo que antes pasaba
         * en silencio aplicando el generico.
         *
         * Son get() y no val a secas porque los archivos se leen al
         * arrancar la app, despues de que esta clase se carga: con un
         * val quedarian congeladas vacias.
         */
        val COMUNAS: List<String>
            get() = Configuracion.vigente().nombresDeComunas

        val CATEGORIAS: List<String>
            get() = FactoresCO2.categorias()
    }
}
