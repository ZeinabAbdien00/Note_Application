package com.example.noteapplication;

import androidx.cardview.widget.CardView;

import com.example.noteapplication.Models.Notes;

public interface NotesClickListener {

    void onClick(Notes notes);
    void onLongClick(Notes notes , CardView cardView);
}
