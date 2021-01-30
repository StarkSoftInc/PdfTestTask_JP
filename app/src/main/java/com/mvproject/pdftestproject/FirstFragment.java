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

import com.google.android.material.snackbar.Snackbar;
import com.mvproject.pdftestproject.databinding.FragmentFirstBinding;
import com.mvproject.pdftestproject.text.MotionEntity;
import com.mvproject.pdftestproject.text.TextEntity;
import com.mvproject.pdftestproject.text.TextLayer;
import com.mvproject.pdftestproject.utils.Font;
import com.mvproject.pdftestproject.utils.FontProvider;
import com.mvproject.pdftestproject.view.MotionView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirstFragment extends Fragment {
    private float currentZoomLevel = 12;
    private FragmentFirstBinding binding;
    private FontProvider fontProvider;
    private static final String FILENAME = "test_pdf_document.pdf";

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

        try {
            File file = new File(requireContext().getCacheDir(), FILENAME);
            if (!file.exists()) {
                // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
                // the cache directory.
                InputStream asset = requireContext().getAssets().open(FILENAME);
                FileOutputStream output = new FileOutputStream(file);
                final byte[] buffer = new byte[1024];
                int size;
                while ((size = asset.read(buffer)) != -1) {
                    output.write(buffer, 0, size);
                }
                asset.close();
                output.close();
            }
            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            // This is the PdfRenderer we use to render the PDF.
            if (parcelFileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);
                PdfRenderer.Page page = renderer.openPage(0);
                int newWidth = (int) (getResources().getDisplayMetrics().widthPixels * page.getWidth() / 72 * currentZoomLevel / 40);
                int newHeight = (int) (getResources().getDisplayMetrics().heightPixels * page.getHeight() / 72 * currentZoomLevel / 64);
                Bitmap bitmap = Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);
                page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                binding.image.setImageBitmap(bitmap);
                page.close();
                renderer.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

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