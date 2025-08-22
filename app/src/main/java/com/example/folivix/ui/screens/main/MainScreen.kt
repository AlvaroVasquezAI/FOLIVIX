package com.example.folivix.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.folivix.R
import com.example.folivix.model.User
import com.example.folivix.ui.components.FolivixUserAvatar
import com.example.folivix.ui.theme.FolivixBlack
import com.example.folivix.ui.theme.FolivixGreen
import com.example.folivix.ui.theme.FolivixWhite
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    currentUser: User
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(FolivixWhite)
                    .padding(top = 50.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                ) {
                    IconButton(onClick = { navController.navigate("info") }) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FolivixGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Information",
                                tint = FolivixWhite
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.folivix_logo),
                        contentDescription = "FOLIVIX Logo",
                        modifier = Modifier.width(150.dp),
                        contentScale = ContentScale.FillWidth
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    IconButton(onClick = { navController.navigate("user_profile") }) {
                        FolivixUserAvatar(
                            imageUri = currentUser.imageUri,
                            size = 40.dp
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.background(FolivixWhite)) {
                Divider(
                    color = Color.LightGray,
                    thickness = 0.75.dp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(FolivixWhite)
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomNavButton(
                            icon = R.drawable.ic_his,
                            label = "Historial",
                            isSelected = currentTab == MainTab.BACKGROUND,
                            onClick = { currentTab = MainTab.BACKGROUND }
                        )

                        CustomNavButton(
                            icon = R.drawable.ic_home,
                            label = "Inicio",
                            isSelected = currentTab == MainTab.HOME,
                            onClick = { currentTab = MainTab.HOME }
                        )

                        CustomNavButton(
                            icon = R.drawable.ic_identify,
                            label = "Identificar",
                            isSelected = currentTab == MainTab.IDENTIFY,
                            onClick = { currentTab = MainTab.IDENTIFY }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                MainTab.HOME -> HomeScreen(viewModel = hiltViewModel())
                MainTab.IDENTIFY -> IdentifyScreen(viewModel = hiltViewModel())
                MainTab.BACKGROUND -> BackgroundScreen(viewModel = hiltViewModel())
            }
        }
    }
}

@Composable
fun CustomNavButton(
    icon: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (isSelected) FolivixGreen else FolivixBlack
    val backgroundColor = if (isSelected) FolivixWhite else Color.Transparent

    Box(
        modifier = Modifier
            .size(width = 90.dp, height = 80.dp)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

enum class MainTab {
    HOME, IDENTIFY, BACKGROUND
}