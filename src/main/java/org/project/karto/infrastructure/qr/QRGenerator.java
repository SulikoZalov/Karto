package org.project.karto.infrastructure.qr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.application.dto.common.QR;
import org.project.karto.application.dto.gift_card.PaymentQRDTO;
import org.project.karto.domain.common.containers.Result;

import java.io.ByteArrayOutputStream;

@ApplicationScoped
public class QRGenerator {

  private static final int WIDTH = 250;
  private static final int HEIGHT = 250;
  private static final String IMAGE_FORMAT = "PNG";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public Result<QR, Throwable> generate(PaymentQRDTO dto) {
    try {
      String json = objectMapper.writeValueAsString(dto);

      BitMatrix matrix = new MultiFormatWriter()
          .encode(json, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        MatrixToImageWriter.writeToStream(matrix, IMAGE_FORMAT, baos);
        return Result.success(new QR(baos.toByteArray()));
      }

    } catch (Exception e) {
      return Result.failure(new QRGenerationException("Failed to generate QR code."));
    }
  }
}
