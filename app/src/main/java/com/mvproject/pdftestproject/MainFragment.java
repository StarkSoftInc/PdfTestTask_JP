package com.mvproject.pdftestproject;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class MainFragment extends Fragment {
    private static float modifierY = 0.07F;
    private static float modifierX = 0.35F;
    private FragmentMainBinding binding;
    private MainViewModel mainViewModel;
    private static final String FILENAME = "test_pdf_document.pdf";

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

        ParcelFileDescriptor pdfDescriptor = mainViewModel.loadPdfFromAssets(FILENAME);
        renderPdf(pdfDescriptor);
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
        center.y = center.y * modifierY;
        center.x = center.x * modifierX;
        textEntity.moveCenterTo(center);

        // redraw
        binding.motionView.invalidate();
        Snackbar.make(binding.getRoot(), String.format(getString(R.string.msg_created) ,center.x,center.y), Snackbar.LENGTH_LONG).show();
    }

    private void renderPdf(ParcelFileDescriptor parcelFileDescriptor) {
        try {
            if (parcelFileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);
                PdfRenderer.Page page = renderer.openPage(0);
                float currentZoomLevel = 12;
                int newWidth = (int) (getResources().getDisplayMetrics().widthPixels * page.getWidth() / 72 * currentZoomLevel / 40);
                int newHeight = (int) (getResources().getDisplayMetrics().heightPixels * page.getHeight() / 72 * currentZoomLevel / 64);
                Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                binding.image.setImageBitmap(bitmap);
                page.close();
                renderer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(binding.getRoot(), getString(R.string.msg_error), Snackbar.LENGTH_LONG).show();
        }
    }
}