package net.rickiekarp.core.account

import net.rickiekarp.core.view.layout.LoginMaskLayout

interface ILoginHandler {
    fun setAppContextLoginBehaviour(loginMaskLayout: LoginMaskLayout)
    fun setOnLogout()
}
