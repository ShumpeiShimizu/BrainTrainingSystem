package com.oklb.braintrainingsystem

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.oklb.braintrainingsystem.fragments.BrainTrainingFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.mainFragmentArea, BrainTrainingFragment())
            commit()
        }
    }
}
