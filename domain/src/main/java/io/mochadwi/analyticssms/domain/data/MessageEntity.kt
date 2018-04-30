package io.mochadwi.analyticssms.domain.data

/**
 * Created by mochadwi on 4/30/18.
 */
data class MessageEntity(
        val id: Int = 0,
        val from: String? = null,
        val subject: String? = null,
        val message: String? = null,
        val timestamp: String? = null,
        val picture: String? = null,
        val isImportant: Boolean = false,
        val isRead: Boolean = false,
        val color: Int = -1
) {
}