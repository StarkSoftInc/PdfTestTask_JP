package com.mvproject.pdftestproject;

import android.app.Application;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.mvproject.pdftestproject.text.TextLayer;
import com.mvproject.pdftestproject.utils.Font;
import com.mvproject.pdftestproject.utils.FontProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    FontProvider getFontProvider(){
        return this.fontProvider;
    }

    ParcelFileDescriptor loadPdfFromAssets(String fileName) {
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
            // This is the PdfRenderer we use to render the PDF.
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parcelFileDescriptor;
    }
}