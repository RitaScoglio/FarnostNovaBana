package sk.farnost.NovaBana

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import sk.farnost.NovaBana.databinding.MainActivityBinding
import sk.farnost.NovaBana.massInformation.MassInformationFragment
import sk.farnost.NovaBana.massInformation.MassInformationViewModel
import sk.farnost.NovaBana.news.NewsFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.initiateFirebase(getString(R.string.FirebaseURL))

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, NewsFragment())
                .commit()
        }
        setBottomNavigation()
        viewModel.getAvailableMassInformation(this)
    }

    private fun setBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_news -> {
                    changeFragment(NewsFragment())
                    true
                }
                R.id.menu_mass_information -> {
                    changeFragment(MassInformationFragment())
                    true
                }
                R.id.menu_day_thought -> {
                    //changeFragment(DayThoughtFragment())
                    true
                }
                R.id.menu_calendar -> {
                    //changeFragment(CalendarFragment())
                    true
                }
                R.id.menu_contact -> {
                    //changeFragment(ContactFragment())
                    true
                }
                else -> true
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
            .replace(R.id.frame_layout, fragment)
            .setReorderingAllowed(true)
            .commit()
    }
}