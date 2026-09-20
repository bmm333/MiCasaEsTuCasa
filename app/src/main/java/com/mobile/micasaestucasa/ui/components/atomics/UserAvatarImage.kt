package com.mobile.micasaestucasa.ui.components.atomics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobile.micasaestucasa.ui.theme.ImageStyle

/**
 * Avatar utente di dimensione standard (navbar, commenti, liste).
 *
 * Delega il rendering a [CoreImage] con [com.mobile.micasaestucasa.ui.theme.ImageStyle.UserAvatarMedium].
 * Il cornerPercent = 50 produce un cerchio perfetto.
 *
 * @param imageUrl  URL dell'avatar remoto. Null → mostra immagine di errore/fallback.
 * @param userName  Nome utente usato come contentDescription per l'accessibilità.
 * @param modifier  Modifier opzionali iniettati dal parent.
 */
@Composable
fun UserAvatarImage(
    imageUrl: String?,
    userName: String,
    modifier: Modifier = Modifier
) {
    CoreImage(
        url = imageUrl,
        style = ImageStyle.UserAvatarMedium,
        contentDescription = "Avatar di $userName",
        modifier = modifier
    )
}

@Composable
fun UserAvatarLargeImage(
    imageUrl: String?,
    userName: String,
    modifier: Modifier = Modifier
) {
    CoreImage(
        url = imageUrl,
        style = ImageStyle.UserAvatarLarge,
        contentDescription = "Foto profilo di $userName",
        modifier = modifier
    )
}
