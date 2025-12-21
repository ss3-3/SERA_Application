package com.example.sera_application.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sera_application.R
import com.example.sera_application.utils.ImagePathUtils
import java.io.File

/**
 * Safely loads and displays an image from a local file path.
 * 
 * This composable handles:
 * - null or empty imagePath → shows default placeholder
 * - invalid imagePath → shows default placeholder
 * - valid local file path → loads image via Coil (file://)
 * 
 * @param imagePath The local file path to the image (nullable)
 * @param contentDescription Description for accessibility
 * @param modifier Modifier for the image container
 * @param contentScale How to scale the image
 * @param placeholderIcon Icon to show when image is not available (default: Image icon)
 * @param placeholderTint Tint color for placeholder icon
 */
@Composable
fun SafeImageLoader(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Image,
    placeholderTint: Color = Color.Gray
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            ImagePathUtils.isValidImagePath(imagePath) && ImagePathUtils.imageFileExists(imagePath) -> {
                // Valid local file path - load via Coil
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(imagePath!!))
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            }
            else -> {
                // Null, empty, invalid, or file doesn't exist - show placeholder
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    colorFilter = ColorFilter.tint(placeholderTint)
                )
            }
        }
    }
}

/**
 * Safely loads and displays a profile image from a local file path.
 * 
 * Similar to SafeImageLoader but uses Person icon as default placeholder.
 * 
 * @param imagePath The local file path to the profile image (nullable)
 * @param contentDescription Description for accessibility
 * @param modifier Modifier for the image container
 * @param contentScale How to scale the image
 */
@Composable
fun SafeProfileImageLoader(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    SafeImageLoader(
        imagePath = imagePath,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholderIcon = Icons.Default.Person,
        placeholderTint = Color.Gray
    )
}

