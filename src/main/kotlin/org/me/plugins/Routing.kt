package org.me.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.me.data.dao
import org.me.models.Chat
import org.me.models.User
import org.me.models.UserSafe
import org.me.models.network.NewChatData
import org.me.models.network.SecretData
import org.me.models.network.SignInData

fun Application.configureRouting() {
    val userSessions = mutableMapOf<String, UserSession>()

    routing {
        post("/createNewAccount" ){
            try {
                val user = call.receive<User>()
                if(dao.getUserByUsername(user.username) != null){
                    call.respondText("UserExists", status = HttpStatusCode.BadRequest)
                    return@post
                }
                dao.addNewUser(user)
                call.respond(HttpStatusCode.Created)
            }
            catch(e: Error) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/createNewChat"){
            try{
                val chat = call.receive<NewChatData>()
                val id = dao.addNewChat(chat)
                call.respond(HttpStatusCode.Created)
            }
            catch(e: Error){
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/signIn") {
            try {
                val signInData = call.receive<SignInData>()
                println(signInData.toString())
                val isLogIn = dao.logIn(signInData)
                if(isLogIn){
                    val socketId: Long = if(userSessions.containsKey(signInData.username)) {
                        userSessions[signInData.username]!!.secret
                    }
                    else{
                        (Math.random() * Long.MAX_VALUE).toLong()
                    }
                    this.application.subscribeUser(signInData.username, socketId, userSessions)
                    call.respondText(socketId.toString(), status = HttpStatusCode.OK)
                }
                else{
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            catch (e: Exception){
                println(e.localizedMessage)
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/getAllMessages") {
            try {
                val secret = call.receive<SecretData>()
                if (userSessions[secret.username]?.secret == secret.secret) {
                    val messages = dao.getMessagesByUser(secret.username)
                    call.respond(Json.encodeToString(messages))
                }
            }
            catch (e: Exception) {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/getChatsByUser"){
            try{
                val secret = call.receive<SecretData>()
                if (userSessions[secret.username]?.secret == secret.secret) {
                    val chats = dao.getChatsByUsername(secret.username)
                    val toSend = Json.encodeToString(chats)
                    call.respond(toSend)
                }else{
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            catch (e: Exception) {
                println(e.localizedMessage)
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/getUserByUsername"){
            try{
                val username = call.receiveText()
                val user = dao.getUserByUsername(username)
                if(user != null){
                    call.respond<UserSafe>(user)
                }
            }
            catch(e: Exception){
                println(e.localizedMessage)
            }
        }

        post("/getChatById"){
            try{
                val id = call.receiveText()
                val chat = dao.getChatById(id)
                if(chat != null){
                    call.respond<Chat>(chat)
                }
            }
            catch(e: Exception){
                println(e.localizedMessage)
            }
        }
    }
}
