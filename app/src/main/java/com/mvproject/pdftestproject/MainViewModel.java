package com.mvproject.pdftestproject;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.android.material.snackbar.Snackbar;
import com.mvproject.pdftestproject.text.TextLayer;
import com.mvproject.pdftestproject.utils.Font;
import com.mvproject.pdftestproject.utils.FontProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final FontProvider fontProvider;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.fontProvider = new FontProvider(getApplication().getResources());
    }

    TextLayer createTextLayer() {
        TextLayer textLayer = new TextLayer();
        Font font = new Font();

        font.setColor(TextLayer.Limits.INITIAL_FONT_COLOR);
        font.setSize(TextLayer.Limits.INITIAL_FONT_SIZE);
        font.setTypeface(fontProvider.getDefaultFontName());
        textLayer.setFont(font);
        textLayer.setText("Sign here");

        return textLayer;
    }

    FontProvider getFontProvider() {
        return this.fontProvider;
    }

    List<Bitmap> getPagesFromFile(String fileName) {
        List<Bitmap> pages = new ArrayList<>();
        ParcelFileDescriptor parcelFileDescriptor = loadPdfFromAssets(fileName);
        try {
            if (parcelFileDescriptor != null) {
                float currentZoomLevel = 9;
                PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);
                for (int i = 0; i < renderer.getPageCount(); i++) {
                    PdfRenderer.Page page = renderer.openPage(i);
                    int newWidth = (int) (getApplication().getResources().getDisplayMetrics().widthPixels * page.getWidth() / 72 * currentZoomLevel / 40);
                    int newHeight = (int) (getApplication().getResources().getDisplayMetrics().heightPixels * page.getHeight() / 72 * currentZoomLevel / 64);
                    Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                    pages.add(bitmap);
                    page.close();
                }
                renderer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pages;
    }

    private ParcelFileDescriptor loadPdfFromAssets(String fileName) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            File file = new File(getApplication().getCacheDir(), fileName);
            if (!file.exists()) {
                // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
                // the cache directory.
                InputStream asset = getApplication().getAssets().open(fileName);
                FileOutputStream output = new FileOutputStream(file);
                final byte[] buffer = new byte[1024];
                int size;
                while ((size = asset.read(buffer)) != -1) {
                    output.write(buffer, 0, size);
                }
                asset.close();
                output.close();
            }
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parcelFileDescriptor;
    }
}