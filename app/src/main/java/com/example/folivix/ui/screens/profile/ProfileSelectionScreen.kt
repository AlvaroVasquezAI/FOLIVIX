package com.example.folivix.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.folivix.R
import com.example.folivix.model.User
import com.example.folivix.ui.components.FolivixButton
import com.example.folivix.ui.components.FolivixUserAvatar

@Composable
fun ProfileSelectionScreen(
    profiles: List<User>,
    onProfileSelected: (User) -> Unit,
    onCreateProfile: () -> Unit,
    onManageProfiles: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.folivix_logo),
            contentDescription = "FOLIVIX Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
                .width(150.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "¿Quién está analizando ahora?",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                profiles.forEach { user ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable { onProfileSelected(user) }
                    ) {
                        FolivixUserAvatar(
                            imageUri = user.imageUri,
                            size = when (profiles.size) {
                                1 -> 120.dp
                                2 -> 100.dp
                                else -> 80.dp
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 48.dp)
        ) {
            if (profiles.size < 3) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FolivixButton(
                        text = "¿Aún no eres analista?",
                        onClick = onCreateProfile,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FolivixButton(
                        text = "Administrar Perfiles",
                        onClick = onManageProfiles,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                    )
                }
            }
        }
    }
}