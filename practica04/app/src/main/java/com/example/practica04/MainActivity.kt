package com.example.practica04

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    // Referencias a los controles del layout
    private lateinit var spinnerColor: Spinner
    private lateinit var radioGroupNivel: RadioGroup
    private lateinit var checkProgramacion: CheckBox
    private lateinit var checkDiseno: CheckBox
    private lateinit var checkBasesDatos: CheckBox
    private lateinit var switchNotificaciones: Switch
    private lateinit var seekBarSatisfaccion: SeekBar
    private lateinit var tvSeekBarValor: TextView
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var btnElegirFecha: Button
    private lateinit var btnProcesar: Button
    private lateinit var tvResumen: TextView

    // Variables de estado que se van actualizando con cada listener
    private var colorSeleccionado: String = ""
    private var nivelSeleccionado: String = "No seleccionado"
    private var satisfaccion: Int = 50
    private var fechaSeleccionada: String = "Ninguna"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enlazar vistas (findViewById)
        spinnerColor = findViewById(R.id.spinnerColor)
        radioGroupNivel = findViewById(R.id.radioGroupNivel)
        checkProgramacion = findViewById(R.id.checkProgramacion)
        checkDiseno = findViewById(R.id.checkDiseno)
        checkBasesDatos = findViewById(R.id.checkBasesDatos)
        switchNotificaciones = findViewById(R.id.switchNotificaciones)
        seekBarSatisfaccion = findViewById(R.id.seekBarSatisfaccion)
        tvSeekBarValor = findViewById(R.id.tvSeekBarValor)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        btnElegirFecha = findViewById(R.id.btnElegirFecha)
        btnProcesar = findViewById(R.id.btnProcesar)
        tvResumen = findViewById(R.id.tvResumen)

        configurarSpinner()
        configurarRadioGroup()
        configurarSeekBar()
        configurarDatePicker()
        configurarBotonProcesar()
    }

    /** Spinner: lista desplegable de opciones, escuchada con OnItemSelectedListener */
    private fun configurarSpinner() {
        val colores = listOf("Rojo", "Verde", "Azul", "Amarillo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colores)
        spinnerColor.adapter = adapter

        spinnerColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                colorSeleccionado = colores[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                colorSeleccionado = ""
            }
        }
    }

    /** RadioGroup: opciones mutuamente excluyentes, escuchado con setOnCheckedChangeListener */
    private fun configurarRadioGroup() {
        radioGroupNivel.setOnCheckedChangeListener { _, checkedId ->
            nivelSeleccionado = when (checkedId) {
                R.id.radioPrincipiante -> "Principiante"
                R.id.radioIntermedio -> "Intermedio"
                R.id.radioAvanzado -> "Avanzado"
                else -> "No seleccionado"
            }
        }
    }

    /** SeekBar: valor numerico continuo, escuchado con OnSeekBarChangeListener */
    private fun configurarSeekBar() {
        seekBarSatisfaccion.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                satisfaccion = progress
                tvSeekBarValor.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /** Selector de fecha: boton que abre un DatePickerDialog */
    private fun configurarDatePicker() {
        btnElegirFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, anioSel, mesSel, diaSel ->
                // El mes de DatePickerDialog es base 0, se suma 1 para mostrarlo correctamente
                fechaSeleccionada = "%02d/%02d/%d".format(diaSel, mesSel + 1, anioSel)
                tvFechaSeleccionada.text = fechaSeleccionada
            }, anio, mes, dia).show()
        }
    }

    /** Boton de procesamiento: recolecta el estado de todos los controles y lo resume */
    private fun configurarBotonProcesar() {
        btnProcesar.setOnClickListener {
            val intereses = mutableListOf<String>()
            if (checkProgramacion.isChecked) intereses.add("Programación")
            if (checkDiseno.isChecked) intereses.add("Diseño")
            if (checkBasesDatos.isChecked) intereses.add("Bases de datos")

            val interesesTexto = if (intereses.isEmpty()) "Ninguno" else intereses.joinToString(", ")
            val notificacionesTexto = if (switchNotificaciones.isChecked) "Activadas" else "Desactivadas"

            val resumen = """
                Color favorito: $colorSeleccionado
                Nivel de experiencia: $nivelSeleccionado
                Intereses: $interesesTexto
                Notificaciones: $notificacionesTexto
                Satisfacción: $satisfaccion%
                Fecha seleccionada: $fechaSeleccionada
            """.trimIndent()

            tvResumen.text = resumen
        }
    }
}
