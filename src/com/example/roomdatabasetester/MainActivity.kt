package com.example.roomdatabasetester

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var database: TestDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch(Dispatchers.IO) {
            database = TestDatabase.getInstance(this@MainActivity)
            val appInfoDao = database.appInfoDao()

            val appInfoToInsert = AppInfo(
                packageName = "com.example.hello",
                versionCode = System.currentTimeMillis().toInt() / 1000
            )

            val previousAppInfo = database.withTransaction {
                appInfoDao.getAppInfo(appInfoToInsert.packageName)
                    .also { appInfoDao.updateOrInsert(appInfoToInsert) }
            }

            val toastText = previousAppInfo
                ?.let { "Inserted $appInfoToInsert. Previous app info: $it" }
                ?: "Inserted $appInfoToInsert. No previous app info."
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity as Activity, toastText, Toast.LENGTH_LONG).show()
            }
            database.close()
        }
    }
}