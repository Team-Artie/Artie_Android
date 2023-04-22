package com.yapp.gallery.saver

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.saver.databinding.ActivitySaverBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaverActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView<ActivitySaverBinding>(this, R.layout.activity_saver)
                .also {
                    it.lifecycleOwner = this
                    it.executePendingBindings()
                }

        val uri = intent.getParcelableExtra("uri") as? Uri
        val uris = intent.getParcelableArrayExtra("uris")
        val postId = intent.getLongExtra("postId", 0L)

        binding.composeView.setContent {
            if (!uris.isNullOrEmpty()) {
                ArtieTheme {
                    SaverView(
                        onRetryListener = {
                            finish()
                        },
                        saveToFile = {
                            val dialog = SaverDialog.getInstance(postId)

                            dialog.show(supportFragmentManager, "saverDialog")
                        },
                        uris = uris.filterIsInstance<Uri>()
                    )
                }
            }

            uri?.let {
                ArtieTheme {
                    SaverView(
                        uri = it,
                        onRetryListener = { finish() },
                        saveToFile = {
                            val dialog = SaverDialog.getInstance(postId)

                            dialog.show(supportFragmentManager, "saverDialog")
                        }
                    )
                }
            }
        }
    }
}
