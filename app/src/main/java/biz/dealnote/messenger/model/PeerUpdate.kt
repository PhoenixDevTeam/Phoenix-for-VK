package biz.dealnote.messenger.model

class PeerUpdate(val accountId: Int, val peerId: Int) {
    var readIn: Read? = null
    var readOut: Read? = null
    var lastMessage: LastMessage? = null
    var unread: Unread? = null

    class Read (val messageId: Int)

    class Unread(val count: Int)

    class LastMessage(val messageId: Int)
}