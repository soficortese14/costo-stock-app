package cl.duoc.costostock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.costostock.data.CarritoRepository
import cl.duoc.costostock.data.OfertaRepository
import cl.duoc.costostock.data.Perfil
import cl.duoc.costostock.data.PreferenciasManager
import cl.duoc.costostock.data.Ubicacion
import cl.duoc.costostock.data.UbicacionManager
import cl.duoc.costostock.data.distanciaKm
import cl.duoc.costostock.data.room.OfertaEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Los estados posibles de la ubicacion del usuario.
 *
 * Se modela con sealed class para que el when de la pantalla sea exhaustivo:
 * si manana se agrega un estado nuevo, el compilador obliga a dibujarlo.
 */
sealed class EstadoUbicacion {

    /** Se esta consultando al dispositivo. */
    object Cargando : EstadoUbicacion()

    /** Se obtuvo la posicion. */
    data class Disponible(val ubicacion: Ubicacion) : EstadoUbicacion()

    /** El usuario no concedio el permiso de ubicacion. */
    object SinPermiso : EstadoUbicacion()

    /** Hay permiso, pero el dispositivo no entrego una posicion. */
    object NoDisponible : EstadoUbicacion()
}

/**
 * Criterio con el que se ordena el catalogo.
 *
 * VIGENCIA pone primero lo que esta por vencer, que es el gancho del
 * negocio. CERCANIA pone primero lo que el usuario puede ir a buscar
 * caminando, que importa porque Costo Stock no tiene delivery.
 */
enum class OrdenCatalogo { VIGENCIA, CERCANIA }

/**
 * Pantalla de ofertas: resuelve la ubicacion del usuario y entrega
 * el catalogo filtrado por categoria.
 */
class OfertasViewModel(application: Application) : AndroidViewModel(application) {

    private val ubicacionManager = UbicacionManager(application)
    private val repositorio = OfertaRepository(application)
    private val preferencias = PreferenciasManager(application)
    private val carrito = CarritoRepository(application)

    /**
     * Perfil con el que se esta navegando. Decide si se muestra el boton
     * de publicar: solo el proveedor puede cargar ofertas.
     */
    val perfilActivo: StateFlow<Perfil> =
        preferencias.perfilActivo.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Perfil.COMPRADOR
        )

    /** Unidades en el carrito, para el globo del icono. */
    val unidadesCarrito: StateFlow<Int> =
        carrito.observarUnidades().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    private val _estado = MutableStateFlow<EstadoUbicacion>(EstadoUbicacion.Cargando)
    val estado: StateFlow<EstadoUbicacion> = _estado

    // Radio de busqueda en kilometros. El cliente pidio 15 por defecto,
    // ajustable hasta 30.
    private val _radioKm = MutableStateFlow(15)
    val radioKm: StateFlow<Int> = _radioKm

    private val _categoria = MutableStateFlow(OfertaRepository.CATEGORIA_TODAS)
    val categoria: StateFlow<String> = _categoria

    /** Texto escrito en el buscador. Vacio significa "sin filtro". */
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    private val _comuna = MutableStateFlow(OfertaRepository.COMUNA_TODAS)
    val comuna: StateFlow<String> = _comuna

    private val _orden = MutableStateFlow(OrdenCatalogo.VIGENCIA)
    val orden: StateFlow<OrdenCatalogo> = _orden

    /**
     * Comunas disponibles para filtrar. Salen de la tabla, asi que una
     * comuna nueva aparece sola apenas se publica una oferta ahi.
     */
    val comunas: StateFlow<List<String>> =
        repositorio.observarComunas()
            .map { listOf(OfertaRepository.COMUNA_TODAS) + it }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = listOf(OfertaRepository.COMUNA_TODAS)
            )

    /**
     * Marca de tiempo que avanza cada segundo.
     *
     * Los contadores regresivos la observan: al cambiar, Compose redibuja
     * solo los textos del tiempo restante y no toda la lista.
     */
    private val _ahora = MutableStateFlow(System.currentTimeMillis())
    val ahora: StateFlow<Long> = _ahora

    /**
     * El catalogo. Se arma combinando la categoria elegida, el texto del
     * buscador y lo que emite Room: al cambiar cualquiera de los tres, la
     * lista se recalcula sola.
     */
    /**
     * Los cuatro filtros viajan juntos.
     *
     * combine con tipos llega hasta cinco flujos y aca hacen falta seis
     * (los cuatro filtros, la ubicacion y la tabla). Agruparlos primero
     * evita tener que anidar combines.
     */
    private val filtros = combine(
        _categoria,
        _busqueda,
        _comuna,
        _radioKm
    ) { categoria, texto, comuna, radio ->
        Filtros(categoria, texto, comuna, radio)
    }

    val ofertas: StateFlow<List<OfertaEntity>> =
        combine(
            filtros,
            _estado,
            _orden,
            repositorio.observarOfertas()
        ) { f, estadoUbicacion, ordenActual, todas ->
            todas
                .filter {
                    f.categoria == OfertaRepository.CATEGORIA_TODAS ||
                        it.categoria == f.categoria
                }
                .filter {
                    f.comuna == OfertaRepository.COMUNA_TODAS ||
                        it.comuna == f.comuna
                }
                .filter { coincideConBusqueda(it, f.texto) }
                .filter { dentroDelRadio(it, estadoUbicacion, f.radioKm) }
                .let { ordenar(it, ordenActual, estadoUbicacion) }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        cargarCatalogo()
        cargarUbicacion()
        iniciarReloj()
    }

    /** Puebla la base la primera vez que se abre la app. */
    private fun cargarCatalogo() {
        viewModelScope.launch {
            repositorio.cargarDatosIniciales()
        }
    }

    /** Hace avanzar los contadores regresivos. */
    private fun iniciarReloj() {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                _ahora.value = System.currentTimeMillis()
            }
        }
    }

    /** Consulta la ubicacion. La pantalla tambien la llama al reintentar. */
    fun cargarUbicacion() {
        viewModelScope.launch {
            _estado.value = EstadoUbicacion.Cargando

            if (!ubicacionManager.hayPermiso()) {
                _estado.value = EstadoUbicacion.SinPermiso
                return@launch
            }

            val ubicacion = ubicacionManager.obtenerUbicacion()

            _estado.value =
                if (ubicacion != null) EstadoUbicacion.Disponible(ubicacion)
                else EstadoUbicacion.NoDisponible
        }
    }

    fun cambiarCategoria(nueva: String) {
        _categoria.value = nueva
    }

    /** Guarda lo que se escribio en el buscador. */
    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun cambiarComuna(nueva: String) {
        _comuna.value = nueva
    }

    fun cambiarOrden(nuevo: OrdenCatalogo) {
        _orden.value = nuevo
    }

    /** Cambia el radio de busqueda, entre 1 y 30 km. */
    fun cambiarRadio(km: Int) {
        _radioKm.value = km.coerceIn(1, 30)
    }

    /** Descuenta una unidad del stock. Demuestra escritura en Room. */
    fun comprar(oferta: OfertaEntity) {
        viewModelScope.launch {
            repositorio.comprar(oferta.id, 1)
        }
    }

    /** Vuelve a dejar el catalogo como al inicio. */
    fun reiniciarCatalogo() {
        viewModelScope.launch {
            repositorio.reiniciarCatalogo()
        }
    }
}

/**
 * true si la oferta calza con lo que escribio el usuario.
 *
 * Se busca en el titulo, la descripcion y el comercio, ignorando
 * mayusculas: escribir "pan" encuentra "Pan de molde integral".
 * Con el campo vacio pasan todas las ofertas.
 *
 * Esta fuera del ViewModel a proposito: es una regla pura, sin estado,
 * y asi se puede probar sin levantar la pantalla.
 */
private fun coincideConBusqueda(oferta: OfertaEntity, texto: String): Boolean {
    val termino = texto.trim()
    if (termino.isBlank()) return true

    return oferta.titulo.contains(termino, ignoreCase = true) ||
        oferta.descripcion.contains(termino, ignoreCase = true) ||
        oferta.comercio.contains(termino, ignoreCase = true)
}

/** Los filtros del catalogo, agrupados para poder combinarlos de una vez. */
private data class Filtros(
    val categoria: String,
    val texto: String,
    val comuna: String,
    val radioKm: Int
)

/**
 * true si la oferta esta dentro del radio elegido.
 *
 * Sin ubicacion no se puede medir nada, asi que pasan todas: el cliente
 * definio cobertura en la V Region y es preferible mostrar el catalogo
 * completo antes que una pantalla vacia por no tener GPS.
 */
private fun dentroDelRadio(
    oferta: OfertaEntity,
    estado: EstadoUbicacion,
    radioKm: Int
): Boolean {
    if (estado !is EstadoUbicacion.Disponible) return true

    val distancia = distanciaKm(
        estado.ubicacion.latitud,
        estado.ubicacion.longitud,
        oferta.latitud,
        oferta.longitud
    )
    return distancia <= radioKm
}

/**
 * Ordena el catalogo segun el criterio elegido.
 *
 * Si se pide cercania pero no hay ubicacion, no hay desde donde medir,
 * asi que se cae a vigencia en vez de dejar la lista en cualquier orden.
 */
private fun ordenar(
    ofertas: List<OfertaEntity>,
    orden: OrdenCatalogo,
    estado: EstadoUbicacion
): List<OfertaEntity> =
    if (orden == OrdenCatalogo.CERCANIA && estado is EstadoUbicacion.Disponible) {
        ofertas.sortedBy {
            distanciaKm(
                estado.ubicacion.latitud,
                estado.ubicacion.longitud,
                it.latitud,
                it.longitud
            )
        }
    } else {
        ofertas.sortedBy { it.finVigencia }
    }
