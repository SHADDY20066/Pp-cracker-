package ai.ppcracker

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    var number by mutableStateOf("")
    var code by mutableStateOf<String?>(null)
    var status by mutableStateOf<String?>(null)
    var busy by mutableStateOf(false)

    fun refresh() = viewModelScope.launch {
        if (busy) return@launch
        busy = true; code = null; status = "checking inbox…"
        val p = Prefs.store(getApplication())
        val cfg = Cfg(
            p.getString("host", "") ?: "",
            p.getString("user", "") ?: "",
            p.getString("pass", "") ?: ""
        )
        if (cfg.user.isBlank()) {
            status = "no mailbox configured"; busy = false; return@launch
        }
        val window = System.currentTimeMillis() - 10 * 60_000L
        val found = runCatching { OtpRepository(cfg).latest(window) }
            .onFailure { status = it.message ?: "connection failed" }
            .getOrNull()
        when {
            found != null -> { code = found; status = null }
            status == "checking inbox…" -> status = "no fresh otp in inbox"
        }
        busy = false
    }
}
