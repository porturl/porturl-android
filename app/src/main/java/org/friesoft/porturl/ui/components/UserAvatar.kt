package org.friesoft.porturl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import org.friesoft.porturl.R
import org.friesoft.porturl.client.model.User

@Composable
fun UserAvatar(currentUser: User?, backendUrl: String, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 32.dp) {
    val imageUrl = currentUser?.imageUrl
    val fullImageUrl = when {
        imageUrl.isNullOrBlank() -> null
        imageUrl.startsWith("http") -> imageUrl
        else -> "${backendUrl.trimEnd('/')}/${imageUrl.trimStart('/')}"
    }
    
    val initials = currentUser?.email?.take(1)?.uppercase() ?: ""
    val borderThickness = 1.5.dp

    val fallback = @Composable {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(borderThickness, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    style = if (size >= 40.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = stringResource(R.string.user_profile),
                    modifier = Modifier.size(if (size >= 40.dp) 24.dp else 20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    if (fullImageUrl != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(fullImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.user_profile),
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(borderThickness, MaterialTheme.colorScheme.outline, CircleShape),
            loading = { fallback() },
            error = { fallback() }
        )
    } else {
        fallback()
    }
}
