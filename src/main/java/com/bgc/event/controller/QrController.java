package com.bgc.event.controller;

/**
 * <pre>
 * - Project : BGC EVENT
 * - File    : QrController.java
 * - Date    : 2026-03-11
 * - Author  : NTAGANIRA Heritier
 * - Desc    : Serves QR code images for users and events.
 *             GET /qr/user/{id}  → PNG QR of the user's unique userCode
 *             GET /qr/event/{id} → PNG QR of the event's qrCodeValue
 * </pre>
 */

import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;
import com.bgc.event.service.EventService;
import com.bgc.event.service.UserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
public class QrController {

    private final UserService  userService;
    private final EventService eventService;

    /** QR code for a user — encodes their unique userCode */
    @GetMapping(value = "/user/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> userQr(@PathVariable Long id,
                                          @RequestParam(defaultValue = "220") int size) {
        User user = userService.findById(id).orElseThrow();
        String content = user.getUserCode() != null ? user.getUserCode() : "BGC-USER-" + id;
        return qrResponse(content, size);
    }

    /** QR code for an event — encodes the event's qrCodeValue */
    @GetMapping(value = "/event/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> eventQr(@PathVariable Long id,
                                           @RequestParam(defaultValue = "220") int size) {
        Event event = eventService.findById(id).orElseThrow();
        String content = event.getQrCodeValue() != null ? event.getQrCodeValue() : "BGC-EVENT-" + id;
        return qrResponse(content, size);
    }

    // ── Shared QR generation ──────────────────────────────────────────
    private ResponseEntity<byte[]> qrResponse(String content, int size) {
        try {
            byte[] png = generateQr(content, Math.min(Math.max(size, 80), 600));
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header("Cache-Control", "max-age=3600")
                .body(png);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] generateQr(String content, int size) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = Map.of(
            EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN, 1
        );
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }
}
