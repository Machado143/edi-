package com.exemplo.oficina.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exemplo.oficina.domain.model.StatusOS
import com.exemplo.oficina.ui.theme.StatusAbertaColor
import com.exemplo.oficina.ui.theme.isDarkScheme
import com.exemplo.oficina.ui.theme.StatusAndamentoColor
import com.exemplo.oficina.ui.theme.StatusCanceladaColor
import com.exemplo.oficina.ui.theme.StatusFinalizadaColor

/** Cor do conteúdo (texto/ícones) sobre a barra superior. */
@Composable
fun topBarContentColor(): Color =
    if (isDarkScheme()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary

/** Cores padrão das barras superiores, compartilhadas por todas as telas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appTopBarColors(): TopAppBarColors {
    val container = if (isDarkScheme()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
    val content = topBarContentColor()
    return TopAppBarDefaults.topAppBarColors(
        containerColor = container,
        titleContentColor = content,
        navigationIconContentColor = content,
        actionIconContentColor = content
    )
}

/** Card padrão: fundo de superfície, borda fina, sem sombra pesada. */
@Composable
fun appCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface
)

@Composable
fun appCardBorder() = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

@Composable
fun LoadingStateIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Composable
fun EmptyStateMessage(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun statusColor(status: String): Color = when (status) {
    StatusOS.ABERTA -> StatusAbertaColor
    StatusOS.EM_ANDAMENTO -> StatusAndamentoColor
    StatusOS.FINALIZADA -> StatusFinalizadaColor
    StatusOS.CANCELADA -> StatusCanceladaColor
    else -> MaterialTheme.colorScheme.outline
}

/** Badge com fundo translúcido e texto na cor do status (melhor contraste que fundo sólido + branco). */
@Composable
fun StatusBadgeComponent(status: String, modifier: Modifier = Modifier) {
    val color = statusColor(status)

    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Surface(color = color, shape = CircleShape, modifier = Modifier.size(6.dp)) {}
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = status,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatSummaryCard(
    title: String,
    value: String,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = appCardColors(),
        border = appCardBorder(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon?.let {
                Icon(imageVector = it, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Círculo com ícone usado nos cards de listagem e no dashboard. */
@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.14f),
        modifier = modifier.size(size.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size((size * 0.5f).dp)
            )
        }
    }
}
