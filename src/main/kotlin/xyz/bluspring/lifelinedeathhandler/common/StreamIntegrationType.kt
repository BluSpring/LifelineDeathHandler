package xyz.bluspring.lifelinedeathhandler.common

enum class StreamIntegrationType(val integrationName: String, val url: String) {
    STREAMELEMENTS("StreamElements", "https://realtime.streamelements.com"),
    STREAMLABS("Streamlabs", "https://sockets.streamlabs.com")
}