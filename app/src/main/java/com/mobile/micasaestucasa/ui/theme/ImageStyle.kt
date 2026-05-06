import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unica fonte di verità per le regole visive delle immagini nell'app.
 *
 * Aggiungere una nuova tipologia significa aggiungere un solo oggetto qui —
 * nessuna modifica al core, nessun nuovo file di componente dedicato.
 *
 * @property size  Dimensioni target del componente (larghezza × altezza).
 * @property cornerPercent  Percentuale di arrotondamento angoli [0..50].
 *                          50 → cerchio perfetto per avatar; 0 → rettangolo netto.
 */
sealed class ImageStyle(
    open val size: ImageSize,
    open val cornerPercent: Int
) {


    /** Card principale nel listing: immagine hero grande, angoli leggermente arrotondati. */
    data object PropertyCard : ImageStyle(
        size = ImageSize(width = 360.dp, height = 220.dp),
        cornerPercent = 8
    )

    /** Thumbnail nella lista ricerca: compatta, stesso arrotondamento della card. */
    data object PropertyThumbnail : ImageStyle(
        size = ImageSize(width = 120.dp, height = 80.dp),
        cornerPercent = 8
    )


    /** Avatar standard nella navbar o nei commenti: quadrato medio → cerchio. */
    data object UserAvatarMedium : ImageStyle(
        size = ImageSize(width = 48.dp, height = 48.dp),
        cornerPercent = 50
    )

    /** Avatar grande nel profilo utente. */
    data object UserAvatarLarge : ImageStyle(
        size = ImageSize(width = 96.dp, height = 96.dp),
        cornerPercent = 50
    )



    /**
     * Per i casi dove nessuno stile predefinito è adatto.
     * Usare con parsimonia: preferire sempre una variante sealed.
     */
    data class Custom(
        override val size: ImageSize,
        override val cornerPercent: Int
    ) : ImageStyle(size, cornerPercent)
}

/**
 * Wrapper tipizzato per le dimensioni, evita l'inversione accidentale di width/height.
 */
data class ImageSize(val width: Dp, val height: Dp)