package com.example.simsong

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.example.simsong.ui.theme.Primary
import com.example.simsong.ui.theme.SimsongTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

const val API_URL = "https://8168-114-122-72-205.ngrok-free.app/predict"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimsongTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "screen1",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("screen1") {
                            Screen1(navController = navController)
                        }
                        composable("screen2") {
                            Screen2(navController = navController)
                        }
                        composable(
                            "screen3?imageUri={imageUri}&prediction={prediction}",
                            arguments = listOf(
                                navArgument("imageUri") { type = NavType.StringType; nullable = true },
                                navArgument("prediction") { type = NavType.StringType; nullable = true }
                            )
                        ) { backStackEntry ->
                            Screen3(
                                navController = navController,
                                imageUri = backStackEntry.arguments?.getString("imageUri"),
                                prediction = backStackEntry.arguments?.getString("prediction")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Logo() {
    Text(
        text = "The Simsong",
        color = Primary,
        style = TextStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
        )
    )
}

@Composable
fun DisplayImageOrPlaceholder(imageUri: Uri?) {
    if (imageUri != null) {
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Uploaded Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(10.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color = Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No Image Available")
        }
    }
}

@Composable
fun Screen1(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Logo()
        Image(
            painter = painterResource(id = R.drawable.badut),
            modifier = Modifier
                .size(500.dp)
                .padding(bottom = 16.dp),
            contentDescription = "badut"
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = { navController.navigate("screen2") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Text(
                text = "Lanjutkan",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
    }
}

@Composable
fun Screen2(navController: NavController) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var prediction by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Tebak Karakter",
            color = Primary,
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )
        )
        Spacer(modifier = Modifier.height(45.dp))
        Text(
            text = "Pilih gambar karakter the simpsons yang ingin ditebak",
            style = TextStyle(
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(15.dp))
        DisplayImageOrPlaceholder(imageUri)
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Text(
                text = "Pilih Gambar",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
        Button(
            onClick = { /* TODO: Implement camera functionality */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false, // Disabled until implemented
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Text(
                text = "Buka Kamera",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
        if (imageUri != null) {
            Spacer(modifier = Modifier.height(15.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val result = uploadImageAndPredict(context, imageUri)
                            prediction = result
                            navController.navigate(
                                "screen3?imageUri=${Uri.encode(imageUri.toString())}&prediction=${Uri.encode(prediction)}"
                            )
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(
                        text = "Kirim",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }
        }
    }
}

suspend fun uploadImageAndPredict(context: Context, imageUri: Uri?): String {
    if (imageUri == null) return "No image selected"

    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext "Failed to read image"

            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.name, requestBody)
                .build()

            val request = Request.Builder()
                .url(API_URL)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                jsonResponse.getString("predicted_class")
            } else {
                "Prediction Failed: ${response.message}"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}

@Composable
fun Screen3(navController: NavController, imageUri: String?, prediction: String?) {
    val parsedUri = imageUri?.let { Uri.parse(it) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Hasil Tebakan",
            color = Primary,
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (parsedUri != null) {
            Image(
                painter = rememberAsyncImagePainter(parsedUri),
                contentDescription = "Uploaded Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = prediction ?: "Prediction not available",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { navController.popBackStack("screen2", inclusive = false) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text(
                text = "Kembali",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Screen1Preview() {
    SimsongTheme {
        Screen1(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun Screen2Preview() {
    SimsongTheme {
        Screen2(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun Screen3Preview() {
    SimsongTheme {
        Screen3(
            navController = rememberNavController(),
            imageUri = null,
            prediction = "Homer Simpson"
        )
    }
}