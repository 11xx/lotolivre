package org.x11xx.loterias.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.x11xx.loterias.R
import org.x11xx.loterias.ui.icons.BackIcon
import org.x11xx.loterias.ui.openRepository
import org.x11xx.loterias.ui.theme.LocalDarkTheme
import org.x11xx.loterias.ui.theme.LotoLivreLogo

@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(BackIcon, contentDescription = stringResource(R.string.action_back))
            }
            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Identity()
            Text(
                text = stringResource(R.string.about_purpose),
                style = MaterialTheme.typography.bodyLarge,
            )
            Section(R.string.about_not_official_title, R.string.about_not_official)
            Section(R.string.about_trademarks_title, R.string.about_trademarks)
            Section(R.string.about_source_title, R.string.about_source)
            Section(R.string.about_license_title, R.string.about_license)
            Repository()
        }
    }
}

@Composable
private fun Identity() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LotoLivreLogo(
            size = 64.dp,
            dark = LocalDarkTheme.current,
            contentDescription = stringResource(R.string.app_name),
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
        )
    }
}

@Composable
private fun Repository() {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.about_repository_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.about_repository),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(
                onClickLabel = stringResource(R.string.action_open_repository),
            ) { context.openRepository() },
        )
    }
}

@Composable
private fun Section(title: Int, body: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = stringResource(title), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
