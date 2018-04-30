package io.mochadwi.analyticssms.data.network

/**
 * Created by mochadwi on 4/30/18.
 */
data class MessageEntity(
        var id: Int = 0,
        var from: String? = null,
        var subject: String? = null,
        var message: String? = null,
        var timestamp: String? = null,
        var picture: String? = null,
        var isImportant: Boolean = false,
        var isRead: Boolean = false,
        var color: Int = -1
) {
}