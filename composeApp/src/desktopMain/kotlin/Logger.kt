object Logger {

    private val debug: Boolean
        get() = System.getenv("DEBUG_MODE")?.toBoolean() ?: false

    fun debug(message: String) {
        if (debug) {
            println("[Magrinov] $message")
        }
    }

}