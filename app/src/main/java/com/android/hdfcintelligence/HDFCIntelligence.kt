package com.android.hdfcintelligence

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


@Composable
fun HdfcIntelligenceScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ... (Existing code for prompt and result)

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var uploadResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val selectedImage = remember { mutableIntStateOf(0) }
    val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFileUri = uri
    }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // ... (Existing prompt and result display code)

        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            Button(onClick = { launcher.launch("*/*") }, enabled = !isLoading) { // Allow any file type
                Text("Select File")
            }

            //Spacer(width = 8.dp) // Add some spacing

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        uploadResult = try {
                            selectedFileUri?.let { uri ->
                                val file = File(uri.path!!) // Correct way to get the file

                                val requestBody = file.asRequestBody(context.contentResolver.getType(uri)?.toMediaTypeOrNull()) // Get MIME type dynamically

                                val part = MultipartBody.Part.createFormData("file", file.name, requestBody) // "file" is the server's expected name

                                val response = uploadFile(part,context.contentResolver.getType(uri)?.toMediaTypeOrNull()) // Your Gemini API function
                                "Upload Successful: $response"

                               // bakingViewModel.sendPromptFile(part,context.contentResolver.getType(uri)?.toMediaTypeOrNull())
                            } ?: "No file selected"
                        } catch (e: Exception) {
                            "Upload Failed: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = selectedFileUri != null && !isLoading // Enable only if a file is selected and not loading
            ) {
                Text(text = "Upload File")
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        uploadResult?.let {
            Text(text = it, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}



// Placeholder - Replace with your actual Gemini API call
suspend fun uploadFile(part: MultipartBody.Part, toMediaTypeOrNull: MediaType?): String {

    // ... Your Gemini API interaction using the 'part'
    delay(2000) // Simulate network delay

    return "Simulated Gemini API Response" // Replace with actual response

}