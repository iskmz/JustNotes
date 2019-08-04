package com.iskandar.justnotes


import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_addnote.*
import kotlinx.android.synthetic.main.fragment_notelist.*
import kotlinx.android.synthetic.main.list_body.view.*
import kotlinx.android.synthetic.main.list_header.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import android.provider.SyncStateContract.Helpers.update
import android.content.ContentValues
import android.widget.ImageButton
import android.widget.Toast


/////////////////////////// DATA classes ( & SQLLITE) ////////////////////////////////////////


data class Note(var title:String , var content:String, var dateTime:Long)
var notes = mutableListOf<Note>()

class NotesDB(context:Context) : SQLiteOpenHelper(context,"myNotes.db",null,1)
{
    private val db = writableDatabase


    companion object {
        val TABLE_NAME ="NOTES"

        val COL_DATETIME = "DATETIME"
        val COL_TITLE = "TITLE"
        val COL_BODY = "BODY"

        val COLNUM_DATETIME = 0
        val COLNUM_TITLE = 1
        val COLNUM_BODY = 2
    }

    init {
        // for dev. db access
        provideAccessToDev()
    }

    private fun provideAccessToDev() {
        if (BuildConfig.DEBUG) File(db.path).setReadable(true, false)
        //@terminal: adb -d pull //data/data/com.iskandar.justnotes/databases/myNotes.db
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // create data table //
        var sqlStatment = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
        sqlStatment += COL_DATETIME + " TEXT PRIMARY KEY,"
        sqlStatment += COL_TITLE + " TEXT,"
        sqlStatment += COL_BODY + " TEXT"
        sqlStatment += ")"
        db!!.execSQL(sqlStatment)
    }

    fun getNotesTable():Cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME,null)

    fun addNoteNow(title : String, body : String) : Boolean {
        //create instance of ContentValues to hold our values
        val myValues = ContentValues()
        val datetime = System.currentTimeMillis().toString()
        //insert data by key and value
        myValues.put(COL_DATETIME,datetime)
        myValues.put(COL_TITLE,title)
        myValues.put(COL_BODY,body)
        // INSERT new row //
        // put values in table and get res (row id) // if res = -1 then ERROR //
        val res = db.insert(TABLE_NAME, null, myValues)
        //return true if we not get -1, error
        return res != (-1).toLong()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

}

/////////////////////////// MAIN ACTIVITY  //////////////////////////////////////

class MainActivity : AppCompatActivity() {

    private lateinit var fm : FragmentManager
    private lateinit var noteListFragment: NoteListFragment
    private lateinit var addNoteFragment: AddNoteFragment

    companion object {
        val TAG_FRAG_NOTELIST = "NOTELIST"
        val TAG_FRAG_ADDNOTE = "ADDNOTE"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFrags()
        setListeners()
    }

    private fun initFrags() {
        noteListFragment = NoteListFragment()
        addNoteFragment = AddNoteFragment()
        fm = supportFragmentManager
        fm.beginTransaction()
                .add(R.id.layContainer, noteListFragment, TAG_FRAG_NOTELIST)
                .add(R.id.layContainer,addNoteFragment, TAG_FRAG_ADDNOTE)
                .replace(R.id.layContainer,noteListFragment)
                .addToBackStack(null)
                .commit()
    }

    private fun setListeners() {

        btnExit.setOnClickListener { finish() }
        btnAbout.setOnClickListener { showAboutDialog() }
        btnAddNote.setOnClickListener {
            it.visibility = View.GONE
            btnBackToList.visibility = View.VISIBLE
            switchTo(addNoteFragment, TAG_FRAG_ADDNOTE)
        }

        btnBackToList.setOnClickListener {
            it.visibility = View.GONE
            btnAddNote.visibility = View.VISIBLE
            switchTo(noteListFragment, TAG_FRAG_NOTELIST)
        }
    }

    private fun switchTo(frg: Fragment, tag: String) {
        fm.beginTransaction()
            .replace(R.id.layContainer, frg,tag)
            .commit()
    }


    private fun showAboutDialog() {
        val about = AlertDialog.Builder(this@MainActivity)
            .setIcon(R.drawable.ic_info_outline)
            .setTitle("Just Notes")
            .setMessage("by Iskandar Mazzawi \u00A9")
            .setPositiveButton("OK"){ dialog, _ -> dialog.dismiss() }
            .create()
        about.setCanceledOnTouchOutside(false)
        about.show()
    }
}


/////////////////////////// NoteList Fragment  ////////////////////////////////////////

class NoteListFragment : Fragment() {

    private lateinit var myView : View
    private lateinit var adapter : NotesAdapter
    private lateinit var notesDB : NotesDB

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_notelist,container,false)
        return myView
    }

    override fun onStart() {
        super.onStart()

        notesDB = NotesDB(activity!!.applicationContext)
        refreshAdapter()

    }

    private fun refreshAdapter() {

        notes = getDataFromDB()
        if(notes.size==0)
        {
            lstNoteList.visibility = View.GONE
            txtEmptyNotesMsg.visibility = View.VISIBLE
            txtEmptyNotesMsg.text = " click on the (+) button below \n to start adding notes ..."
        }
        else {
            lstNoteList.visibility = View.VISIBLE
            txtEmptyNotesMsg.visibility = View.GONE
            adapter = NotesAdapter(activity!!.applicationContext)
            lstNoteList.setAdapter(adapter)
        }

    }

    private fun getDataFromDB(): MutableList<Note> {

        val lst = mutableListOf<Note>()

        // get data from SQL db using Cursor //
        val res = notesDB.getNotesTable()
        if (res.count == 0) return lst
        while (res.moveToNext()) {
            lst.add(
                Note(
                    res.getString(NotesDB.COLNUM_TITLE),
                    res.getString(NotesDB.COLNUM_BODY),
                    res.getString(NotesDB.COLNUM_DATETIME).toLong()
                )
            )
        }
        return lst
    }
}



///////////////////////////////// AddNote Fragment  ////////////////////////////////////////


class AddNoteFragment : Fragment() {

    private lateinit var myView : View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_addnote,container,false)
        return myView
    }

    override fun onStart() {
        super.onStart()

        setListeners()
    }

    private fun setListeners() {

        btnSaveNote.setOnClickListener {
            val title = txtTitle.text.toString()
            val body = txtBody.text.toString()

            if (checkFields(title,body)) saveNote(title,body)
        }
    }

    private fun saveNote(title: String, body: String) {

        val notesDB = NotesDB(context!!)

        if(notesDB.addNoteNow(title,body))
        {
            Toast.makeText(context,"Note was added successfully!",Toast.LENGTH_LONG).show()

            // switch buttons visibility !! //
            val vBtnBackToList = myView.rootView.findViewById<ImageButton>(R.id.btnBackToList)
            val vBtnAddNote = myView.rootView.findViewById<ImageButton>(R.id.btnAddNote)
            vBtnBackToList.visibility = View.GONE
            vBtnAddNote.visibility = View.VISIBLE

            // return to notelist fragment !! //
            val fm = activity!!.supportFragmentManager
            val noteListFragment = fm.findFragmentByTag(MainActivity.TAG_FRAG_NOTELIST)!!
            fm.beginTransaction()
                .replace(R.id.layContainer, noteListFragment)
                .commit()
        }
        else
        {
            Toast.makeText(context,"ERROR while adding note !!!",Toast.LENGTH_LONG).show()
        }


    }

    private fun checkFields(title: String, body: String): Boolean {

        if(title.isEmpty() || body.isEmpty())
        {
            Toast.makeText(context,"BOTH fields must NOT be empty!",Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

}

//////////////////////////////////////////  NOTES ADAPTER //////////////////////////////////////////////////////


class NotesAdapter(val context : Context) : BaseExpandableListAdapter() {


    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val v = LayoutInflater.from(context).inflate(R.layout.list_header,null)
        v.txtNoteOrder.text = (groupPosition+1).toString()
        v.txtNoteTitle.text = notes[groupPosition].title
        return v
    }


    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?,
                              parent: ViewGroup?): View
    {
        val v = LayoutInflater.from(context).inflate(R.layout.list_body,null)
        v.txtNoteDateTime.text = SimpleDateFormat("yyyy-MM-dd , HH:mm:ss")
            .format(Date(notes[groupPosition].dateTime))
        v.txtNoteBody.text = notes[groupPosition].content
        // TO-DO // v.btnNoteDelete.setOnClickListener {  }
        return v
    }

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int) = (10*groupPosition + childPosition + 1).toLong()

    override fun getChildrenCount(groupPosition: Int) = if(notes.size==0) 0 else 1
    override fun getGroupCount() = notes.size

    override fun getGroup(groupPosition: Int) = notes[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int) = notes[groupPosition]
    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = false
    override fun hasStableIds() = false
}