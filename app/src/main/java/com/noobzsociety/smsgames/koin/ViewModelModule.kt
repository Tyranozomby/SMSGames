package com.noobzsociety.smsgames.koin

import com.noobzsociety.smsgames.ui.screens.files.FilesViewModel
import com.noobzsociety.smsgames.ui.screens.gamemodes.details.GamemodeDetailsViewModel
import com.noobzsociety.smsgames.ui.screens.gamemodes.list.GamemodeListViewModel
import com.noobzsociety.smsgames.ui.screens.games.list.GameListViewModel
import com.noobzsociety.smsgames.ui.screens.init.userinfos.UserInfosViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel {
        UserInfosViewModel(get(), get())
    }

    viewModel {
        GamemodeListViewModel(get())
    }

    viewModel {
        GamemodeDetailsViewModel(get(), get())
    }

    viewModel {
        GameListViewModel(get())
    }

    viewModel {
        FilesViewModel(get())
    }
}