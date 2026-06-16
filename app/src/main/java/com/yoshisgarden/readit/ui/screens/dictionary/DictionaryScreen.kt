package com.yoshisgarden.readit.ui.screens.dictionary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.yoshisgarden.readit.data.Categories
import com.yoshisgarden.readit.ui.components.CategoryFilterChip
import com.yoshisgarden.readit.ui.components.PhraseCard

@Composable
fun DictionaryScreen(
    vm: DictionaryViewModel,
    onOpenPhrase: (Long) -> Unit,
) {
    val phrases by vm.phrases.collectAsState()
    val filter by vm.filter.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = filter.query,
            onValueChange = vm::setQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                if (filter.query.isNotEmpty()) {
                    TextButton(onClick = {
                        vm.setQuery("")
                        keyboard?.hide()
                    }) {
                        Text("CLR")
                    }
                }
            },
            placeholder = { Text("英語・日本語・例文で検索") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            CategoryFilterChip(
                "すべて",
                selected = filter.category == null && !filter.favoritesOnly && filter.query.isBlank(),
            ) { vm.setCategory(null) }
            CategoryFilterChip(
                "★ お気に入り",
                selected = filter.favoritesOnly,
            ) { vm.setFavoritesOnly(true) }
            Categories.ALL.forEach { c ->
                CategoryFilterChip(c, selected = filter.category == c) { vm.setCategory(c) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "${phrases.size} 件",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
        LazyColumn(Modifier.fillMaxSize()) {
            items(phrases, key = { it.id }) { p ->
                PhraseCard(
                    english = p.english,
                    japanese = p.japanese,
                    category = p.category,
                    isFavorite = p.isFavorite,
                    onClick = { onOpenPhrase(p.id) },
                    onToggleFavorite = { vm.toggleFavorite(p.id) },
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
