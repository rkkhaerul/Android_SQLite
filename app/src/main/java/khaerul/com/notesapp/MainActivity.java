package khaerul.com.notesapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import khaerul.com.notesapp.database.DatabaseHelper;
import khaerul.com.notesapp.database.model.Note;
import khaerul.com.notesapp.views.NotesAdapter;

public class MainActivity extends AppCompatActivity {

    private NotesAdapter adapterNote;
    private List<Note> noteList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noNotesTextView;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout_main);
        recyclerView = (RecyclerView) findViewById(R.id.rv_list);
        noNotesTextView = (TextView) findViewById(R.id.txt_emptyfound);

        db = new DatabaseHelper(this);

        noteList.addAll(db.getAllNotes());

        FloatingActionButton fabAddNewNote = (FloatingActionButton) findViewById(R.id.fab_add);

        fabAddNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog(false, null, -1);
            }
        });

        adapterNote = new NotesAdapter(this, noteList);
        RecyclerView.LayoutManager rv_layoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(rv_layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(adapterNote);

        toogleEmptyNotes();
    }

    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View view = layoutInflater.inflate(R.layout.add_note_dialog, null);

        AlertDialog.Builder alertdialogBuilderInput = new AlertDialog.Builder(this);
        alertdialogBuilderInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.edt_new_note);
        TextView dialogTitle = view.findViewById(R.id.dialog_add_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_notetitle) : getString(R.string.lbl_edit_notetitle));

        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }

        alertdialogBuilderInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "Update" : "Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogBox, int id) {
                        dialogBox.cancel();
                    }
                });

        final AlertDialog alertDialog = alertdialogBuilderInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputNote.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Inputan Note Belum Diisi!", Toast.LENGTH_SHORT).show();

                    return;
                } else {
                    alertDialog.dismiss();
                }

                // Melakukan Update/New Note Proses
                if (shouldUpdate && note != null) {
                    updateNote(inputNote.getText().toString(), position);
                } else {
                    createNote(inputNote.getText().toString());

                    Log.d("GetNote :", inputNote.getText().toString());
                }
            }
        });
    }

    private void updateNote(String note, int position) {
        Note noteUpdate = noteList.get(position);

        noteUpdate.setNote(note);

        db.upadteNote(noteUpdate);

        noteList.set(position, noteUpdate);
        adapterNote.notifyItemChanged(position);

        toogleEmptyNotes();
    }

    private void toogleEmptyNotes() {
        if (db.getNoteCount() > 0) {
            noNotesTextView.setVisibility(View.GONE);
        } else {
            noNotesTextView.setVisibility(View.VISIBLE);
        }
    }

    private void createNote(String note) {
        long id = db.insertNote(note);

        Note noteCreate = db.getNote(id);

        if (noteCreate != null) {
            noteList.add(0, noteCreate);

            adapterNote.notifyDataSetChanged();
            toogleEmptyNotes();
        }
    }
}
