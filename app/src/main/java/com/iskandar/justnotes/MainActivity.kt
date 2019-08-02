package com.iskandar.justnotes

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*


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
            switchTo(fm.findFragmentById(R.id.layContainer)!!)
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


class NoteListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

}


class AddNoteFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

}
