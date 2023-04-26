package com.gabidbr.mindzen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExercisesAdapter extends RecyclerView.Adapter<ExercisesAdapter.ViewHolder> {
    private List<ChildModelClass> childModelClassList;
    Context context;

    public ExercisesAdapter(List<ChildModelClass> childModelClassList, Context context) {
        this.childModelClassList = childModelClassList;
        this.context = context;
    }

    @Override
    public ExercisesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.child_rv_layout, null, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ExercisesAdapter.ViewHolder holder, int position) {
        ChildModelClass child = childModelClassList.get(position);

        holder.imageView.setImageResource(child.image);
        holder.textView.setText(child.descriptionText);

        // Set click listener to start the video activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoUrl = child.getVideoName();

                // Start the video activity with the video URL
                Intent intent = new Intent(context, VideoActivity.class);
                intent.putExtra("videoName", videoUrl);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childModelClassList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_child_item);
            textView = itemView.findViewById(R.id.textView_child_item);

        }

    }
}
