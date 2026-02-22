package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : QrCodeService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Service for QR code generation
 * </pre>
 */

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class QrCodeService {
    
    private static final int QR_CODE_SIZE = 300;
    private static final String QR_CODE_IMAGE_TYPE = "PNG";
    
    public String generateQrCodeBase64(String content) {
        try {
            byte[] qrCodeImage = generateQrCode(content);
            return Base64.getEncoder().encodeToString(qrCodeImage);
        } catch (Exception e) {
            log.error("Failed to generate QR code", e);
            return null;
        }
    }
    
    public byte[] generateQrCode(String content) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                QR_CODE_SIZE,
                QR_CODE_SIZE,
                hints
        );
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, QR_CODE_IMAGE_TYPE, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to write QR code to stream", e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}