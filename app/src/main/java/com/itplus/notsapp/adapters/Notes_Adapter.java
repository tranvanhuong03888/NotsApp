package com.itplus.notsapp.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itplus.notsapp.Listeners.NotesListener;
import com.itplus.notsapp.R;
import com.itplus.notsapp.entities.Note;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Notes_Adapter extends RecyclerView.Adapter<Notes_Adapter.NoteViewHoder>{

    private List<Note> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> notesSource;

    public Notes_Adapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        notesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHoder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHoder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(view ->
                notesListener.onNoteClicked(notes.get(position),position));
    }

    @Override
    public int getItemCount() {
        if (notes == null){
            return 0;
        }
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHoder extends RecyclerView.ViewHolder{

        TextView txtvTitle,txtvSubTitle,txtvDateTime;
        LinearLayoutCompat layoutNote;
        RoundedImageView imgNote;
        public NoteViewHoder(@NonNull View itemView) {
            super(itemView);
            txtvTitle = itemView.findViewById(R.id.txtvTitle);
            txtvSubTitle = itemView.findViewById(R.id.txtvSubTitle);
            txtvDateTime = itemView.findViewById(R.id.txtvDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imgNote = itemView.findViewById(R.id.imgNote);
        }

        void setNote(Note note){
            txtvTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()){
                txtvSubTitle.setVisibility(View.GONE);
            }else {
                txtvSubTitle.setText(note.getDateTime());
            }
            txtvDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if (note.getImagePath() != null){
                imgNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imgNote.setVisibility(View.VISIBLE);
            }else{
                imgNote.setVisibility(View.GONE);
            }
        }

    }

    public void searchNotes(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()){
                    notes = notesSource;
                }else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : notesSource){
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                        || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                        || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        },500);
    }

    public void cancelTimer(){
        if (timer != null){
            timer.cancel();
        }
    }

}
