package io.mochadwi.analyticssms

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import io.mochadwi.analyticssms.data.helper.CustomDividerItemDecoration
import io.mochadwi.analyticssms.data.network.ApiClient
import io.mochadwi.analyticssms.data.network.ApiInterface
import io.mochadwi.analyticssms.domain.SmsEntity
import io.mochadwi.analyticssms.sms.adapter.SmsAdapter
import kotlinx.android.synthetic.main.activity_sms.*
import kotlinx.android.synthetic.main.content_sms.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SmsActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
        SmsAdapter.SmsAdapterListener {
    private val messages = ArrayList<SmsEntity>()
    private var mAdapter: SmsAdapter? = null
    private var actionModeCallback: ActionModeCallback? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        setSupportActionBar(toolbar)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            Snackbar.make(it, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        swipe_refresh_layout?.setOnRefreshListener(this)

        mAdapter = SmsAdapter(this, messages, this)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recycler_view?.layoutManager = mLayoutManager
        recycler_view?.itemAnimator = DefaultItemAnimator()
        recycler_view?.addItemDecoration(CustomDividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recycler_view?.adapter = mAdapter

        actionModeCallback = ActionModeCallback()

        // show loader and fetch messages
        swipe_refresh_layout?.post { getInbox() }
    }

    /**
     * Fetches mail messages by making HTTP request
     * url: https://api.androidhive.info/json/inbox.json
     */
    private fun getInbox() {
        swipe_refresh_layout?.isRefreshing = true

        val apiService = ApiClient.client.create<ApiInterface>(ApiInterface::class.java)

        val call = apiService.inbox
        call.enqueue(object : Callback<List<SmsEntity>> {
            override fun onResponse(call: Call<List<SmsEntity>>, response: Response<List<SmsEntity>>) {
                // clear the inbox
                messages.clear()

                // add all the messages
                // messages.addAll(response.body());

                // TODO - avoid looping
                // the loop was performed to add colors to each message
                for (message in response.body()!!) {
                    // generate a random color
                    message.color = getRandomMaterialColor("400")
                    messages.add(message)
                }

                mAdapter?.notifyDataSetChanged()
                swipe_refresh_layout?.isRefreshing = false
            }

            override fun onFailure(call: Call<List<SmsEntity>>, t: Throwable) {
                Toast.makeText(applicationContext, "Unable to fetch json: " + t.message, Toast.LENGTH_LONG).show()
                swipe_refresh_layout?.isRefreshing = false
            }
        })
    }

    /**
     * chooses a random color from array.xml
     */
    private fun getRandomMaterialColor(typeColor: String): Int {
        var returnColor = Color.GRAY
        val arrayId = resources.getIdentifier("mdcolor_$typeColor", "array", packageName)

        if (arrayId != 0) {
            val colors = resources.obtainTypedArray(arrayId)
            val index = (Math.random() * colors.length()).toInt()
            returnColor = colors.getColor(index, Color.GRAY)
            colors.recycle()
        }
        return returnColor
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_sms, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_search) {
            Toast.makeText(applicationContext, "Search...", Toast.LENGTH_SHORT).show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        // swipe refresh is performed, fetch the messages again
        getInbox()
    }

    override fun onIconClicked(position: Int) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback!!)
        }

        toggleSelection(position)
    }

    override fun onIconImportantClicked(position: Int) {
        // Star icon is clicked,
        // mark the message as important
        val message = messages[position]
        message.isImportant = !message.isImportant
        messages[position] = message
        mAdapter?.notifyDataSetChanged()
    }

    override fun onMessageRowClicked(position: Int) {
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        mAdapter?.selectedItemCount?.takeIf { it > 0 }?.let {
            enableActionMode(position)
        } ?: run {
            // read the message which removes bold from the row
            val message = messages[position]
            message.isRead = true
            messages[position] = message
            mAdapter?.notifyDataSetChanged()

            Toast.makeText(applicationContext, "Read: " + message.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRowLongClicked(position: Int) {
        // long press is performed, enable action mode
        enableActionMode(position)
    }

    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback!!)
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        mAdapter?.toggleSelection(position)
        val count = mAdapter?.selectedItemCount

        if (count == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = count.toString()
            actionMode?.invalidate()
        }
    }


    private inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_action_mode, menu)

            // disable swipe refresh if action mode is enabled
            swipe_refresh_layout?.isEnabled = false
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> {
                    // delete all the selected messages
                    deleteMessages()
                    mode.finish()
                    return true
                }

                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mAdapter?.clearSelections()
            swipe_refresh_layout?.isEnabled = true
            actionMode = null
            recycler_view?.post {
                mAdapter?.resetAnimationIndex()
                mAdapter?.notifyDataSetChanged()
            }
        }
    }

    // deleting the messages from recycler view
    private fun deleteMessages() {
        mAdapter?.resetAnimationIndex()
        val selectedItemPositions = mAdapter?.getSelectedItems()
        selectedItemPositions?.indices?.reversed()?.forEachIndexed { i, _ ->
            mAdapter?.removeData(selectedItemPositions[i])
        }
        mAdapter?.notifyDataSetChanged()
    }
}