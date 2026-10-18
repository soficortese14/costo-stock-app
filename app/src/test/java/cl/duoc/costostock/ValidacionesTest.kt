package cl.duoc.costostock

import cl.duoc.costostock.utils.esEmailValido
import cl.duoc.costostock.utils.esNombreValido
import cl.duoc.costostock.utils.esNumeroChilenoValido
import cl.duoc.costostock.utils.esPasswordValida
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesTest {

    @Test
    fun `esNombreValido devuelve true para nombres validos`() {
        assertTrue(esNombreValido("Juan Pérez"))
        assertTrue(esNombreValido("Ana"))
    }

    @Test
    fun `esNombreValido devuelve false para nombres invalidos`() {
        assertFalse(esNombreValido(""))
        assertFalse(esNombreValido("A"))
    }

    @Test
    fun `esEmailValido devuelve true para correos validos`() {
        assertTrue(esEmailValido("comprador@duoc.cl"))
        assertTrue(esEmailValido("usuario.test@gmail.com"))
    }

    @Test
    fun `esEmailValido devuelve false para correos invalidos`() {
        assertFalse(esEmailValido(""))
        assertFalse(esEmailValido("correoInvalido"))
        assertFalse(esEmailValido("@dominio.com"))
    }

    @Test
    fun `esNumeroChilenoValido devuelve true para numeros chilenos validos`() {
        assertTrue(esNumeroChilenoValido("912345678"))
        assertTrue(esNumeroChilenoValido("+56912345678"))
        assertTrue(esNumeroChilenoValido("56912345678"))
        assertTrue(esNumeroChilenoValido("9 1234 5678"))
        assertTrue(esNumeroChilenoValido("+56 9 1234 5678"))
    }

    @Test
    fun `esNumeroChilenoValido devuelve false para numeros invalidos`() {
        assertFalse(esNumeroChilenoValido(""))
        assertFalse(esNumeroChilenoValido("12345678"))
        assertFalse(esNumeroChilenoValido("812345678"))
        assertFalse(esNumeroChilenoValido("abc123456"))
    }

    @Test
    fun `esPasswordValida devuelve true cuando tiene al menos 6 caracteres`() {
        assertTrue(esPasswordValida("123456"))
        assertTrue(esPasswordValida("contrasenaSegura123"))
    }

    @Test
    fun `esPasswordValida devuelve false cuando tiene menos de 6 caracteres`() {
        assertFalse(esPasswordValida("12345"))
        assertFalse(esPasswordValida(""))
    }
}
