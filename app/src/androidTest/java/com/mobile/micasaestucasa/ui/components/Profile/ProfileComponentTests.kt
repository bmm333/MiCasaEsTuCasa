package com.mobile.micasaestucasa.ui.components.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.util.Resource
import com.mobile.micasaestucasa.ui.screens.profile.ProfileContent
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import org.junit.Rule
import org.junit.Test

class ProfileComponentTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUser = User(
        id = "1",
        name = "Mario Rossi",
        email = "mario.rossi@example.com",
        roles = listOf(UserRole.GUEST),
        bio = "Bio di test",
        address = "Via Roma 1",
        phone = "123456"
    )

    @Test
    fun profileHeader_displaysCorrectData() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                ProfileHeader(
                    name = testUser.name,
                    bio = testUser.bio
                )
            }
        }

        composeTestRule.onNodeWithText(testUser.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(testUser.bio).assertIsDisplayed()
    }

    @Test
    fun personalInfoCard_displaysLegalNameAndEmail() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                PersonalInfoCard(
                    fullName = testUser.name,
                    email = testUser.email
                )
            }
        }

        composeTestRule.onNodeWithText(testUser.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(testUser.email).assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsLoadingIndicator() {
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                ProfileContent(
                    userState = Resource.Loading,
                    onLogout = {},
                    onNavigateToSettings = {}
                )
            }
        }

        // Verifica che il CircularProgressIndicator sia presente (tramite matcher generico o testTag se aggiunto)
        // In questo caso verifichiamo che non ci sia il testo dell'utente
        composeTestRule.onNodeWithText(testUser.name).assertDoesNotExist()
    }

    @Test
    fun logoutButton_triggersAction() {
        var logoutClicked = false
        composeTestRule.setContent {
            MiCasaEsTuCasaTheme {
                ProfileContent(
                    userState = Resource.Success(testUser),
                    onLogout = { logoutClicked = true },
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Sign Out").performClick()
        assert(logoutClicked)
    }
}
