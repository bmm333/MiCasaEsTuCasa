package com.mobile.micasaestucasa.ui.components.atomics

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mobile.micasaestucasa.ui.theme.AppShapes
import com.mobile.micasaestucasa.ui.theme.IconSize
import com.mobile.micasaestucasa.ui.theme.Primario
import com.mobile.micasaestucasa.ui.theme.Secondary
import com.mobile.micasaestucasa.ui.theme.Spacing
import com.mobile.micasaestucasa.ui.theme.Typography

object HomeAtomics {
    val paddingCard = 16.dp
    val paddingSection = 24.dp
    val internalGap = 8.dp
    val cardShape = RoundedCornerShape(20.dp)
    val chipShape = RoundedCornerShape(16.dp)
    val buttonShape = RoundedCornerShape(12.dp)
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 1000
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Shimmer loading animation"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation.value - widthOfShadowBrush, y = 0.0f),
        end = Offset(x = translateAnimation.value, y = angleOfAxisY)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush)
    )
}

@Composable
fun AppAvatar(
    imageRes: Int,
    modifier: Modifier = Modifier,
    size: Dp = IconSize.Avatar,
    showBorder: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBorder) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primaryContainer, AppShapes.Avatar)
                } else {
                    Modifier
                }
            )
            .padding(if (showBorder) 2.dp else 0.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(AppShapes.Avatar)
        )
    }
}

@Composable
fun AppAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = IconSize.Avatar,
    showBorder: Boolean = false,
    placeholderRes: Int = android.R.drawable.ic_menu_gallery
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBorder) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primaryContainer, AppShapes.Avatar)
                } else {
                    Modifier
                }
            )
            .padding(if (showBorder) 2.dp else 0.dp)
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(AppShapes.Avatar)
            )
        } else {
            Image(
                painter = painterResource(id = placeholderRes),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(AppShapes.Avatar)
            )
        }
    }
}

@Composable
fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), AppShapes.Badge)
            .padding(horizontal = Spacing.Small, vertical = Spacing.ExtraSmall)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Primario,
            modifier = Modifier.size(IconSize.Small)
        )
        Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
        Text(
            text = "%.1f".format(rating),
            style = Typography.bodyLarge,
            color = Primario,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Secondary,
    contentColor: Color = Color.White
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = CircleShape
    ) {
        Text(
            text = text.uppercase(),
            style = Typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = Primario
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = HomeAtomics.buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = baseColor
        ),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(baseColor)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.outline) },
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, contentDescription = null, tint = Primario) }
        } else {
            null
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = HomeAtomics.buttonShape
    )
}

@Composable
fun CollectionCategoryItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(80.dp)
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = HomeAtomics.chipShape,
            color = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            tonalElevation = 2.dp,
            onClick = onClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(32.dp).scale(if (isSelected) 1.1f else 1f),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        Secondary
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(HomeAtomics.internalGap))
        Text(
            text = label.uppercase(),
            style = Typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = 1.sp
        )
    }
}
