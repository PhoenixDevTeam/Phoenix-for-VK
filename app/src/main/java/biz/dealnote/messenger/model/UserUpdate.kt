package biz.dealnote.messenger.model

/**
 * Created by Ruslan Kolbasa on 18.09.2017.
 * phoenix
 */
class UserUpdate(val accountId: Int, val userId: Int) {

    var status: Status? = null

    var online: Online? = null

    class Online(val isOnline: Boolean, val lastSeen: Long, val platform: Int)

    class Status(val status: String)
}