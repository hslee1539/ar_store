package com.example.cvtest03;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

public class ButtonClick implements View.OnClickListener {
    RecyclerView recyclerView;
    public ButtonClick(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
    }
    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        RecycleAdapter recycleAdapter = (RecycleAdapter) recyclerView.getAdapter();
        recycleAdapter.list.add(button.summary);
        recyclerView.invalidateItemDecorations();
        recyclerView.invalidate();
    }
}
