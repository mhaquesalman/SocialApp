package com.salman.socialapp.ui.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.salman.socialapp.R
import com.salman.socialapp.ui.fragments.MessageFragment
import com.salman.socialapp.ui.fragments.RecentFragment
import com.salman.socialapp.util.Utils
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.getLanguage(this)
        setContentView(R.layout.activity_message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val typefaceSpan = TypefaceSpan(resources.getFont(R.font.aclonica))
            val title = SpannableString("SocialApp")
            title.setSpan(typefaceSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            supportActionBar?.setTitle(title)
        } else {
            val actionBar = supportActionBar
            val tv = TextView(applicationContext)
            val typeface =  ResourcesCompat.getFont(this, R.font.aclonica)
            val lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            tv.layoutParams = lp
            tv.setText("SocialApp")
            tv.setTextSize(22f)
            tv.setTextColor(Color.WHITE)
            tv.setTypeface(typeface)
            actionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
            actionBar?.setCustomView(tv)

        }
//        supportActionBar?.setTitle("Messages")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initialization()
    }

    private fun initialization() {
        // Tab Layout and viewpager
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(MessageFragment.getInstance(), resources.getString(R.string.friends_title))
        viewPagerAdapter.addFragment(RecentFragment.getInstance(), resources.getString(R.string.recent_title))
        view_pager.adapter = viewPagerAdapter

        // attach tablayout with viewpager
        tab_layout.setupWithViewPager(view_pager)
        tab_layout.getTabAt(0)?.setIcon(R.drawable.ic_friends)
        tab_layout.getTabAt(1)?.setIcon(R.drawable.ic_recent_message)
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