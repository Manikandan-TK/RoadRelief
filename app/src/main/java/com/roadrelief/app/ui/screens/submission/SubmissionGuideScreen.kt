package com.roadrelief.app.ui.screens.submission

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SubmissionGuideScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(text = "Submission Guide")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Here are the steps to submit your claim:")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "1. Gather all necessary documents, including your generated PDF report, photos, and any other supporting evidence.")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "2. Visit the official E-Daakhil portal to file your complaint. Make sure you have a stable internet connection.")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "3. Fill out the online form accurately. Attach all your documents as required.")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "4. Keep a record of your complaint number for future reference.")
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://edaakhil.nic.in/"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to E-Daakhil Portal")
        }
    }
}
