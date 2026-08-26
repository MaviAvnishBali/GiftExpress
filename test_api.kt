import java.net.URL
import java.net.HttpURLConnection

fun main() {
    val url = URL("https://mcstaging.giftexpress.com/rest/V1/custom/categories/all")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    val response = connection.inputStream.bufferedReader().readText()
    println(response)
}
