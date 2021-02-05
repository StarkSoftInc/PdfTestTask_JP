package com.mvproject.pdftestproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.util.List;

public class MainFragment extends Fragment {
    private static final int PICK_PDF_FILE = 2;
    private static final String TYPE_PDF = "application/pdf";

    private static float modifierY = 200;
    private static float modifierX = 400;
    private FragmentMainBinding binding;
    private MainViewModel mainViewModel;
    private static final String FILENAME = "cheet_sql.pdf";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
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
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open) {
            openFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(TYPE_PDF);
        startActivityForResult(intent, PICK_PDF_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == PICK_PDF_FILE
                && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Handler mHandler = new Handler();
                Uri uri = resultData.getData();
                final int takeFlags = resultData.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                requireActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                try{
                    ParcelFileDescriptor parcelFileDescriptor = requireActivity().getContentResolver().openFileDescriptor(uri, "r");
                    renderPdf(mainViewModel.getPagesFromFile(parcelFileDescriptor));
                } catch (Exception ex) {
                    Snackbar.make(binding.getRoot(), getString(R.string.msg_error), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    protected void addTextSticker() {
        if (binding.linear.getChildCount() > 0) {
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
        } else {
            Snackbar.make(binding.getRoot(), getString(R.string.msg_file_not_opened), Snackbar.LENGTH_LONG).show();
        }
    }

    private void renderPdf(List<Bitmap> pages) {
        if (pages.size() > 0) {
            binding.linear.removeAllViews();
            for (int i = 0; i < pages.size(); i++) {
                getNewPageView(pages.get(i));
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.msg_error), Toast.LENGTH_LONG).show();
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