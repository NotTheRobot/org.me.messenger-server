package org.me.data

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.me.data.DatabaseSingleton.dbQuery
import org.me.models.*
import org.me.models.network.NewMessageData
import org.me.models.network.NewChatData
import org.me.models.network.SignInData
import java.util.UUID

class MainDao() {

    init{
        runBlocking {
            addNewUser(User("kekw", "firstUser", "228322", null))
            addNewUser(User("kekw2", "secondUser", "228322", null))
            createAtomic()
        }
    }

    suspend fun createAtomic(){
        dbQuery {
            AtomicRefTable.replace {
                it[AtomicRefTable.id] = 1
                it[AtomicRefTable.imageRef] = 1
                it[AtomicRefTable.soundRef] = 1
            }
        }
    }

    suspend fun getUserByUsername(username: String): UserSafe?{
        return dbQuery{
            UserTable
                .select{UserTable.username eq username}
                .map { it.toUserSafe() }
                .singleOrNull()
        }
    }

    suspend fun addNewUser(user: User){
        dbQuery {
            UserTable.replace {
                it[username] = user.username
                it[alterName] = user.alterName
                it[password] = user.password
            }
        }
    }

    suspend fun updateUser(user: User){
        dbQuery {
            UserTable.update {
                it[username] = user.username
                it[alterName] = user.alterName
                it[password] = user.password
                it[imageRef] = user.imageRef
            }
        }
    }

    suspend fun getChatById(id: String): Chat?{
        return dbQuery {
            ChatTable
                .select{ ChatTable.id eq id }
                .map { it.toChat() }
                .singleOrNull()
        }
    }

    suspend fun addNewChat(chat: NewChatData){
        val chatId = dbQuery {
            ChatTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[chatName] = chat.chatName
                it[imageRef] = chat.imageRef
            }[ChatTable.id]
        }
        addUsersToChat(chatId, chat.users)
    }

    suspend fun updateChat(chatDTO: Chat){
        dbQuery {
            ChatTable.update({ChatTable.id eq chatDTO.id}) {
                it[ChatTable.chatName] = chatDTO.chatName
                it[ChatTable.imageRef] = chatDTO.imageRef
            }
        }
    }

    suspend fun addUsersToChat(chat: String, users: List<String>) {
        dbQuery {
            users.forEach { user ->
                UserChatTable.insert {
                    it[chatId] = chat
                    it[username] = user
                }
            }
        }
    }

    suspend fun deleteUserFromChat(user: String, chat: String){
        dbQuery {
            UserChatTable.deleteWhere {
                (UserChatTable.chatId eq chat) and (UserChatTable.username eq user)
            }
        }
    }

    suspend fun getMessagesByChat(chat: String): List<Message>{
        return dbQuery {
            MessageTable
                .select{ MessageTable.receiverChatId eq chat }
                .map { it.toMessage() }
        }
    }
    suspend fun getMessagesByUser(user: String): List<List<Message>>{
        return dbQuery {
            val chatsId = UserChatTable.select { UserChatTable.username eq user }.map { it[UserChatTable.chatId] }
            val result = mutableListOf<List<Message>>()
            chatsId.forEach{
                result.add(getMessagesByChat(it))
            }
            result
        }
    }
    suspend fun getMessageById(id: String): Message?{
        return dbQuery {
            MessageTable
                .select { MessageTable.id eq id }
                .map { it.toMessage() }
                .singleOrNull()
        }
    }

    suspend fun insertMessage(message: NewMessageData, sender: String): String{
        return dbQuery {
            MessageTable.insert {
                it[MessageTable.id] = UUID.randomUUID().toString()
                it[MessageTable.receiverChatId] = message.receiverChatId
                it[MessageTable.senderUsername] = sender
                it[MessageTable.message] = message.message
                it[MessageTable.time] = message.time
                it[MessageTable.imageRef] = message.imageRef
                it[MessageTable.soundRef] = message.soundRef
                it[MessageTable.gifRef] = message.gifRef
            }[MessageTable.id]
        }
    }

    suspend fun logIn(signInData: SignInData): Boolean {
        return dbQuery {
            UserTable
                .select {
                    (UserTable.username eq signInData.username) and
                    (UserTable.password eq signInData.password)
                }
                .toList()
                .isNotEmpty()
        }
    }

    suspend fun getAtomic(): AtomicRef{
        return dbQuery {
            AtomicRefTable
                .selectAll()
                .map { it.toAtomicRef() }
                .singleOrNull()!!

        }
    }

    suspend fun getUsernamesByChat(chatId: String): List<String> {
        return dbQuery {
            UserChatTable
                .select { UserChatTable.chatId eq chatId }
                .map { it[UserChatTable.username] }
        }
    }

    suspend fun getChatIdsByUsername(username: String): List<String>{
        return dbQuery {
            UserChatTable
                .select{ UserChatTable.username eq username }
                .map { it[UserChatTable.chatId] }
        }
    }

    suspend fun getChatsByUsername(username: String): List<Chat> {
        return dbQuery {
            val chatIds = getChatIdsByUsername(username)
            ChatTable
                .select { ChatTable.id.inList(chatIds) }
                .map { it.toChat() }
        }
    }

    suspend fun checkRulesForImage(username: String, imageRef: String): Boolean{
        return dbQuery {
            val imageChats = MessageTable.select{ MessageTable.imageRef eq  imageRef}.map { it[MessageTable.receiverChatId] }
            val userChats = getChatIdsByUsername(username)

            imageChats.forEach {
                if(userChats.contains(it)){
                    true
                }
            }
            false
        }

        }
}

val dao = MainDao()
