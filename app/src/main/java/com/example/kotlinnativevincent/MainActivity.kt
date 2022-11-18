package com.example.kotlinnativevincent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.kotlinnativevincent.databinding.ActivityMainBinding
import com.example.kotlinnativevincent.fragments.FormInput
import com.example.kotlinnativevincent.fragments.ShowTripList

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(FormInput())
        binding.bottomNavigation.setOnItemSelectedListener {

            when(it.itemId){

                R.id.formInput -> replaceFragment(FormInput())
                R.id.showTripList -> replaceFragment(ShowTripList())
                else ->{}
            }
            true
        }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }

}