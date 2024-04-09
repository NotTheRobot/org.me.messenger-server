package org.me.plugins

import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import org.me.data.dao
import org.me.models.Message
import org.me.models.network.NewMessageData
import org.me.utils.AtomicRefs
import java.io.File
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)

    }
}

fun Application.subscribeUser(username: String, secret: Long, userSessions: MutableMap<String, UserSession>) {
    val currentSession = UserSession(userSessions, secret)
    userSessions.put(username, currentSession)

    routing {
        webSocket("/$username/$secret/messages") {
            currentSession.websocketMessages = this
            userSessions.put(username, currentSession)
            try {
                for(frame in incoming){
                    val message = converter!!.deserialize<NewMessageData>(frame)
                    val id = dao.insertMessage(message, username)
                    println("message id: $id")
                    val result = dao.getMessageById(id)
                    if(result != null){
                        val usersToNotice = dao.getUsernamesByChat(message.receiverChatId)
                        userSessions[username]!!.sendMessageToMembers(usersToNotice, result)
                    }
                }
            } catch (e: Exception) {
                userSessions.remove(username)
                println(e.localizedMessage)
            }
        }

        webSocket("/$username/$secret/sendImages") {
            currentSession.websocketImages = this
            try {
                for (frame in incoming) {
                    val data = frame.data
                    val picture = data.copyOfRange(0, data.size - 1)
                    val format = if (data.last() == 0.toByte()) ".jpg" else ".png"
                    val pictureId = AtomicRefs.atomicImage.getAndIncrement()
                    val path = "resources/images/$pictureId$format"
                    val file = File(path)
                    file.createNewFile()
                    file.writeBytes(picture)
                    send(path)
                }
            }
            catch (e: Exception){
                userSessions.remove(username)
                println(e.localizedMessage)
            }
        }

        webSocket("/$username/$secret/uploadImages") {
            try {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val path = frame.readText()
                    val file: File = File(path)
                    if (file.exists() && dao.checkRulesForImage(username, path)) {
                        val picture = file.readBytes()
                        send(picture)
                    }
                }
            }
            catch (e: Exception){
                userSessions.remove(username)
                println(e.localizedMessage)
            }
        }
    }
}

class UserSession(
    val userSessions: Map<String, UserSession>,
    val secret: Long
) {
    lateinit var websocketMessages: DefaultWebSocketServerSession
    lateinit var websocketImages: DefaultWebSocketServerSession

    suspend fun sendMessageToMembers(usernames: List<String>, message: Message){
        usernames.forEach {
            userSessions[it]?.websocketMessages?.sendSerialized(message)
        }
    }

    suspend fun sendImageToMembers(usernames: List<String>, image: ByteArray){
        usernames.forEach {
            userSessions[it]!!.websocketImages.send(image)
        }
    }
}
