package biz.dealnote.messenger.model

class PeerUpdate(val accountId: Int, val peerId: Int) {
    var readIn: Read? = null
    var readOut: Read? = null
    var lastMessage: LastMessage? = null

    class Read (val messageId: Int, val unreadCount: Int)

    class LastMessage(val messageId: Int)
}