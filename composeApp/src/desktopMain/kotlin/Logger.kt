object Logger {

    private val debug: Boolean
        get() = System.getenv("DEBUG")?.toBoolean() ?: false

    fun debug(message: String) {
        if (debug) {
            println("[Magrinov] $message")
        }
    }

    fun error(message: String, e: Exception) {
        println("[Magrinov] ERROR!: $message")
        e.printStackTrace()
    }

}