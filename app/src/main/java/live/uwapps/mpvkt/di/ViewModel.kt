package live.uwapps.mpvkt.di

import live.uwapps.mpvkt.ui.custombuttons.CustomButtonsScreenViewModel
import live.uwapps.mpvkt.presentation.library.LibraryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ViewModelModule = module {
  viewModelOf(::CustomButtonsScreenViewModel)
  viewModelOf(::LibraryViewModel)
}
