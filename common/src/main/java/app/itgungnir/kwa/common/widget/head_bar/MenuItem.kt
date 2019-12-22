package app.itgungnir.kwa.common.widget.head_bar

import app.itgungnir.kwa.common.widget.easy_adapter.ListItem

data class MenuItem(
    val iconRes: Int,
    val title: String,
    val callback: () -> Unit
) : ListItem