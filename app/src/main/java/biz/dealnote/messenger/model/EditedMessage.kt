package biz.dealnote.messenger.model

import biz.dealnote.messenger.upload.Upload

class EditedMessage(val message: Message) {

    var body: String? = message.body
    val attachments: MutableList<AttachmenEntry>

    init {
        val orig = message.attachments?.toList() ?: ArrayList()

        attachments = ArrayList()

        for (model in orig) {
            attachments.add(AttachmenEntry(true, model))
        }

        message.fwd?.run {
            attachments.add(AttachmenEntry(true, FwdMessages(this)))
        }
    }

    val canSave: Boolean
        get() {
            if(body.isNullOrBlank()){
                for(entry in attachments){
                    if(entry.attachment is Upload) continue

                    return true
                }

                return false
            } else{
                return true
            }
        }
}