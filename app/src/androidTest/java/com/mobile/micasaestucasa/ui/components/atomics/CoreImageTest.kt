package com.mobile.micasaestucasa.ui.components.atomics

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.mobile.micasaestucasa.ui.theme.ImageSize
import com.mobile.micasaestucasa.ui.theme.ImageStyle
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented test per [CoreImage].
 *
 * Strategia di copertura:
 *  - url = null  → Coil mostra placeholder/errore, il nodo rimane comunque visualizzato.
 *  - url valido  → immagine caricata (test sincrono, nessuna rete: verifica solo il nodo).
 *  - Tutti gli [ImageStyle] predefiniti + [ImageStyle.Custom].
 *  - [ContentScale] di default (Crop) e override (Fit).
 *  - Modifier aggiuntivi (testTag) applicati dall'esterno.
 */
class CoreImageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -------------------------------------------------------------------------
    // url branches
    // -------------------------------------------------------------------------

    @Test
    fun coreImage_withNullUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.PropertyCard,
                    contentDescription = "null url test",
                    modifier = Modifier.testTag("core_image_null_url")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_null_url").assertIsDisplayed()
    }

    @Test
    fun coreImage_withValidUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
                    style = ImageStyle.PropertyCard,
                    contentDescription = "valid url test",
                    modifier = Modifier.testTag("core_image_valid_url")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_valid_url").assertIsDisplayed()
    }

    @Test
    fun coreImage_withEmptyUrl_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = "",
                    style = ImageStyle.PropertyCard,
                    contentDescription = "empty url test",
                    modifier = Modifier.testTag("core_image_empty_url")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_empty_url").assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // ImageStyle branches — uno per ogni data object + Custom
    // -------------------------------------------------------------------------

    @Test
    fun coreImage_style_propertyCard_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.PropertyCard,
                    contentDescription = "PropertyCard style",
                    modifier = Modifier.testTag("core_image_property_card")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_property_card").assertIsDisplayed()
    }

    @Test
    fun coreImage_style_propertyThumbnail_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.PropertyThumbnail,
                    contentDescription = "PropertyThumbnail style",
                    modifier = Modifier.testTag("core_image_property_thumbnail")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_property_thumbnail").assertIsDisplayed()
    }

    @Test
    fun coreImage_style_userAvatarMedium_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.UserAvatarMedium,
                    contentDescription = "UserAvatarMedium style",
                    modifier = Modifier.testTag("core_image_avatar_medium")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_avatar_medium").assertIsDisplayed()
    }

    @Test
    fun coreImage_style_userAvatarLarge_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.UserAvatarLarge,
                    contentDescription = "UserAvatarLarge style",
                    modifier = Modifier.testTag("core_image_avatar_large")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_avatar_large").assertIsDisplayed()
    }

    @Test
    fun coreImage_style_custom_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.Custom(
                        size = ImageSize(width = 80.dp, height = 80.dp),
                        cornerPercent = 20
                    ),
                    contentDescription = "Custom style",
                    modifier = Modifier.testTag("core_image_custom")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_custom").assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // contentScale branches
    // -------------------------------------------------------------------------

    @Test
    fun coreImage_defaultContentScale_crop_isDisplayed() {
        // Non passa contentScale → usa default ContentScale.Crop
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.PropertyCard,
                    contentDescription = "default scale",
                    modifier = Modifier.testTag("core_image_scale_default")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_scale_default").assertIsDisplayed()
    }

    @Test
    fun coreImage_contentScale_fit_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.PropertyCard,
                    contentDescription = "fit scale",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.testTag("core_image_scale_fit")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_scale_fit").assertIsDisplayed()
    }

    @Test
    fun coreImage_contentScale_inside_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.PropertyThumbnail,
                    contentDescription = "inside scale",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.testTag("core_image_scale_inside")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_scale_inside").assertIsDisplayed()
    }

    @Test
    fun coreImage_contentScale_fillBounds_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.UserAvatarMedium,
                    contentDescription = "fillBounds scale",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.testTag("core_image_scale_fill_bounds")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_scale_fill_bounds").assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // cornerPercent boundary — Custom con cornerPercent ai limiti
    // -------------------------------------------------------------------------

    @Test
    fun coreImage_customStyle_cornerPercent_zero_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.Custom(ImageSize(64.dp, 64.dp), cornerPercent = 0),
                    contentDescription = "corner 0",
                    modifier = Modifier.testTag("core_image_corner_0")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_corner_0").assertIsDisplayed()
    }

    @Test
    fun coreImage_customStyle_cornerPercent_50_isDisplayed() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.Custom(ImageSize(64.dp, 64.dp), cornerPercent = 50),
                    contentDescription = "corner 50",
                    modifier = Modifier.testTag("core_image_corner_50")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_corner_50").assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // Modifier passthrough
    // -------------------------------------------------------------------------

    @Test
    fun coreImage_withExternalModifier_isAccessibleViaTestTag() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                CoreImage(
                    url = null,
                    style = ImageStyle.PropertyCard,
                    contentDescription = "modifier test",
                    modifier = Modifier.testTag("core_image_external_modifier")
                )
            }
        }
        composeTestRule.onNodeWithTag("core_image_external_modifier").assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // Dp extension shim (usata nei test Custom — viene dagli import di Compose)
    // -------------------------------------------------------------------------
    private val Int.dp get() = androidx.compose.ui.unit.Dp(this.toFloat())
}
