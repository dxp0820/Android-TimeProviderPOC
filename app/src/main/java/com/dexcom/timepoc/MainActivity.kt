package com.dexcom.timepoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.dexcom.timepoc.ui.theme.TimePOCTheme

class MainActivity : ComponentActivity() {

    private val syncViewModel: SyncViewModel by viewModels { SyncViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TimePOCTheme {
                val dateState = remember { syncViewModel.timeState }
                val ntpDateState = remember {
                    syncViewModel.ntpTimeState
                }
                val syncState = remember {
                    syncViewModel.syncResult
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        date = dateState.value,
                        ntpDate = ntpDateState.value,
                        modifier = Modifier.padding(innerPadding),
                        syncResult = syncState.value,
                        sync = syncViewModel::sync
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    date: String,
    ntpDate: String,
    modifier: Modifier = Modifier,
    syncResult: Boolean,
    sync: () -> Unit = {}
) {

    Column(
        modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val textStyle = TextStyle(fontSize = TextUnit(24f, TextUnitType.Sp), color = Color.DarkGray)
        Text(
            text = "Device Time: $date", style = textStyle
        )
        Text(
            text = "Ntp Date: $ntpDate", style = textStyle
        )
        Spacer(modifier = Modifier.height(height = Dp(8f)))
        Button(onClick = { sync() }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Sync")
                Spacer(modifier = Modifier.width(Dp(4f)))
                Image(
                    modifier = Modifier.size(Dp(20f),Dp(20f)),
                    painter = painterResource(id = if (syncResult) R.drawable.green_tick else R.drawable.error_icon),
                    contentDescription = "Sync status icon"
                )
            }

        }

    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TimePOCTheme {
        Greeting(
            date = "4/5/24",
            ntpDate = "",
            syncResult = true
        )
    }
}