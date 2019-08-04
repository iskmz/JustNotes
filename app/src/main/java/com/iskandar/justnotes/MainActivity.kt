package com.iskandar.justnotes


import android.content.Context
import android.database.DataSetObserver
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_addnote.*
import kotlinx.android.synthetic.main.fragment_notelist.*
import kotlinx.android.synthetic.main.list_body.view.*
import kotlinx.android.synthetic.main.list_header.view.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/////////////////////////// DATA classes ////////////////////////////////////////


data class Note(var title:String , var content:String, var dateTime:Long)
var notes = mutableListOf<Note>()


/////////////////////////// MAIN ACTIVITY  //////////////////////////////////////

class MainActivity : AppCompatActivity() {

    private lateinit var fm : FragmentManager
    private lateinit var noteListFragment: NoteListFragment
    private lateinit var addNoteFragment: AddNoteFragment


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
                .add(R.id.layContainer, noteListFragment)
                .commit()
    }

    private fun setListeners() {

        btnExit.setOnClickListener { finish() }
        btnAbout.setOnClickListener { showAboutDialog() }
        btnAddNote.setOnClickListener {
            it.visibility = View.INVISIBLE
            switchTo(addNoteFragment)
        }
    }

    private fun switchTo(frg: Fragment) {
        fm.beginTransaction()
            .replace(R.id.layContainer, frg)
            .commit()
    }


    private fun showAboutDialog() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


/////////////////////////// NoteList Fragment  ////////////////////////////////////////

class NoteListFragment : Fragment() {

    private lateinit var myView : View
    private lateinit var adapter : NotesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.fragment_notelist,container,false)
        return myView
    }

    override fun onStart() {
        super.onStart()

        refreshAdapter()

    }

    private fun refreshAdapter() {

        notes = getDataFromDB()
        adapter = NotesAdapter(activity!!.applicationContext)
        lstNoteList.setAdapter(adapter)

    }

    private fun getDataFromDB(): MutableList<Note> {

        val lst = mutableListOf<Note>()

        lst.add(Note("note 1","sdgnosdngonasdknglsndgnsdgs",124178624L))
        lst.add(Note("note 2","sdgnosdngonasdknglsndgnsdgs",12411816524L))
        lst.add(Note("note 3","sdgnosdngonasdknglsndgnsdgs",12412278784L))
        lst.add(Note("note 4","sdgnosdngonasdknglsndgnsdgs",1242158124L))
        lst.add(Note("note 5","sdgnosdngonasdknglsndgnsdgs",124118324L))

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


    }

    private fun checkFields(title: String, body: String): Boolean {

            return false
    }

}

//////////////////////////////////////////  NOTES ADAPTER //////////////////////////////////////////////////////


class NotesAdapter(val context : Context) : BaseExpandableListAdapter() {


    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {

        val v = LayoutInflater.from(context).inflate(R.layout.list_header,null)

        if(notes.size==0)
        {
            v.txtNoteOrder.text = ""
            v.txtNoteTitle.text = " click on (+) below \n to start adding notes ... "
        }
        else
        {
            v.txtNoteOrder.text = (groupPosition+1).toString()
            v.txtNoteTitle.text = notes[groupPosition].title
        }

        return v
    }


    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?,
                              parent: ViewGroup?): View
    {
        val v = LayoutInflater.from(context).inflate(R.layout.list_body,null)

        v.txtNoteDateTime.text = SimpleDateFormat("yyyy-MM-dd , HH:mm:ss")
            .format(Date(notes[groupPosition].dateTime))

        v.txtNoteBody.text = notes[groupPosition].content

        return v
    }

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int) = (10*groupPosition + childPosition + 1).toLong()

    override fun getChildrenCount(groupPosition: Int) = if(notes.size==0) 0 else 1
    override fun getGroupCount() = if (notes.size==0) 1 else notes.size

    override fun getGroup(groupPosition: Int) = notes[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int) = notes[groupPosition]
    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = false
    override fun hasStableIds() = false
}