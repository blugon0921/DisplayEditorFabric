package kr.blugon.displayeditorfabric.client.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kr.blugon.displayeditorfabric.client.api.NamedTextColor
import kr.blugon.displayeditorfabric.client.api.color
import kr.blugon.displayeditorfabric.client.api.literal
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


private var isFirstJoin = true
fun registerJoinEvent() {
    val repoUrl = "https://github.com/blugon0921/DisplayEditorFabric/releases"
    ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { handler, sender, client ->
        if(!isFirstJoin) return@Join
        if(client.player != null) {
            val isLatestVersion = isLatestVersion()
            if(isLatestVersion?.first == false) {
                client.player!!.sendMessage(
                    "DisplayEditor를 ${isLatestVersion.second.original}버전으로 업데이트해주세요 (해당 메세지 클릭시 다운로드 페이지로 이동)".literal
                        .setStyle(Style.EMPTY
                            .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, repoUrl))
                            .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "클릭하여 이동".literal))
                        )
                        .color(NamedTextColor.YELLOW)
                )
            }
            isFirstJoin = false
        }
    })
}

private fun isLatestVersion(): Pair<Boolean, Version>? {
    val nowVersionString = FabricLoader.getInstance().getModContainer("displayeditorfabric")?.get()?.metadata?.version?.friendlyString ?: return null
    val nowVersion = Version(versionMMP(nowVersionString), nowVersionString)
    val repoApiUrl = "https://api.github.com/repos/blugon0921/DisplayEditorFabric"
    val client = HttpClient.newBuilder().build()
    val request  = HttpRequest.newBuilder()
        .uri(URI.create("${repoApiUrl}/releases/latest"))
        .GET()
        .build()
    return try {
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return if (response.statusCode() == 200) {
            val json = Json.parseToJsonElement(response.body())
            val originalVersion = json.jsonObject["tag_name"]?.jsonPrimitive?.content?: return null
            val version = Version(versionMMP(originalVersion), originalVersion)
            val latestVersion = version.getMoreLatest(nowVersion)
            val isLatest = latestVersion.original == nowVersion.original
            return isLatest to latestVersion
        } else null
    } catch (e: Exception) { println(e).let { null } }
}


private fun versionMMP(original: String): String {
    return original.split("+").first().replace("v", "")
}

private class Version(val major: Int, val minor: Int, val patch: Int, val original: String) {
    constructor(version: String, original: String): this(
        version.split(".").first().toIntOrNull()?: -1,
        version.split(".").getOrNull(1)?.toIntOrNull() ?: -1,
        version.split(".").getOrNull(2)?.toIntOrNull() ?: -1,
        original
    )

    fun getMoreLatest(version: Version): Version {
        return if(this.toString().replace(".", "").toInt() < version.toString().replace(".", "").toInt()) {
            version
        } else this
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }
}