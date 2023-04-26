package com.gabidbr.mindzen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class ExercisesActivity extends AppCompatActivity {

    RecyclerView breathingRecyclerView;
    RecyclerView musicRecyclerView;
    ArrayList<ChildModelClass> breathingExercisesList;
    ArrayList<ChildModelClass> musicList;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        backButton = findViewById(R.id.backButton1);

        breathingRecyclerView = findViewById(R.id.rv_breathing_exercises);
        musicRecyclerView = findViewById(R.id.rv_relaxing_music);

        breathingExercisesList = new ArrayList<>();
        musicList = new ArrayList<>();

        breathingExercisesList.add(new ChildModelClass(R.drawable.lions_breathing,"Lion breathing","lions-breathing.mp4"));
        breathingExercisesList.add(new ChildModelClass(R.drawable.bellybreathing, "Belly breathing","belly-breathing.mp4"));
        breathingExercisesList.add(new ChildModelClass(R.drawable.boxbreathing, "Box breathing","box-breathing.mp4"));
        breathingExercisesList.add(new ChildModelClass(R.drawable.breathing_4_7_8, "4-7-8 method","4-7-8-breathing.mp4"));
        breathingExercisesList.add(new ChildModelClass(R.drawable.pursedlip_breathing, "Pursed-lip method","pursed-lip-breathing.mp4"));

        musicList.add(new ChildModelClass(R.drawable.music, "movie","4-7-8-breathing.mp4"));
        musicList.add(new ChildModelClass(R.drawable.music, "movie","4-7-8-breathing.mp4"));
        musicList.add(new ChildModelClass(R.drawable.music, "movie","4-7-8-breathing.mp4"));
        musicList.add(new ChildModelClass(R.drawable.music, "movie","4-7-8-breathing.mp4"));
        musicList.add(new ChildModelClass(R.drawable.music, "movie","4-7-8-breathing.mp4"));
        musicList.add(new ChildModelClass(R.drawable.music, "movie","4-7-8-breathing.mp4"));
        musicList.add(new ChildModelClass(R.drawable.music, "movie","4-7-8-breathing.mp4"));

        ExercisesAdapter breathingAdapter = new ExercisesAdapter(breathingExercisesList,ExercisesActivity.this);
        breathingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        breathingRecyclerView.setAdapter(breathingAdapter);

        ExercisesAdapter musicAdapter = new ExercisesAdapter(musicList,ExercisesActivity.this);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        musicRecyclerView.setAdapter(musicAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}