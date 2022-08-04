package xyz.bluspring.lifelinedeathhandler.server.integration.twitch

import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import xyz.bluspring.lifelinedeathhandler.server.LifelineDeathHandlerServer
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.*

class LiveManager(private val server: MinecraftServer) {
    private val streamChannels = mutableListOf<StreamChannel>()
    private val timer = Timer()

    private val twitchApiConfig = LifelineDeathHandlerServer.config.twitchApi
    private var accessToken: String = ""

    private val tokenFile = File(FabricLoader.getInstance().gameDir.toFile(), "twitch_access_token.lifeline.json")

    fun addChannel(player: PlayerEntity, channel: String) {
        val streamChannel = streamChannels.firstOrNull { player.uuid == it.player && it.twitchChannel == channel }
            ?: StreamChannel(
                player.uuid, channel
            )

        streamChannels.removeIf { player.uuid == it.player }
        streamChannels.add(streamChannel)

        checkForList(listOf(streamChannel), ranBefore = false, fromLogin = true)
    }

    init {
        if (!tokenFile.exists())
            updateAccessToken()

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    this@LiveManager.check()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 0L, 30 * 1000)
    }

    private fun updateAccessToken() {
        val client = HttpClient.newHttpClient()

        val params = mapOf(
            "client_id" to twitchApiConfig.clientId,
            "client_secret" to twitchApiConfig.clientSecret,
            "grant_type" to "client_credentials"
        ).map { (key, value) ->
            "$key=$value"
        }.joinToString("&")

        val request = HttpRequest
            .newBuilder()
            .uri(URI("https://id.twitch.tv/oauth2/token"))
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    params
                )
            )
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200)
            throw Exception("Failed to update access token!\n${response.body()}")

        val json = JsonParser.parseString(response.body()).asJsonObject
        accessToken = json.get("access_token").asString
    }

    fun isLive(player: PlayerEntity): Boolean {
        return streamChannels.any { it.player == player.uuid && it.isLive }
    }

    fun check() {
        val channelsToCheck = mutableListOf<StreamChannel>()

        streamChannels.forEach {
            val player = server.playerManager.getPlayer(it.player)
            if (player == null) {
                if (System.currentTimeMillis() - it.lastCheck >= 30 * 60 * 1000) {
                    streamChannels.remove(it)
                }

                return@forEach
            }

            channelsToCheck.add(it)
        }

        checkForList(channelsToCheck)
    }

    fun checkForList(channels: List<StreamChannel>, ranBefore: Boolean = false, fromLogin: Boolean = false) {
        if (channels.isEmpty())
            return

        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(URI("https://api.twitch.tv/helix/streams?user_login=${
            channels.filter { !fromLogin || System.currentTimeMillis() - it.lastCheck >= 15 * 60 * 1000 }.joinToString(
                "&user_login="
            ) { it.twitchChannel }
        }"))
            .GET()
            .headers("Authorization", "Bearer $accessToken", "Client-ID", twitchApiConfig.clientId, "Client-Secret", twitchApiConfig.clientSecret)
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 401 || response.statusCode() == 403) {
            if (ranBefore)
                throw Exception("Failed to check for streams, access token may have failed to update!")

            updateAccessToken()
            checkForList(channels, true)

            return
        }

        if (response.statusCode() == 429) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    checkForList(channels)
                }
            },System.currentTimeMillis() - Instant.ofEpochSecond(Integer.parseInt(response.headers().map()["Ratelimit-Reset"]?.get(0) ?: "0").toLong()).toEpochMilli() + 300L)

            return
        }

        if (response.statusCode() != 200)
            throw Exception("Failed to check for live channels: ${response.statusCode()}")

        val liveChannels = JsonParser.parseString(response.body()).asJsonObject.getAsJsonArray("data")
        val playersToUpdate = mutableListOf<ServerPlayerEntity>()

        val liveStreamChannels = mutableListOf<StreamChannel>()

        liveChannels.forEach {
            val channelData = it.asJsonObject

            // Some people can have the same channel as a bunch of others, make sure to check that.
            val streamChannels = channels.filter { channel ->
                channel.twitchChannel == channelData.get("user_login").asString
            }

            streamChannels.forEach channel@{ channel ->
                channel.isLive = true
                val player = server.playerManager.getPlayer(channel.player) ?: return@channel

                playersToUpdate.add(player)
                liveStreamChannels.add(channel)
            }
        }

        channels.filterNot { liveStreamChannels.contains(it) }.forEach {
            it.isLive = false

            val player = server.playerManager.getPlayer(it.player) ?: return@forEach

            playersToUpdate.add(player)
        }

        if (playersToUpdate.isNotEmpty())
            server.playerManager.sendToAll(
                PlayerListS2CPacket(
                    PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME,
                    *playersToUpdate.toTypedArray()
                )
            )
    }
}