package com.yapp.gallery.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.yapp.gallery.domain.usecase.auth.GetValidTokenUseCase
import com.yapp.gallery.home.ui.home.HomeActivity
import com.yapp.gallery.login.ui.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var getValidTokenUseCase: GetValidTokenUseCase
    override fun onCreate(savedInstanceState: Bundle?) {
        //
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val content: View = findViewById(android.R.id.content)
//            content.viewTreeObserver.addOnPreDrawListener { false }
//        }

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            auth.currentUser?.let {
                getValidTokenUseCase()
                    .catch {
                        // 네트워크 오류 등으로 못 받아와질 때
                        delay(1000)
                        moveToHomeLogin(true, "")
                    }
                    .collectLatest {
                        moveToHomeLogin(true, it)
                    }
            } ?: run {
                delay(1000)
                moveToHomeLogin(false, "")
            }
        }
    }

    private fun moveToHomeLogin(isHome: Boolean, accessToken: String){
        finishAfterTransition()
        if (isHome) {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                if (accessToken.isNotEmpty()) putExtra("accessToken", accessToken)
            })
        }
        else startActivity(Intent(this, LoginActivity::class.java))
    }
}
