package com.example.noteapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteapplication.Adapter.NotesListAdapter;
import com.example.noteapplication.Database.RoomDB;
import com.example.noteapplication.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    FloatingActionButton fab_add;
    SearchView searchView_home;
    Notes selectedNote ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView= findViewById(R.id.recycler_home);
        fab_add= findViewById(R.id.fab_add);
        searchView_home = findViewById(R.id.searchView_home);

        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();

        upDateRecycler(notes);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , NotesTakerActivity.class);
                startActivityForResult(intent , 154);

            }
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filter(s);

                return true;
            }
        });

    }

    private void filter(String s) {

        List<Notes> filteredList = new ArrayList<>();
        for(Notes singleNote : notes)
        {
            if(singleNote.getTitle().toLowerCase().contains(s.toLowerCase())
                    || singleNote.getNotes().toLowerCase().contains(s.toLowerCase()))
            {
                filteredList.add(singleNote);
            }
        }

        notesListAdapter.filterList(filteredList);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 154)
        {
            if(resultCode==Activity.RESULT_OK)
            {
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
            }
        }
        else if(requestCode == 198)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().update(new_notes.getId() , new_notes.getTitle() , new_notes.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                recyclerView.setAdapter(notesListAdapter);
            }
        }

    }

    private void upDateRecycler(List<Notes> notes) {

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2 , LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this , notes , notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this , NotesTakerActivity.class);
            intent.putExtra("old_note" , notes);
            startActivityForResult(intent , 198);

        }


        @Override
        public void onLongClick(Notes notes, CardView cardView) {

            selectedNote = new Notes();
            selectedNote = notes ;
            showPopup(cardView);

        }
    };

    private void showPopup(CardView cardView) {

        PopupMenu popupMenu = new PopupMenu(this , cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();

    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        switch ((menuItem.getItemId()))
        {
            case R.id.pin:
                if(selectedNote.getPinned())
                {
                    database.mainDAO().pin(selectedNote.getId() , false);
                    Toast.makeText(MainActivity.this, "Unpinned!", Toast.LENGTH_SHORT).show();
                }else
                    {
                        database.mainDAO().pin(selectedNote.getId() , true);
                        Toast.makeText(MainActivity.this, "Pinned!", Toast.LENGTH_SHORT).show();
                    }
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
                return  true ;

            case R.id.delete:
                database.mainDAO().delete(selectedNote);
                notes.remove(selectedNote);
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Note is deleted", Toast.LENGTH_SHORT).show();
                return  true;

            default:
                return  false;

        }

    }
}
