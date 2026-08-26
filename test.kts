fun main() {
    val url = "https://magento-1620955-6409883.cloudwaysapps.com/brand/adrienne-vittadini.html"
    val fallbackName = url.substringAfterLast("/").removeSuffix(".html")
        .replace("-", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
    println(fallbackName)
}
