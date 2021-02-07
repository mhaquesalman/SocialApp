package com.salman.socialapp.ui.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.salman.socialapp.R
import com.salman.socialapp.ui.fragments.MessageFragment
import com.salman.socialapp.ui.fragments.RecentFragment
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val typefaceSpan = TypefaceSpan(resources.getFont(R.font.aclonica))
        val title = SpannableString("SocialApp")
        title.setSpan(typefaceSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        supportActionBar?.setTitle(title)
//        supportActionBar?.setTitle("Messages")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initialization()
    }

    private fun initialization() {
        // Tab Layout and viewpager
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(MessageFragment.getInstance(), "Friends")
        viewPagerAdapter.addFragment(RecentFragment.getInstance(), "Recents")
        view_pager.adapter = viewPagerAdapter

        // attach tablayout with viewpager
        tab_layout.setupWithViewPager(view_pager)
        tab_layout.getTabAt(0)?.setIcon(R.drawable.ic_friends)

    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    inner class ViewPagerAdapter(fm: FragmentManager, behavior: Int = 0) : FragmentPagerAdapter(fm, behavior) {

        private var fragments: ArrayList<Fragment> = ArrayList()
        private var titles: ArrayList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

    }
}