package com.uw.mpvDroid.di

import com.uw.mpvDroid.ui.custombuttons.CustomButtonsScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ViewModelModule = module {
  viewModelOf(::CustomButtonsScreenViewModel)
}
