package com.dagimg.expensms.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.dagimg.expensms.R
import com.dagimg.expensms.ui.theme.AppTheme
import com.dagimg.expensms.ui.theme.LocalAppTheme
import com.dagimg.expensms.ui.theme.ThemeSwitcherColors
import kotlinx.coroutines.launch

@Composable
fun ThemeToggleButton(
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentTheme = LocalAppTheme.current
    val isDarkTheme = currentTheme == AppTheme.DARK

    DarkModeSwitch(
        checked = isDarkTheme,
        modifier = modifier,
        onCheckedChanged = { checked ->
            onThemeChange(if (checked) "dark" else "light")
        },
    )
}

@Composable
private fun DarkModeSwitch(
    checked: Boolean,
    modifier: Modifier,
    onCheckedChanged: (Boolean) -> Unit,
) {
    val switchWidth = 80.dp
    val switchHeight = 40.dp
    val handleSize = 32.dp
    val handlePadding = 4.dp

    val valueToOffset = if (checked) 1f else 0f
    val offset = remember { Animatable(valueToOffset) }
    val scope = rememberCoroutineScope()

    DisposableEffect(checked) {
        if (offset.targetValue != valueToOffset) {
            scope.launch {
                offset.animateTo(valueToOffset, animationSpec = tween(1000))
            }
        }
        onDispose { }
    }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier =
            modifier
                .width(switchWidth)
                .height(switchHeight)
                .clip(RoundedCornerShape(switchHeight))
                .background(
                    androidx.compose.ui.graphics.lerp(
                        ThemeSwitcherColors.BlueSky,
                        ThemeSwitcherColors.NightSky,
                        offset.value,
                    ),
                ).border(2.dp, ThemeSwitcherColors.BorderColor, RoundedCornerShape(switchHeight))
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChanged,
                    role = Role.Switch,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
    ) {
        val backgroundPainter = painterResource(R.drawable.background)
        Canvas(modifier = Modifier.fillMaxSize()) {
            with(backgroundPainter) {
                val scale = size.width / intrinsicSize.width
                val scaledHeight = intrinsicSize.height * scale
                translate(top = (size.height - scaledHeight) * (1f - offset.value)) {
                    draw(Size(size.width, scaledHeight))
                }
            }
        }

        Image(
            painter = painterResource(R.drawable.glow),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(switchWidth * 1.2f)
                    .graphicsLayer {
                        scaleX = 1.2f
                        scaleY = scaleX
                        translationX =
                            lerp(
                                -size.width * 0.5f + handlePadding.toPx() + handleSize.toPx() * 0.5f,
                                switchWidth.toPx() -
                                    size.width * 0.5f -
                                    handlePadding.toPx() -
                                    handleSize.toPx() * 0.5f,
                                offset.value,
                            )
                    },
        )

        Box(
            modifier =
                Modifier
                    .padding(horizontal = handlePadding)
                    .size(handleSize)
                    .offset(x = (switchWidth - handleSize - handlePadding * 2f) * offset.value)
                    .paint(painterResource(R.drawable.sun))
                    .clip(CircleShape),
        ) {
            Image(
                painter = painterResource(R.drawable.moon),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(handleSize)
                        .graphicsLayer {
                            translationX = size.width * (1f - offset.value)
                        },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeToggleButtonPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        ThemeToggleButton(onThemeChange = {})
    }
}
