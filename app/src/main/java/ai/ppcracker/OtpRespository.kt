package ai.ppcracker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Properties
import javax.mail.*
import javax.mail.search.*

class OtpRepository(private val cfg: Cfg) {

    suspend fun latest(after: Long): String? = withContext(Dispatchers.IO) {
        val props = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.ssl.enable", "true")
            put("mail.imaps.timeout", "15000")
            put("mail.imaps.connectiontimeout", "15000")
        }
        val session = Session.getInstance(props)
        val store = session.getStore("imaps")
        store.connect(cfg.host, 993, cfg.user, cfg.pass)
        try {
            val inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)
            val hits = inbox.search(
                AndTerm(
                    FromStringTerm("paypal"),
                    ReceivedDateTerm(ComparisonTerm.GE, Date(after))
                )
            )
            hits.sortedByDescending { it.receivedDate.time }
                .forEach { msg ->
                    val body = textOf(msg).replace(Regex("<[^>]+>"), " ")
                    Regex("\\b(\\d{6})\\b").find(body)?.let { return@withContext it.value }
                }
            null
        } finally {
            runCatching { store.close() }
        }
    }

    private fun textOf(part: Part): String = buildString {
        when {
            part.isMimeType("text/plain") -> append(part.content as? String ?: "")
            part.isMimeType("multipart/*") -> {
                val mp = part.content as Multipart
                for (i in 0 until mp.count) append(textOf(mp.getBodyPart(i)))
            }
        }
    }
}
