package com.example.domopotapp.test

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.domopotapp.R
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TestActivity : AppCompatActivity() {

    val vm: TestViewModel by viewModels()

    private val db = Firebase.database.reference
    private lateinit var dbListener: ChildEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val vp = findViewById<ViewPager2>(R.id.testViewPager)
        /*val btn = findViewById<Button>(R.id.btn)
        val txt = findViewById<TextView>(R.id.txt)*/

        vp.adapter = TestAdapter(vm.testData.value!!)

        /*btn.setOnClickListener {
            vm.text.value += "!"
        }*/

        /*vm.testData.observe(this) {
            (vp.adapter as TestAdapter).submitList(vm.testData.value!!)
        }*/


        dbListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //vm.text.value = snapshot.value.toString()
                val index = vm.testData.value!!.indexOf(vm.testData.value!!.find { it.id == snapshot.key.toString() })

                if (index >= 0) vm.testData.value!![index].text = snapshot.value.toString()
                else vm.testData.value!!.add(TestData(snapshot.key.toString(), snapshot.value.toString()))

                (vp.adapter as TestAdapter).submitList(vm.testData.value!!)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                //vm.text.value = snapshot.value.toString()
                val index = vm.testData.value!!.indexOf(vm.testData.value!!.find { it.id == snapshot.key.toString() })
                if (index >= 0) vm.testData.value!![index].text = snapshot.value.toString()

                (vp.adapter as TestAdapter).submitList(vm.testData.value!!)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        }

        db.child("Test").addChildEventListener(dbListener)
    }

    override fun onDestroy() {
        db.child("Test").removeEventListener(dbListener)
        super.onDestroy()
    }

}

data class TestData(
    var id: String,
    var text: String
)

class TestAdapter(private var l: MutableList<TestData>) :
    RecyclerView.Adapter<TestAdapter.TestViewHolder>() {

    class TestViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val text: TextView = v.findViewById(R.id.viewPagerText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.test_item, parent, false)
        return TestViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.text.text = l[position].text
    }

    override fun getItemCount(): Int {
        return l.size
    }

    fun submitList(newL: List<TestData>) {
        /*Log.i("submitList", l.size.toString() + " " + newL.size.toString())
        l.clear()
        l.addAll(newL)
        notifyDataSetChanged()*/
        newL.forEach { new ->
            val index = l.indexOf(l.find { it.id == new.id })
            if (index >= 0) updateListItem(new, index)
            else addListItem(new, l.size)
        }
    }

    fun addListItem(newData: TestData, position: Int) {
        l.add(position, newData)
        notifyItemInserted(position)
    }

    fun removeListItem(position: Int) {
        l.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateListItem(newData: TestData, position: Int) {
        l[position].text = newData.text
        notifyItemChanged(position)
    }
}