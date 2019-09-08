package de.reynok.authentication.core.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageHelper {

    public static byte[] createQrCode(String from) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix    bitMatrix    = qrCodeWriter.encode(from, BarcodeFormat.QR_CODE, 512, 512);

            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", bos);

            byte[] result = bos.toByteArray();
            bos.close();
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
