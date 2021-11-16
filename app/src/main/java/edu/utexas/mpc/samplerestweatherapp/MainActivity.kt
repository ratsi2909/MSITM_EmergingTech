package edu.utexas.mpc.samplerestweatherapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
// imports for MQTT
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage

class MainActivity : AppCompatActivity() {


    // I'm using lateinit for these widgets because I read that repeated calls to findViewById
    // are energy intensive
    lateinit var textView: TextView
    lateinit var retrieveButton: Button
    lateinit var syncButton: Button

    lateinit var queue: RequestQueue
    lateinit var gson: Gson
    lateinit var mostRecentWeatherResult: WeatherResult
    lateinit var getIcon: WeatherResult

    lateinit var weatherresult: String

    // code for MQTT
    lateinit var mqttAndroidClient: MqttAndroidClient

    // you may need to change this depending on where your MQTT broker is running
    val serverUri = "tcp://192.168.4.14:1883"
    // you can use whatever name you want to here
    val clientId = "EmergingTechMQTTClient"

    //these should "match" the topics on the "other side" (i.e, on the Raspberry Pi)
    val subscribeTopic = "testTopic2"
    val publishTopic = "testTopic1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = this.findViewById(R.id.text)
        retrieveButton = this.findViewById(R.id.retrieveButton)
        syncButton = this.findViewById(R.id.syncButton)


        // when the user presses the syncbutton, this method will get called
        syncButton.setOnClickListener({ syncWithPi() })

        // initialize the paho mqtt client with the uri and client id
        mqttAndroidClient = MqttAndroidClient(getApplicationContext(), serverUri, clientId);

        // when things happen in the mqtt client, these callbacks will be called
        mqttAndroidClient.setCallback(object: MqttCallbackExtended {

            // when the client is successfully connected to the broker, this method gets called
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                println("Connection Complete!!")
//                println("+++++++ PRINT WEATHER RESULT IN MQTT...")
//                println(weatherresult)
                // this subscribes the client to the subscribe topic
                mqttAndroidClient.subscribe(subscribeTopic, 0)
                val message = MqttMessage()
                message.payload = ("Hello World").toByteArray()

                // this publishes a message to the publish topic
                mqttAndroidClient.publish(publishTopic, message)
            }

            // this method is called when a message is received that fulfills a subscription
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                println(message)
            }

            override fun connectionLost(cause: Throwable?) {
                println("Connection Lost")
            }

            // this method is called when the client succcessfully publishes to the broker
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Delivery Complete")
            }
        })

        // when the user presses the syncbutton, this method will get called
        retrieveButton.setOnClickListener({ requestWeather() })

        queue = Volley.newRequestQueue(this)
        gson = Gson()
    }

    fun requestWeather(){
        val url = StringBuilder("https://api.openweathermap.org/data/2.5/weather?id=4254010&appid=ea9d4a9da52aaa9920a1f1ab27e0086e").toString()
        val stringRequest = object : StringRequest(com.android.volley.Request.Method.GET, url,
            com.android.volley.Response.Listener<String> { response ->
                //textView.text = response
                mostRecentWeatherResult = gson.fromJson(response, WeatherResult::class.java)
                textView.text = mostRecentWeatherResult.weather.get(0).main
                weatherresult = mostRecentWeatherResult.weather.get(0).main
                println("+++++++ PRINT WEATHER RESULT...")
                println(weatherresult)
                println("+++++++ getting icon...")
                getIcon = gson.fromJson(response, WeatherResult::class.java)
                val icon = getIcon.weather.get(0).icon

                val iconUrl = "https://openweathermap.org/img/w/$icon.png"
                println(iconUrl)

                //print icon in image view
                val imageView: ImageView = this.findViewById(R.id.image_view)
                Picasso.with(this).load(iconUrl).into(imageView)
            },
            com.android.volley.Response.ErrorListener { println("******That didn't work!") }) {}
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    // this method just connects the paho mqtt client to the broker
    fun syncWithPi(){
        println("+++++++ Connecting...")
        mqttAndroidClient.connect()
    }

}

class WeatherResult(val id: Int, val name: String, val cod: Int, val coord: Coordinates, val main: WeatherMain, val weather: Array<Weather>)
class Coordinates(val lon: Double, val lat: Double)
class Weather(val id: Int, val main: String, val description: String, val icon: String)
class WeatherMain(val temp: Double, val pressure: Int, val humidity: Int, val temp_min: Double, val temp_max: Double)

