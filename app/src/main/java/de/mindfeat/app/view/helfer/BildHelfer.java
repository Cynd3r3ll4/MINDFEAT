package de.mindfeat.app.view.helfer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;

public class BildHelfer {

    private BildHelfer() {
        // Keine Instanziierung erforderlich.
    }

    // Ein Bitmap anhand der gespeicherten EXIF-Orientierung ausrichten
    public static Bitmap orientieren(Bitmap bitmap, String bildPfad) {
        if (bitmap == null || bildPfad == null || bildPfad.trim().isEmpty()) {
            return bitmap;
        }
        try {
            ExifInterface exif = new ExifInterface(bildPfad);
            int orientierung = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientierung) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(270);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    return bitmap;
            }
            Bitmap korrigiertesBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (korrigiertesBitmap != bitmap) {
                bitmap.recycle();
            }
            return korrigiertesBitmap;
        } catch (IOException | IllegalArgumentException exception) { // Bei fehlenden oder ungültigen EXIF-Daten das Originalbild verwenden
            return bitmap;
        }
    }

    // Bild in kleiner Auflösung laden und korrigieren der Ausrichtung anhand der EXIF-Daten.
    public static Bitmap skaliertLaden(String bildPfad, int zielBreite, int zielHoehe) {
        if (bildPfad == null || bildPfad.trim().isEmpty()
                || zielBreite <= 0 || zielHoehe <= 0) {
            return null;
        }
        BitmapFactory.Options optionen = new BitmapFactory.Options(); // Bildabmessungen ermitteln
        optionen.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bildPfad, optionen);
        if (optionen.outWidth <= 0 || optionen.outHeight <= 0) {
            return null;
        }
        int skalierungsfaktor = 1; // Passenden Verkleinerungsfaktor bestimmen
        int halbeBreite = optionen.outWidth / 2;
        int halbeHoehe = optionen.outHeight / 2;
        while ((halbeBreite / skalierungsfaktor) >= zielBreite && (halbeHoehe / skalierungsfaktor) >= zielHoehe) {
            skalierungsfaktor *= 2;
        }
        optionen.inJustDecodeBounds = false; // Bild mit reduzierter Auflösung laden
        optionen.inSampleSize = skalierungsfaktor;
        Bitmap bitmap = BitmapFactory.decodeFile(bildPfad, optionen);
        if (bitmap == null) {
            return null;
        }
        return orientieren(bitmap, bildPfad);
    }
}
