package uk.airbyte.skrrt

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import android.widget.LinearLayout
import com.google.firebase.analytics.FirebaseAnalytics
import android.util.StatsLog.logEvent
import android.R.id






class MainActivity : AppCompatActivity(), ItemRowListener {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    lateinit var mDatabase: DatabaseReference
    lateinit var adapter: RapperAdapter
    lateinit var listView: ListView
    var rapperList: MutableList<Rapper>? = null

    var sortedBy: String = "name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        listView = findViewById(R.id.rapper_list_view)
        listView.setOnItemClickListener { _, _, position, _ ->

            val seletedRapper = rapperList!![position]
            val detail = RapperDetailActivity.newIntent(this, seletedRapper)
            startActivity(detail)
        }

        listView.setOnItemLongClickListener { adapterView, view, position, l ->
            val selectedRapper = rapperList!![position]
            editItemDialog(selectedRapper)
            true
        }

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            //Show Dialog here to add new Item

            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Add new rapper")
            mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            addNewItemDialog()
        }

        val sortName = findViewById<View>(R.id.sortName) as FloatingActionButton
        sortName.setOnClickListener { view ->
            sortByName()
        }

        val sortStatus = findViewById<View>(R.id.sortStatus) as FloatingActionButton
        sortStatus.setOnClickListener { view ->
            sortByRating()
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        rapperList = mutableListOf()

        adapter = RapperAdapter(this, rapperList!!)
        listView.adapter = adapter
        mDatabase.addValueEventListener(itemListener)
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {
        val items = dataSnapshot.children.iterator()

        // Check if current database contains any collection
        if (items.hasNext()) {
            val rapperListindex = items.next()
            val itemsIterator = rapperListindex.children.iterator()
            rapperList!!.clear()

            // check if the collection has any to do items or not
            while (itemsIterator.hasNext()) {

                // get current item
                val currentItem = itemsIterator.next()
                val rapperItem = Rapper.create()

                // get current data in a map
                val map = currentItem.getValue() as HashMap<*, *>

                // key will return Firebase ID
                rapperItem.objectId = currentItem.key
                rapperItem.name = map.get("name") as String?
                rapperItem.birthplace = map.get("birthplace") as String?
                rapperItem.imageUrl = map.get("imageUrl") as String?
                rapperItem.status = map.get("status") as String?
                rapperItem.rating = map.get("rating") as Long?
                rapperList!!.add(rapperItem)
            }
        }

        if (sortedBy == "name") {
            sortByName()
        } else {
            sortByRating()
        }

        //alert adapter that has changed
//        adapter.notifyDataSetChanged()
    }

    private fun addNewItemDialog() {
        val alert = AlertDialog.Builder(this)
        alert.setMessage("Add New Rapper")

        val context = alert.context
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val nameBox = EditText(context)
        nameBox.hint = "Name"
        layout.addView(nameBox)

        val birthplaceBox = EditText(context)
        birthplaceBox.hint = "Birthplace"
        layout.addView(birthplaceBox)

        val statusBox = EditText(context)
        statusBox.hint = "Status (dead / alive / prison)"
        layout.addView(statusBox)

        val imageBox = EditText(context)
        imageBox.hint = "Image URL"
        layout.addView(imageBox)

        alert.setView(layout)

        alert.setPositiveButton("Submit") { dialog, positiveButton ->
            val rapperItem = Rapper.create()
            rapperItem.name = nameBox.text.toString()
            rapperItem.birthplace = birthplaceBox.text.toString()
            rapperItem.status = statusBox.text.toString()
            rapperItem.imageUrl = imageBox.text.toString()

            // We first make a push so that a new item is made with a unique ID
            val newItem = mDatabase.child(Constants.FIREBASE_ITEM).push()

            // then, we used the reference to set the value on that ID
            rapperItem.objectId = newItem.key

            newItem.setValue(rapperItem)
            dialog.dismiss()
            Toast.makeText(this, "Rapper saved with ID " + rapperItem.objectId, Toast.LENGTH_SHORT).show()
        }
        alert.show()
    }

    private fun editItemDialog(rapper: Rapper) {
        val alert = AlertDialog.Builder(this)
        alert.setMessage("Edit Rapper")

        val context = alert.context
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val nameBox = EditText(context)
        nameBox.hint = "Name"
        nameBox.setText(rapper.name)
        layout.addView(nameBox)

        val birthplaceBox = EditText(context)
        birthplaceBox.hint = "Birthplace"
        birthplaceBox.setText(rapper.birthplace)
        layout.addView(birthplaceBox)

        val statusBox = EditText(context)
        statusBox.hint = "Status (dead / alive / prison)"
        statusBox.setText(rapper.status)
        layout.addView(statusBox)

        val imageBox = EditText(context)
        imageBox.hint = "Image URL"
        imageBox.setText(rapper.imageUrl)
        layout.addView(imageBox)

        alert.setView(layout)

        alert.setPositiveButton("Submit") { dialog, positiveButton ->
            val rapperItem = Rapper.create()
            rapperItem.name = nameBox.text.toString()
            rapperItem.birthplace = birthplaceBox.text.toString()
            rapperItem.status = statusBox.text.toString()
            rapperItem.imageUrl = imageBox.text.toString()

            val rowListener: ItemRowListener = this
            rowListener.modifyItemState(rapper.objectId!!, rapperItem)

            dialog.dismiss()
            Toast.makeText(this, "Rapper updated with ID " + rapper.objectId, Toast.LENGTH_SHORT).show()
        }
        alert.setNegativeButton("Delete") { dialog, negativeButton ->
            val rowListener: ItemRowListener = this
            rowListener.onItemDelete(rapper.objectId!!, rapper)
        }
        alert.show()
    }

    override fun modifyItemState(itemObjectId: String, rapper: Rapper) {
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        itemReference.child("name").setValue(rapper.name)
        itemReference.child("birthplace").setValue(rapper.birthplace)
        itemReference.child("status").setValue(rapper.status)
        itemReference.child("imageUrl").setValue(rapper.imageUrl)
    }

    override fun onItemDelete(itemObjectId: String, rapper: Rapper) {
        // get child reference in database via the ObjectID
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        // deletion can be done via removeValue() method
        itemReference.removeValue()

        rapperList!!.remove(rapper)
        //alert adapter that has changed
        adapter.notifyDataSetChanged()
    }

    override fun setRapperStatus(itemObjectId: String, rapper: Rapper) {

    }

    override fun changeCountItemState(itemObjectId: String, rapper: Rapper) {
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        itemReference.child("rating").setValue(rapper.rating)
    }

    private var itemListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            addDataToList(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    fun nameSort(r: Rapper): String = r.name!!

    fun sortByName() {
        rapperList!!.sortBy { rapper -> nameSort(rapper) }
        sortedBy = "name"
        adapter.notifyDataSetChanged()
    }

    fun ratingSort(r: Rapper) : Long? = r.rating

    fun sortByRating() {
        rapperList!!.sortByDescending { rapper -> ratingSort(rapper) }
        sortedBy = "rating"
        adapter.notifyDataSetChanged()
    }
}
