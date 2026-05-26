package com.mobile.micasaestucasa.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests per [ImageStyle] e [ImageSize].
 *
 * Obiettivo: 100% branch coverage su tutto il sealed class.
 *
 * Nota tecnica: [androidx.compose.ui.unit.Dp] è disponibile nel sourceset
 * JVM di test grazie all'artifact `androidx.compose.ui:ui` incluso come
 * `implementation` e propagato al compile-classpath dei test unitari.
 * I valori Dp vengono confrontati tramite la loro proprietà `.value` (Float).
 */
class ImageStyleTest {

    // -------------------------------------------------------------------------
    // Helpers per confrontare Dp come Float (evita dipendenza da compose-ui test)
    // -------------------------------------------------------------------------

    private fun assertDpEquals(expected: Float, actual: androidx.compose.ui.unit.Dp) {
        assertEquals("Dp value", expected, actual.value, 0.001f)
    }

    // -------------------------------------------------------------------------
    // ImageSize — data class
    // -------------------------------------------------------------------------

    @Test
    fun imageSize_equality_sameValues() {
        val a = ImageSize(
            width  = androidx.compose.ui.unit.Dp(360f),
            height = androidx.compose.ui.unit.Dp(220f)
        )
        val b = ImageSize(
            width  = androidx.compose.ui.unit.Dp(360f),
            height = androidx.compose.ui.unit.Dp(220f)
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun imageSize_inequality_differentValues() {
        val a = ImageSize(androidx.compose.ui.unit.Dp(360f), androidx.compose.ui.unit.Dp(220f))
        val b = ImageSize(androidx.compose.ui.unit.Dp(100f), androidx.compose.ui.unit.Dp(100f))
        assertNotEquals(a, b)
    }

    @Test
    fun imageSize_copy_overridesHeight() {
        val original = ImageSize(androidx.compose.ui.unit.Dp(360f), androidx.compose.ui.unit.Dp(220f))
        val copy = original.copy(height = androidx.compose.ui.unit.Dp(999f))
        assertDpEquals(999f, copy.height)
        assertEquals(original.width, copy.width)
    }

    @Test
    fun imageSize_toString_containsClassName() {
        val size = ImageSize(androidx.compose.ui.unit.Dp(48f), androidx.compose.ui.unit.Dp(48f))
        assertTrue(size.toString().contains("ImageSize"))
    }

    // -------------------------------------------------------------------------
    // ImageStyle.PropertyCard
    // -------------------------------------------------------------------------

    @Test
    fun propertyCard_size_is360x220() {
        assertDpEquals(360f, ImageStyle.PropertyCard.size.width)
        assertDpEquals(220f, ImageStyle.PropertyCard.size.height)
    }

    @Test
    fun propertyCard_cornerPercent_is8() {
        assertEquals(8, ImageStyle.PropertyCard.cornerPercent)
    }

    @Test
    fun propertyCard_isInstanceOfImageStyle() {
        assertTrue(ImageStyle.PropertyCard is ImageStyle)
    }

    @Test
    fun propertyCard_dataObject_sameReference() {
        val a: ImageStyle = ImageStyle.PropertyCard
        val b: ImageStyle = ImageStyle.PropertyCard
        assertEquals(a, b)
    }

    // -------------------------------------------------------------------------
    // ImageStyle.PropertyThumbnail
    // -------------------------------------------------------------------------

    @Test
    fun propertyThumbnail_size_is120x80() {
        assertDpEquals(120f, ImageStyle.PropertyThumbnail.size.width)
        assertDpEquals(80f,  ImageStyle.PropertyThumbnail.size.height)
    }

    @Test
    fun propertyThumbnail_cornerPercent_is8() {
        assertEquals(8, ImageStyle.PropertyThumbnail.cornerPercent)
    }

    @Test
    fun propertyThumbnail_isInstanceOfImageStyle() {
        assertTrue(ImageStyle.PropertyThumbnail is ImageStyle)
    }

    @Test
    fun propertyThumbnail_equality_dataObject() {
        assertEquals(ImageStyle.PropertyThumbnail, ImageStyle.PropertyThumbnail)
    }

    // -------------------------------------------------------------------------
    // ImageStyle.UserAvatarMedium
    // -------------------------------------------------------------------------

    @Test
    fun userAvatarMedium_size_is48x48() {
        assertDpEquals(48f, ImageStyle.UserAvatarMedium.size.width)
        assertDpEquals(48f, ImageStyle.UserAvatarMedium.size.height)
    }

    @Test
    fun userAvatarMedium_cornerPercent_is50() {
        assertEquals(50, ImageStyle.UserAvatarMedium.cornerPercent)
    }

    @Test
    fun userAvatarMedium_isInstanceOfImageStyle() {
        assertTrue(ImageStyle.UserAvatarMedium is ImageStyle)
    }

    @Test
    fun userAvatarMedium_equality_dataObject() {
        assertEquals(ImageStyle.UserAvatarMedium, ImageStyle.UserAvatarMedium)
    }

    // -------------------------------------------------------------------------
    // ImageStyle.UserAvatarLarge
    // -------------------------------------------------------------------------

    @Test
    fun userAvatarLarge_size_is96x96() {
        assertDpEquals(96f, ImageStyle.UserAvatarLarge.size.width)
        assertDpEquals(96f, ImageStyle.UserAvatarLarge.size.height)
    }

    @Test
    fun userAvatarLarge_cornerPercent_is50() {
        assertEquals(50, ImageStyle.UserAvatarLarge.cornerPercent)
    }

    @Test
    fun userAvatarLarge_isInstanceOfImageStyle() {
        assertTrue(ImageStyle.UserAvatarLarge is ImageStyle)
    }

    @Test
    fun userAvatarLarge_equality_dataObject() {
        assertEquals(ImageStyle.UserAvatarLarge, ImageStyle.UserAvatarLarge)
    }

    // -------------------------------------------------------------------------
    // ImageStyle.Custom — data class
    // -------------------------------------------------------------------------

    @Test
    fun custom_storesProvidedValues() {
        val size = ImageSize(androidx.compose.ui.unit.Dp(200f), androidx.compose.ui.unit.Dp(150f))
        val style = ImageStyle.Custom(size = size, cornerPercent = 25)
        assertEquals(size, style.size)
        assertEquals(25, style.cornerPercent)
    }

    @Test
    fun custom_equality_sameFields() {
        val size = ImageSize(androidx.compose.ui.unit.Dp(200f), androidx.compose.ui.unit.Dp(150f))
        val a = ImageStyle.Custom(size, 25)
        val b = ImageStyle.Custom(size, 25)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun custom_inequality_differentCornerPercent() {
        val size = ImageSize(androidx.compose.ui.unit.Dp(200f), androidx.compose.ui.unit.Dp(150f))
        assertNotEquals(ImageStyle.Custom(size, 10), ImageStyle.Custom(size, 20))
    }

    @Test
    fun custom_inequality_differentSize() {
        val s1 = ImageSize(androidx.compose.ui.unit.Dp(100f), androidx.compose.ui.unit.Dp(100f))
        val s2 = ImageSize(androidx.compose.ui.unit.Dp(200f), androidx.compose.ui.unit.Dp(100f))
        assertNotEquals(ImageStyle.Custom(s1, 10), ImageStyle.Custom(s2, 10))
    }

    @Test
    fun custom_copy_overridesCornerPercent() {
        val size = ImageSize(androidx.compose.ui.unit.Dp(200f), androidx.compose.ui.unit.Dp(150f))
        val copy = ImageStyle.Custom(size, 25).copy(cornerPercent = 0)
        assertEquals(0, copy.cornerPercent)
        assertEquals(size, copy.size)
    }

    @Test
    fun custom_isInstanceOfImageStyle() {
        val style = ImageStyle.Custom(
            ImageSize(androidx.compose.ui.unit.Dp(100f), androidx.compose.ui.unit.Dp(100f)), 0
        )
        assertTrue(style is ImageStyle)
    }

    @Test
    fun custom_cornerPercent_zero_isValid() {
        val style = ImageStyle.Custom(
            ImageSize(androidx.compose.ui.unit.Dp(100f), androidx.compose.ui.unit.Dp(100f)),
            cornerPercent = 0
        )
        assertEquals(0, style.cornerPercent)
    }

    @Test
    fun custom_cornerPercent_50_isValid() {
        val style = ImageStyle.Custom(
            ImageSize(androidx.compose.ui.unit.Dp(100f), androidx.compose.ui.unit.Dp(100f)),
            cornerPercent = 50
        )
        assertEquals(50, style.cornerPercent)
    }

    // -------------------------------------------------------------------------
    // when-expression: verifica tutti i branch sealed in un unico test
    // -------------------------------------------------------------------------

    @Test
    fun whenExpression_coversAllSealedBranches() {
        val styles: List<ImageStyle> = listOf(
            ImageStyle.PropertyCard,
            ImageStyle.PropertyThumbnail,
            ImageStyle.UserAvatarMedium,
            ImageStyle.UserAvatarLarge,
            ImageStyle.Custom(
                ImageSize(androidx.compose.ui.unit.Dp(64f), androidx.compose.ui.unit.Dp(64f)),
                16
            )
        )

        val labels = styles.map { style ->
            when (style) {
                is ImageStyle.PropertyCard      -> "property_card"
                is ImageStyle.PropertyThumbnail -> "property_thumbnail"
                is ImageStyle.UserAvatarMedium  -> "avatar_medium"
                is ImageStyle.UserAvatarLarge   -> "avatar_large"
                is ImageStyle.Custom            -> "custom"
            }
        }

        assertEquals(
            listOf("property_card", "property_thumbnail", "avatar_medium", "avatar_large", "custom"),
            labels
        )
    }

    // -------------------------------------------------------------------------
    // Tutti gli stili sono distinti tra loro
    // -------------------------------------------------------------------------

    @Test
    fun allStyles_areDistinct() {
        val all: List<ImageStyle> = listOf(
            ImageStyle.PropertyCard,
            ImageStyle.PropertyThumbnail,
            ImageStyle.UserAvatarMedium,
            ImageStyle.UserAvatarLarge,
            ImageStyle.Custom(
                ImageSize(androidx.compose.ui.unit.Dp(1f), androidx.compose.ui.unit.Dp(1f)), 0
            )
        )
        for (i in all.indices) {
            for (j in all.indices) {
                if (i != j) {
                    assertNotEquals("Style[$i] == Style[$j]", all[i], all[j])
                }
            }
        }
    }
}
