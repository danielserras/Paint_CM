package com.example.paint;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Palette#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Palette extends Fragment {


    public Palette() {
        // Required empty public constructor
    }

    public static Palette newInstance(String param1, String param2) {
        Palette fragment = new Palette();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_palette, container, false);

    }

}