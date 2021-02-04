package com.mvproject.pdftestproject;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.mvproject.pdftestproject.databinding.FragmentMainBinding;
import com.mvproject.pdftestproject.text.MotionEntity;
import com.mvproject.pdftestproject.text.TextEntity;
import com.mvproject.pdftestproject.text.TextLayer;
import com.mvproject.pdftestproject.view.MotionView;

import java.io.IOException;
import java.util.List;

public class MainFragment extends Fragment {
    private static float modifierY = 200;
    private static float modifierX = 400;
    private FragmentMainBinding binding;
    private MainViewModel mainViewModel;
    //  private static final String FILENAME = "test_pdf_document.pdf";
    private static final String FILENAME = "cheet_sql.pdf";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        final MotionView.MotionViewCallback motionViewCallback = new MotionView.MotionViewCallback() {
            @Override
            public void onEntitySelected(@Nullable MotionEntity entity) {
                binding.scrollViewVertical.setScrolling(false);
                binding.scrollViewHorizontal.setScrolling(false);
            }

            @Override
            public void onEntityDoubleTap(@NonNull MotionEntity entity) {
                // startTextEntityEditing();
            }

            @Override
            public void onEntityUnSelected() {
                binding.scrollViewVertical.setScrolling(true);
                binding.scrollViewHorizontal.setScrolling(true);
            }
        };

        binding.motionView.setMotionViewCallback(motionViewCallback);
        renderPdf();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected void addTextSticker() {
        TextLayer textLayer = mainViewModel.createTextLayer();
        TextEntity textEntity = new TextEntity(
                textLayer,
                binding.motionView.getWidth(),
                binding.motionView.getHeight(),
                mainViewModel.getFontProvider()
        );

        binding.motionView.addEntityAndPosition(textEntity);

        // move text sticker up so that its not hidden under keyboard
        PointF center = textEntity.absoluteCenter();
        center.y = binding.scrollViewVertical.getScrollY() + modifierY;
        center.x = binding.scrollViewHorizontal.getScrollX() + modifierX;
        textEntity.moveCenterTo(center);

        // redraw
        binding.motionView.invalidate();
        Snackbar.make(binding.getRoot(), String.format(getString(R.string.msg_created), center.x, center.y), Snackbar.LENGTH_LONG).show();
    }

    private void renderPdf() {
        List<Bitmap> pages = mainViewModel.getPagesFromFile(FILENAME);
        if (pages.size() > 0) {
            for (int i = 0; i < pages.size(); i++) {
                getNewPageView(pages.get(i));
            }
        } else {
            Toast.makeText(requireContext(),getString(R.string.msg_error),Toast.LENGTH_LONG).show();
        }
    }

    public void getNewPageView(Bitmap bitmap) {
        final ImageView newPageView = new ImageView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 30, 0, 0);
        newPageView.setLayoutParams(lp);
        newPageView.setImageBitmap(bitmap);
        binding.linear.addView(newPageView);
    }
}