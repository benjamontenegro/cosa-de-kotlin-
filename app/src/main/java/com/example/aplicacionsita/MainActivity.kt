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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private var permisosOk = false
    private val permisos = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
    private val lanzador = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        permisosOk = resultados.values.all { it }
        if (permisosOk) {
            Toast.makeText(this, "Permisos concedidos",
                Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Sin permisos no hay conexión",
                Toast.LENGTH_SHORT).show()
        }
    }

    private val adapter: BluetoothAdapter by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as
                BluetoothManager).adapter
    }
    private val uuid = UUID.fromString(
        "00001101-0000-1000-8000-00805F9B34FB"
    )

    // OTRA COSAAAAAAAAAAA

    private lateinit var textViewMensaje: TextView

    var rolServer = 0
    var rolClient = 0
    private var miRol = ""

    /*
    @SuppressLint("MissingPermission")
    private fun manejarConexion(socket: BluetoothSocket) {
        val salida = socket.outputStream
        val entrada = socket.inputStream
        salida.write("ENCENDER_LED".toByteArray())
        val buffer = ByteArray(1024)
        while (true) {
            val bytes = entrada.read(buffer)
            val mensaje = String(buffer, 0, bytes)
            runOnUiThread { textViewMensaje.text = mensaje }
        }
    }*/

    private var socketActual: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    private fun manejarConexion(socket: BluetoothSocket)
    {
        socketActual = socket
        val salida = socket.outputStream
        val entrada = socket.inputStream
        salida.write("ENCENDER_LED".toByteArray())
        val buffer = ByteArray(1024)
        while (true)
        {
            val bytes = entrada.read(buffer)
            val mensaje = String(buffer, 0, bytes)
            if (mensaje.startsWith("CONTADOR:"))
            {
                val valorRecibido =
                    mensaje.removePrefix("CONTADOR:").toIntOrNull() ?: continue
                runOnUiThread {
                    if (miRol == "servidor")
                    {
                        rolClient = valorRecibido
                        findViewById<TextView>(R.id.txtCountClient).text =
                            "Cliente: $rolClient"
                    }
                    else
                    {
                        rolServer = valorRecibido
                        findViewById<TextView>(R.id.txtCountServer).text =
                            "Servidor: $rolServer"
                    }
                }
            } else {
                runOnUiThread { textViewMensaje.text = mensaje }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun iniciarComoServidor() {
        miRol = "servidor"
        Toast.makeText(this, "Esperando conexión...",
            Toast.LENGTH_SHORT).show()
        val serverSocket = adapter.listenUsingRfcommWithServiceRecord(
            "MiAppIoT", uuid
        )
        Thread {
            val socket = serverSocket.accept()
            runOnUiThread {
                Toast.makeText(this, "¡Cliente conectado!",
                    Toast.LENGTH_SHORT).show()
            }
            manejarConexion(socket)
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun iniciarComoCliente(nameOfServer:String) {
        miRol = "cliente"
        Toast.makeText(this, "Buscando servidor...",
            Toast.LENGTH_SHORT).show()
        val dispositivo = adapter.bondedDevices
            .firstOrNull { it.name == nameOfServer }
        if (dispositivo == null) {
            Toast.makeText(this, "No encontré ese dispositivo emparejado",
                Toast.LENGTH_SHORT).show()
                return
        }
        val socket = dispositivo.createRfcommSocketToServiceRecord(uuid)
        Thread {
            adapter.cancelDiscovery()
            socket.connect()
            runOnUiThread {
                Toast.makeText(this, "¡Conectado al servidor!",
                    Toast.LENGTH_SHORT).show()
            }
            manejarConexion(socket)
        }.start()
    }

    private fun enviarContador(valor: Int) {
        Thread {
            try {
                socketActual?.outputStream?.write("CONTADOR:$valor".toByteArray())
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "No se pudo enviar",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        textViewMensaje = findViewById(R.id.textViewMensaje)
        val btnServidor = findViewById<Button>(R.id.btnServidor)
        val btnCliente = findViewById<Button>(R.id.btnCliente)
        val btnCount = findViewById<Button>(R.id.btnContar)
        val txtCountServidor = findViewById<TextView>(R.id.txtCountServer)
        val txtCountCliente = findViewById<TextView>(R.id.txtCountClient)

        lanzador.launch(permisos)

        btnServidor.setOnClickListener {
            if (permisosOk) iniciarComoServidor()
        }
        btnCliente.setOnClickListener {
            if (permisosOk) iniciarComoCliente("Howl")
        }

        btnCount.setOnClickListener {
            when (miRol) {
                "servidor" -> {
                    rolServer ++;
                    txtCountServidor.text = rolServer.toString();
                }
                "cliente" -> {
                    rolClient ++;
                    txtCountCliente.text = rolClient.toString();
                }
                else -> {
                    Toast.makeText(this,
                        "Eliga si es servidor o cliente",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<Button>(R.id.btnContar).setOnClickListener {
            when (miRol) {
                "servidor" -> {
                    rolServer++
                    findViewById<TextView>(R.id.txtCountServer).text = "Servidor: $rolServer"
                    enviarContador(rolServer)
                }
                "cliente" -> {
                    rolClient++
                    findViewById<TextView>(R.id.txtCountClient).text = "Cliente: $rolClient"
                    enviarContador(rolClient)
                }
                else -> {
                    Toast.makeText(this, "Primero elige servidor o cliente",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        /*
        // COSAS OLD

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
        */
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