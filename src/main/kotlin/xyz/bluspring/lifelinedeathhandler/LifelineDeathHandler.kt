package xyz.bluspring.lifelinedeathhandler

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.api.ModInitializer
import org.json.JSONObject

class LifelineDeathHandler : ModInitializer {
    override fun onInitialize() {

    }

    companion object {
        /**
         * The org.json methods make no sense to me, and they feel like you need to do more.
         * So might as well convert them to Gson, a format that feels easier to comprehend.
         */
        fun convertJsonToGson(json: JSONObject): JsonObject {
            return JsonParser.parseString(json.toString()).asJsonObject
        }

        const val TESTING = false
    }
}