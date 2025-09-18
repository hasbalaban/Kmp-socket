package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MbsIcon(mbs: Int, modifier: Modifier = Modifier)

@Composable
expect fun KingIcon(modifier: Modifier = Modifier)