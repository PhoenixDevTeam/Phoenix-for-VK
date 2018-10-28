package biz.dealnote.messenger.db.model

class MessagePatch(val messageId: Int) {
    var deletion: Deletion? = null
    var important: Important? = null

    class Deletion(val deleted: Boolean, val deletedForAll: Boolean)

    class Important(val important: Boolean)
}