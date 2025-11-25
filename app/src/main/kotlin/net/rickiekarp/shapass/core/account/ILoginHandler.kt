package net.rickiekarp.shapass.core.account

import net.rickiekarp.shapass.core.view.layout.LoginMaskLayout

interface ILoginHandler {
    fun setAppContextLoginBehaviour(loginMaskLayout: LoginMaskLayout)
    fun setOnLogout()
}
