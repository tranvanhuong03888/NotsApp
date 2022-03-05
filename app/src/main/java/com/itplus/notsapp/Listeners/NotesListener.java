package com.itplus.notsapp.Listeners;

import com.itplus.notsapp.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note,int position);
}
