package com.mobile.micasaestucasa.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mobile.micasaestucasa.domain.model.user.User
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.util.Resource
import com.mobile.micasaestucasa.ui.components.nav.DefaultBottomNavItems
import com.mobile.micasaestucasa.ui.components.nav.MiCasaBottomNav
import com.mobile.micasaestucasa.ui.components.profile.HostBanner
import com.mobile.micasaestucasa.ui.components.profile.PersonalInfoCard
import com.mobile.micasaestucasa.ui.components.profile.ProfileHeader
import com.mobile.micasaestucasa.ui.components.profile.ProfileSectionCard
import com.mobile.micasaestucasa.ui.components.profile.SettingsRow
import com.mobile.micasaestucasa.ui.components.profile.WishlistCard
import com.mobile.micasaestucasa.ui.theme.MiCasaEsTuCasaTheme
import com.mobile.micasaestucasa.ui.viewmodels.auth.AuthViewModel
import com.mobile.micasaestucasa.ui.viewmodels.user.UserViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToSettings: (String) -> Unit = {},
    onLogoutNavigate: () -> Unit = {},
    onNavigateBack: () -> Boolean
) {
    // Osservazione corretta dello stato utente e autenticazione
    val userState by userViewModel.userState.collectAsStateWithLifecycle()
    val isAuthSuccessful by authViewModel.isAuthSuccessful.collectAsStateWithLifecycle()

    // Effetto per il logout: naviga via se la sessione non è più valida
    LaunchedEffect(isAuthSuccessful) {
        if (!isAuthSuccessful) {
            onLogoutNavigate()
        }
    }

    ProfileContent(
        userState = userState,
        navController = navController,
        onLogout = { authViewModel.logout() },
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
fun ProfileContent(
    userState: Resource<User?>,
    navController: NavController,
    onLogout: () -> Unit,
    onNavigateToSettings: (String) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        bottomBar = {
            MiCasaBottomNav(
                items = DefaultBottomNavItems.items,
                selectedRoute = "profile_screen",
                onItemSelected = { /* TODO: hook up navigation */ }
            )
        }
    ) { paddingValues ->
        when (userState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = userState.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Retry Login")
                        }
                    }
                }
            }
            is Resource.Success -> {
                val user = userState.data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ProfileHeader(
                            name = user?.name ?: "Guest",
                            memberSince = "2024",
                            bio = user?.bio ?: "Amo viaggiare e scoprire posti nuovi!",
                            imageUrl = user?.profileImageUrl
                        )
                    }

                    item {
                        PersonalInfoCard(
                            fullName = user?.name ?: "",
                            email = user?.email ?: "",
                            phone = user?.phone?.takeIf { it.isNotEmpty() } ?: "+39 333 1234567",
                            address = user?.address?.takeIf { it.isNotEmpty() } ?: "Via Roma 123, Milano"
                        )
                    }

                    if (user?.roles?.contains(UserRole.OWNER) == false) {
                        item {
                            PaddingWrapper {
                                HostBanner()
                            }
                        }
                    }

                    item {
                        WishlistCard(count = 5)
                    }

                    item {
                        ProfileSectionCard(
                            title = "Settings",
                            icon = Icons.Default.Settings
                        ) {
                            Column {
                                SettingsRow(
                                    icon = Icons.Default.Notifications,
                                    label = "Notifications",
                                    onClick = { onNavigateToSettings("notifications") }
                                )
                                SettingsRow(
                                    icon = Icons.Default.PrivacyTip,
                                    label = "Privacy",
                                    onClick = { onNavigateToSettings("privacy") }
                                )
                            }
                        }
                    }

                    item {
                        PaddingWrapper {
                            Button(
                                onClick = onLogout,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Sign Out", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaddingWrapper(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val dummyNavController = rememberNavController()
    MiCasaEsTuCasaTheme {
        ProfileContent(
            userState = Resource.Success(
                User(
                    id = "1",
                    name = "Mario Rossi",
                    email = "mario.rossi@example.com",
                    roles = listOf(UserRole.GUEST),
                    bio = "Amo viaggiare e scoprire posti nuovi!",
                    profileImageUrl = null,
                    address = "Via Roma 123, Milano",
                    phone = "+39 333 1234567"
                )
            ),
            navController = dummyNavController,
            onLogout = {},
            onNavigateToSettings = {}
        )
    }
}
