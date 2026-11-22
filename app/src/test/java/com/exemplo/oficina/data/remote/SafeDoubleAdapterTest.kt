package com.exemplo.oficina.data.remote

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter

class SafeDoubleAdapterTest {
    private val adapter = SafeDoubleAdapter()

    @Test
    fun leString250Como250() {
        JsonReader(StringReader("\"250.00\"")).use { reader ->
            assertEquals(250.0, adapter.read(reader), 0.0001)
        }
    }

    @Test
    fun leNumero250Como250() {
        JsonReader(StringReader("250")).use { reader ->
            assertEquals(250.0, adapter.read(reader), 0.0001)
        }
    }

    @Test
    fun leStringVaziaComoZero() {
        JsonReader(StringReader("\"\"")).use { reader ->
            assertEquals(0.0, adapter.read(reader), 0.0001)
        }
    }

    @Test
    fun escreveComoNumeroJson() {
        val output = StringWriter()
        JsonWriter(output).use { writer ->
            adapter.write(writer, 250.0)
        }

        assertTrue(output.toString().matches(Regex("-?\\d+(\\.\\d+)?")))
        assertEquals("250.0", output.toString())
    }
}
