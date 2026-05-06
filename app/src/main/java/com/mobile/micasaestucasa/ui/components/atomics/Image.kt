package com.mobile.micasaestucasa.ui.components.atomics

import ImageStyle
import android.R.attr.contentDescription

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage



/**
 * Componente core per il recupero e rendering asincrono di immagini remote.
 *
 * Responsabilità di questo composable:
 *  - Eseguire la richiesta di rete tramite Coil (usa il singleton configurato in [AppImageLoader])
 *  - Applicare dimensioni e forma passate tramite [style]
 *  - Mostrare placeholder e stato di errore
 *
 * Non contiene dimensioni hardcoded: ogni regola visiva arriva dall'esterno.
 *
 * @param url            URL remoto dell'immagine da caricare.
 * @param style          Regole visive (dimensioni + corner radius) dall'[ImageStyle].
 * @param contentDescription  Descrizione per l'accessibilità (a11y). Null → decorativa.
 * @param modifier       Modifier aggiuntivi iniettati dalla componente padre.
 * @param contentScale   Modalità di scala dell'immagine. Default: [ContentScale.Crop].
 */
@Composable
fun Image (
    url : String?,
    style: ImageStyle,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
){
    val shape = RoundedCornerShape(percent=style.cornerPercent)

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
            .size(
                width  = style.size.width,
                height = style.size.height
            )
            .clip(shape)
    )
}