package com.mobile.micasaestucasa.ui.components.atomics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobile.micasaestucasa.ui.theme.ImageStyle


/**
 * Immagine hero di una proprietà immobiliare.
 *
 * Questo composable conosce solo il dominio (una proprietà ha un'immagine) —
 * non sa nulla di rete, cache o Coil. Delega tutto a [CoreImage].
 *
 * @param imageUrl  URL dell'immagine della proprietà (può essere null: mostra l'errore).
 * @param modifier  Modifier opzionali per layout aggiuntivi dal parent (padding, ecc.).
 */
@Composable
fun PropertyImage(
    imageUrl : String?,
    modifier: Modifier= Modifier
){
    CoreImage(
        url=imageUrl,
        style= ImageStyle.PropertyCard,
        contentDescription="Immagine della propietà",
        modifier=modifier
    )
}

@Composable
fun PropertyThumbnailImage(
    imageUrl:String?,
    modifier: Modifier= Modifier
){
    CoreImage(
        url=imageUrl,
        style= ImageStyle.PropertyThumbnail,
        contentDescription="Immagine della propietà",
        modifier=modifier
    )
}