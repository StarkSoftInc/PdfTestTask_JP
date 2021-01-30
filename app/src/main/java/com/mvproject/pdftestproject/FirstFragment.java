package com.mvproject.pdftestproject;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.mvproject.pdftestproject.databinding.FragmentFirstBinding;
import com.mvproject.pdftestproject.text.TextEntity;
import com.mvproject.pdftestproject.text.TextLayer;
import com.mvproject.pdftestproject.utils.Font;
import com.mvproject.pdftestproject.utils.FontProvider;

public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;
    private FontProvider fontProvider;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.fontProvider = new FontProvider(getResources());

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected void addTextSticker() {
        TextLayer textLayer = createTextLayer();
        TextEntity textEntity = new TextEntity(
                textLayer,
                binding.motionView.getWidth(),
                binding.motionView.getHeight(),
                fontProvider
        );

        binding.motionView.addEntityAndPosition(textEntity);

        // move text sticker up so that its not hidden under keyboard
        PointF center = textEntity.absoluteCenter();
        center.y = center.y * 0.2F;
        textEntity.moveCenterTo(center);

        // redraw
        binding.motionView.invalidate();
        Snackbar.make(binding.getRoot(), "Created at " + center.x + ", " + center.y, Snackbar.LENGTH_LONG)
          .setAction("Action", null).show();
    }


    private TextLayer createTextLayer() {
        TextLayer textLayer = new TextLayer();
        Font font = new Font();

        font.setColor(TextLayer.Limits.INITIAL_FONT_COLOR);
        font.setSize(TextLayer.Limits.INITIAL_FONT_SIZE);
        font.setTypeface(fontProvider.getDefaultFontName());
        textLayer.setFont(font);
        textLayer.setText("Sign here");

        return textLayer;
    }
}