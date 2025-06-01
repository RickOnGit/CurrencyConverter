package com.example.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val myCoroutineScope = CoroutineScope(Dispatchers.IO)
    var baseCurrency = "USD"
    var convertedToCurrency = "EUR"
    private var conversionRate = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        spinnerSetup()
        textChanged()
    }

    private fun textChanged() {
        val editText: EditText = findViewById(R.id.et_firstConversion)
        var debounceJob: Job? = null

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                debounceJob?.cancel()
                debounceJob = myCoroutineScope.launch {
                    delay(500) //for limiting API calls while typing
                    val currentInput = s?.toString()
                    if (!currentInput.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            getApiResult()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            getApiResult()
                            Toast.makeText(applicationContext, "Type a value", Toast.LENGTH_SHORT).show()
                        }}
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun getApiResult() {
        //val API = "http://10.176.134.58:5000/convert?from=$baseCurrency&to=$convertedToCurrency"
        val API = "http://192.168.0.109:5000/convert?from=$baseCurrency&to=$convertedToCurrency"
        myCoroutineScope.launch {
            try {
                val apiResult: String = URL(API).readText()
                val jsonObject = JSONObject(apiResult)

                conversionRate = jsonObject.getString("$baseCurrency-$convertedToCurrency").toFloat()
                Log.d("Main", "Conversion Rate: $conversionRate")

                val fromValueText: String = findViewById<EditText>(R.id.et_firstConversion).text.toString()
                val fromValue: Float = fromValueText.toFloatOrNull() ?: 0f
                val convertedValue = fromValue * conversionRate

                withContext(Dispatchers.Main) {
                    val conversionRateTextView: TextView = findViewById(R.id.tv_conversionRate)
                    conversionRateTextView.text = String.format("Conversion Rate: %s", conversionRate)

                    val toEditText: EditText = findViewById(R.id.et_secondConversion)
                    toEditText.setText(convertedValue.toString())
                }
            } catch (e: Exception) {
                Log.e("Main", "Error fetching API result: $e")
            }
        }
    }

    private fun spinnerSetup() {
        val spinner: Spinner = findViewById(R.id.spinner_firstConversion)
        val spinner2: Spinner = findViewById(R.id.spinner_secondConversion)

        val adapter = ArrayAdapter.createFromResource( //string arrays defined into res/values/strings.xml
            this,
            R.array.currencies, //string array to be used
            android.R.layout.simple_spinner_item // standard android layout
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) //object with the list of currencies + how to display those
        spinner.adapter = adapter //the spinner gonna use the adapter<x>


        val adapter2 = ArrayAdapter.createFromResource(
            this,
            R.array.currencies2,
            android.R.layout.simple_spinner_item,
        )
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?, // equal to spinner<x>
                view: View?,
                position: Int,
                id: Long,
            ) {
                baseCurrency = parent?.getItemAtPosition(position).toString() //passing the x currency as base currency
                getApiResult()
            }
        }

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                convertedToCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }
        }
    }
}