package ai.ppcracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Bg = Color(0xFF08080A)
private val Accent = Color(0xFF33E06B)

class MainActivity : ComponentActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { Screen() } }
    }
}

@Composable
fun Screen(vm: MainViewModel = viewModel()) {
    var settings by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(Bg).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("pp cracker", color = Accent, fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold, fontSize = 20.sp)
            TextButton(onClick = { settings = true }) {
                Text("settings", color = Color.Gray, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = vm.number,
            onValueChange = { vm.number = it },
            label = { Text("target number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = vm::refresh,
            enabled = !vm.busy,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(88.dp)
        ) {
            Text(
                if (vm.busy) "SCANNING…" else "REFRESH",
                fontSize = 26.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(24.dp))

        vm.code?.let {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFF121417))) {
                Column(Modifier.padding(20.dp)) {
                    Text("your one time password is", color = Color.Gray,
                        fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("($it)", color = Accent, fontFamily = FontFamily.Monospace,
                        fontSize = 34.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        vm.status?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        }
    }

    if (settings) SettingsDialog(onDismiss = { settings = false })
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val p = remember { Prefs.store(ctx) }
    var host by remember { mutableStateOf(p.getString("host", "imap.gmail.com") ?: "") }
    var user by remember { mutableStateOf(p.getString("user", "") ?: "") }
    var pass by remember { mutableStateOf(p.getString("pass", "") ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                p.edit().putString("host", host.trim())
                    .putString("user", user.trim())
                    .putString("pass", pass.trim()).apply()
                onDismiss()
            }) { Text("save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("cancel") } },
        title = { Text("mailbox") },
        text = {
            Column {
                OutlinedTextField(host, { host = it }, label = { Text("imap host") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(user, { user = it }, label = { Text("email") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    pass, { pass = it }, label = { Text("app password") },
                    singleLine = true, visualTransformation = PasswordVisualTransformation()
                )
            }
        }
    )
}
