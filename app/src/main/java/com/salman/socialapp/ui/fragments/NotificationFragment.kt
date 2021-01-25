package com.salman.socialapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.salman.socialapp.R
import com.salman.socialapp.adapters.NotificationAdapter
import com.salman.socialapp.model.Notification
import com.salman.socialapp.ui.activities.MainActivity
import com.salman.socialapp.viewmodels.NotificationViewModel
import com.salman.socialapp.viewmodels.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_notification.*

class NotificationFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var notificationViewModel: NotificationViewModel
    lateinit var notificationAdapter: NotificationAdapter
    val notificationList: MutableList<Notification> = ArrayList()
    var currentUserId: String? = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        // get user from sharedRef
        this.currentUserId = (activity as MainActivity).currentUserId
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationViewModel = ViewModelProvider(mContext as FragmentActivity, ViewModelFactory()).get(NotificationViewModel::class.java)
        notificationRV.layoutManager = LinearLayoutManager(mContext)
        fetchNotification()
    }

    private fun fetchNotification() {
        (activity as MainActivity).showProgressBar()
        notificationViewModel.getNotification(currentUserId!!)?.observe(viewLifecycleOwner, Observer { notificationResponse ->
            (activity as MainActivity).hideProgressBar()
            if (notificationResponse.status == 200) {
                notificationList.clear()
                notificationList.addAll(notificationResponse.notifications!!)
                notificationAdapter = NotificationAdapter(mContext, notificationList)
                notificationRV.adapter = notificationAdapter
            }
        })
    }

    companion object {
        fun getInstance() = NotificationFragment()
    }
}