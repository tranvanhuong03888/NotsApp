package com.itplus.notsapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.itplus.notsapp.R;
import com.itplus.notsapp.database.NotesDatabase;
import com.itplus.notsapp.entities.Note;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNote_Activity extends AppCompatActivity {

    private EditText edtNoteTitle,edtNoteSubTitle,edtNoteText;
    private TextView txtvDateTime;
    private View viewSubtitleIndicator;
    private ImageView imgNote;
    private String selectedImagePath;
    private String selectedNoteColor;
    private TextView txtvWebURL;
    private LinearLayout layoutWebURL;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private AlertDialog dialogAddURL;
    private Note alreadyAvaiLableNote;
    private AlertDialog dialogDeleteNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        selectedNoteColor = "#333333";
        selectedImagePath = "";
        mapping();
        if (getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvaiLableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
        if (getIntent().getBooleanExtra("isFromQuickActions",false)){
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null){
                if (type.equals("image")){
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    imgNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imgNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imgRemoveImage).setVisibility(View.VISIBLE);
                }else if(type.equals("URL")){
                    txtvWebURL.setText(getIntent().getStringExtra("URL"));
                    layoutWebURL.setVisibility(View.VISIBLE);
                }
            }
        }
        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    private void mapping() {
        edtNoteTitle = findViewById(R.id.edtNoteTitle);
        edtNoteSubTitle = findViewById(R.id.edtNoteSubTitle);
        edtNoteText = findViewById(R.id.edtNote);
        txtvDateTime = findViewById(R.id.txtvDateTime);
        ImageView imgBack = findViewById(R.id.imgBack);
        imgNote = findViewById(R.id.imgNote);
        txtvWebURL = findViewById(R.id.txtvWebURL);
        layoutWebURL = findViewById(R.id.layoutWebUrl);
        ImageView imgSave = findViewById(R.id.imgSave);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        txtvDateTime.setText(
                new SimpleDateFormat("EEEE,dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date())
        );
        imgSave.setOnClickListener(view -> saveNote());
        imgBack.setOnClickListener(view -> onBackPressed());
        findViewById(R.id.imgRemoveWebURL).setOnClickListener(view -> {
            txtvWebURL.setText(null);
            layoutWebURL.setVisibility(View.GONE);
        });
        findViewById(R.id.imgRemoveImage).setOnClickListener(view -> {
            imgNote.setImageBitmap(null);
            imgNote.setVisibility(View.GONE);
            findViewById(R.id.imgRemoveImage).setVisibility(View.GONE);
            selectedImagePath = "";
        });
    }

    private void setViewOrUpdateNote(){

        edtNoteTitle.setText(alreadyAvaiLableNote.getTitle());
        edtNoteSubTitle.setText(alreadyAvaiLableNote.getSubtitle());
        edtNoteText.setText(alreadyAvaiLableNote.getNoteText());
        txtvDateTime.setText(alreadyAvaiLableNote.getDateTime());

        if (alreadyAvaiLableNote.getImagePath() != null && !alreadyAvaiLableNote.getImagePath().trim().isEmpty()){
            imgNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvaiLableNote.getImagePath()));
            imgNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imgRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvaiLableNote.getImagePath();
        }

        if (alreadyAvaiLableNote.getWebLink() != null && !alreadyAvaiLableNote.getWebLink().trim().isEmpty()){
            txtvWebURL.setText(alreadyAvaiLableNote.getWebLink());
            layoutWebURL.setVisibility(View.VISIBLE);
        }

    }

    private void saveNote() {
        if (edtNoteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note title can't be empty!",Toast.LENGTH_SHORT).show();
            return;
        }else if (edtNoteSubTitle.getText().toString().trim().isEmpty()
        && edtNoteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note can't be empty!",Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(edtNoteTitle.getText().toString());
        note.setSubtitle(edtNoteSubTitle.getText().toString());
        note.setNoteText(edtNoteText.getText().toString());
        note.setDateTime(txtvDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if (layoutWebURL.getVisibility() == View.VISIBLE){
            note.setWebLink(txtvWebURL.getText().toString());
        }

        if (alreadyAvaiLableNote != null){
            note.setId(alreadyAvaiLableNote.getId());
        }

        class SaveNoteTask extends AsyncTask<Void,Void,Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }

    private void initMiscellaneous(){
        final LinearLayoutCompat layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.txtvMiscellaneous).setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }else{
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView imgColor1 = layoutMiscellaneous.findViewById(R.id.imgColor1);
        final ImageView imgColor2 = layoutMiscellaneous.findViewById(R.id.imgColor2);
        final ImageView imgColor3 = layoutMiscellaneous.findViewById(R.id.imgColor3);
        final ImageView imgColor4 = layoutMiscellaneous.findViewById(R.id.imgColor4);
        final ImageView imgColor5 = layoutMiscellaneous.findViewById(R.id.imgColor5);

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(view -> {
            selectedNoteColor = "#333333";
            imgColor1.setImageResource(R.drawable.ic_done);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(view -> {
            selectedNoteColor = "#FDBE3B";
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(R.drawable.ic_done);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(view -> {
            selectedNoteColor = "#FF4842";
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(R.drawable.ic_done);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(view -> {
            selectedNoteColor = "#3A52Fc";
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(R.drawable.ic_done);
            imgColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });

        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(view -> {
            selectedNoteColor = "#000000";
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(R.drawable.ic_done);
            setSubtitleIndicatorColor();
        });

        if (alreadyAvaiLableNote != null && alreadyAvaiLableNote.getColor() != null && !alreadyAvaiLableNote.getColor().trim().isEmpty()){
            switch (alreadyAvaiLableNote.getColor()){
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52Fc":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        CreateNote_Activity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            }else {
                selectImage();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layAddUrl).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });

        if (alreadyAvaiLableNote != null){
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(view -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteNoteDialog();
            });
        }

    }

    private void showDeleteNoteDialog(){

        if (dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNote_Activity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.txtvDeleteNote).setOnClickListener(view1 -> {
                class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NotesDatabase.getDatabase(getApplicationContext()).noteDao()
                                .deleteNote(alreadyAvaiLableNote);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        Intent intent = new Intent();
                        intent.putExtra("isNoteDeleted",true);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }
                new DeleteNoteTask().execute();
            });

            view.findViewById(R.id.txtvCancel).setOnClickListener(view1 -> {
                dialogDeleteNote.dismiss();
            });

        }
        dialogDeleteNote.show();
    }

    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage(){
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
            //Intent.createChooser(intent,"Select Picture")
       // }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imgNote.setImageBitmap(bitmap);
                        imgNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imgRemoveImage).setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectedImageUri);

                    } catch (Exception e) {
                        Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri,null,null,null,null);
        if (cursor == null){
            filePath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showAddURLDialog(){
        if (dialogAddURL == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNote_Activity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_uri,
                    findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null){
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText edtURL = view.findViewById(R.id.edtURL);
            edtURL.requestFocus();

            view.findViewById(R.id.txtvAdd).setOnClickListener(view1 -> {
                if (edtURL.getText().toString().trim().isEmpty()){
                    Toast.makeText(CreateNote_Activity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(edtURL.getText().toString()).matches()) {
                    Toast.makeText(CreateNote_Activity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                }else{
                    txtvWebURL.setText(edtURL.getText().toString());
                    layoutWebURL.setVisibility(View.VISIBLE);
                    dialogAddURL.dismiss();
                }

            });
            view.findViewById(R.id.txtvCancel).setOnClickListener(view1 -> {
                dialogAddURL.dismiss();
            });
        }
        dialogAddURL.show();
    }

}