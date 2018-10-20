package biz.dealnote.messenger.model

class EditedMessage(val message: Message){

    var body: String? = message.body
    val attachments: MutableList<AttachmenEntry>

    init {
        val orig = message.attachments?.toList() ?: ArrayList()

        attachments = ArrayList()

        for(model in orig){
            attachments.add(AttachmenEntry(true, model))
        }

        message.fwd?.run {
            attachments.add(AttachmenEntry(true, FwdMessages(this)))
        }
    }
}