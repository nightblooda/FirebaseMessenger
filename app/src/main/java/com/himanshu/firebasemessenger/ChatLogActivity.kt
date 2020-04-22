package com.himanshu.firebasemessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    val toUser : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)



        rv_chat_log.adapter = adapter




//        val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser.username



        listenForMessages()

        send_button.setOnClickListener {
            Log.d("AppDebug", "ChatLogActivity: Send button is clicked")
            performChatSend()
        }


    }

    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
//        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        Log.d("AppDebug", "ChatLogActivity: listenForMessages is called")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("AppDebug", "ChatLogActivity: onCancelled")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                Log.d("AppDebug", "ChatLogActivity: onChildMoved")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d("AppDebug", "ChatLogActivity: onChildChanged")
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                Log.d("AppDebug", "ChatLogActivity: onChildAdded is called")

                val chatMessage = p0.getValue(ChatMessage::class.java)
                if(chatMessage != null){
                    Log.d("AppDebug", "ChatLogActivity: ${chatMessage?.text}")
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        Log.d("AppDebug", "Chat From: ${chatMessage.text}")
                        val fromUser = LatestMessageActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, fromUser!!))
                    }else{
                        Log.d("AppDebug", "Chat To: ${chatMessage.text}")
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }

                }else{
                    Log.d("AppDebug", "ChatLogActivity: Chat message is null")
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                Log.d("AppDebug", "ChatLogActivity: onChildRemoved")
            }

        })
    }

    private fun performChatSend(){
        val text = chat_box.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid
        if(fromId == null) return

//        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = toId?.let {
            ChatMessage(ref.key!!, text, fromId,
                it, System.currentTimeMillis() / 1000)
        }
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                chat_box.text.clear()
                rv_chat_log.scrollToPosition(adapter.itemCount - 1)
                if(chatMessage != null){
                    adapter.add(ChatFromItem(chatMessage?.text, user))
                    Log.d("AppDebug", "ChatLogActivity: Chat is sent ref: ${ref.key}")
                }
            }
        toRef.setValue(chatMessage)

    }
}


class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_chat_from.text = text
        Picasso.get().load(user.imgUrl).into(viewHolder.itemView.img_chat_from)
    }

}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_chat_to.text = text
        Picasso.get().load(user.imgUrl).into(viewHolder.itemView.img_chat_to)
    }

}
