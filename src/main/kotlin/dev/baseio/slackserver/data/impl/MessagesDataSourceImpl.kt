package dev.baseio.slackserver.data.impl

import com.mongodb.client.model.Filters
import com.mongodb.client.model.changestream.OperationType
import dev.baseio.slackdata.protos.SKWorkspaceChannelRequest
import dev.baseio.slackserver.data.sources.MessagesDataSource
import dev.baseio.slackserver.data.models.SkMessage
import dev.baseio.slackserver.data.models.SkUser
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.match

class MessagesDataSourceImpl(private val slackCloneDB: CoroutineDatabase) : MessagesDataSource {

    override suspend fun getMessage(uuid: String, workspaceId: String): SkMessage? {
        return slackCloneDB.getCollection<SkMessage>()
            .findOne(SkMessage::uuid eq uuid, SkUser::workspaceId eq workspaceId)
    }

    override suspend fun updateMessage(request: SkMessage): SkMessage? {
        slackCloneDB.getCollection<SkMessage>()
            .updateOne(SkMessage::uuid eq request.uuid, request)
        return getMessage(request.uuid,request.workspaceId)
    }

    override fun registerForChanges(request: SKWorkspaceChannelRequest): Flow<Pair<SkMessage?, SkMessage?>> {
        val collection = slackCloneDB.getCollection<SkMessage>()

        val pipeline: List<Bson> = listOf(
            match(
                Document.parse("{'fullDocument.workspaceId': '${request.workspaceId}'}"),
                Document.parse("{'fullDocument.channelId': '${request.channelId}'}"),
                Filters.`in`("operationType", OperationType.values().map { it.value }.toList())
            )
        )

        return collection
            .watch<SkMessage>(pipeline).toFlow().map {
                Pair(it.fullDocumentBeforeChange, it.fullDocument)
            }
    }

    override suspend fun saveMessage(request: SkMessage): SkMessage {
        val collection = slackCloneDB.getCollection<SkMessage>()
        collection.insertOne(request)
        return collection.findOne(SkMessage::uuid eq request.uuid) ?: throw StatusException(Status.CANCELLED)
    }

    override suspend fun getMessages(workspaceId: String, channelId: String): List<SkMessage> {
        val collection = slackCloneDB.getCollection<SkMessage>()
        return collection.find(SkMessage::workspaceId eq workspaceId, SkMessage::channelId eq channelId)
            .toList()
    }
}