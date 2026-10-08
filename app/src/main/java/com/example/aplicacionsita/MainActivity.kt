package com.example.aplicacionsita


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val nombres = listOf(
            "meowth", "meowtha", "mew", "snorlax"
        )
        val imagenes = listOf(
            R.drawable.meowth, R.drawable.meowtha, R.drawable.mew, R.drawable.snorlax
        )

        val descripcion = listOf(
            "gato", "gato morado", "gato ancestral", "gato gordo"
        )

        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, nombres
        )

        val spinner = findViewById<Spinner>(R.id.spinner)
        val imgPokemon = findViewById<ImageView>(R.id.imgPokemon)
        val txtPokemon = findViewById<TextView>(R.id.txtPokemon)
        val btnAplicar = findViewById<Button>(R.id.btnAplicar)
        btnAplicar.text = "Random"

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    imgPokemon.setImageResource(imagenes[position])
                    txtPokemon.text = descripcion[position]
                    btnAplicar.text = "Random %d".format(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        btnAplicar.setOnClickListener {
            val posrandom = Random.nextInt(0, 4)
            spinner.setSelection(posrandom)
            btnAplicar.text = "Random %d".format(posrandom)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart()
    {
        super.onStart()
        Log.d("MainActivity", "onStart() Llamado")
    }

    override fun onPause()
    {
        super.onPause()
        Log.d("MainActivity", "onPause() Llamado")
    }

    override fun onResume()
    {
        super.onResume()
        Log.d("MainActivity", "onResume Llamado")
    }

    override fun onRestart()
    {
        super.onRestart()
        Log.d("MainActivity", "onRestart Llamado")
    }

    override fun onStop()
    {
        super.onStop()
        Log.d("MainActivity", "KAJSDBKABdsk")
        Log.d("MainActivity", "onStop Llamado")
    }

    override fun onDestroy()
    {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy Llamado")
    }
}