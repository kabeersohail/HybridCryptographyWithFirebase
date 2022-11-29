package com.example.hybridcryptographywithfirebase.viewpager

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter


class PagerAdapter(
    private val views: List<View>
): PagerAdapter() {

    override fun getCount(): Int = views.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) = container.removeView(views[position])

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = views[position]
        container.addView(view)
        return view
    }

    override fun getItemPosition(`object`: Any): Int {
        for (index in 0 until count) {
            if (`object` as View === views[index]) {
                return index
            }
        }
        return POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence {
        return "View " + (position + 1)
    }

    fun getView(position: Int): View = views[position]

}