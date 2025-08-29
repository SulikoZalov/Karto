package org.project.karto.infrastructure.qr;

import boofcv.alg.fiducial.qrcode.QrCode;
import boofcv.alg.fiducial.qrcode.QrCodeEncoder;
import boofcv.alg.fiducial.qrcode.QrCodeGeneratorImage;
import boofcv.struct.image.GrayU8;
import org.project.karto.application.dto.common.QR;
import org.project.karto.application.dto.gift_card.PaymentQRDTO;
import org.project.karto.domain.common.containers.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class QRGenerator {

  private QRGenerator() {}

  public static Result<QR, Throwable> generate(PaymentQRDTO dto) {
    try {
      QrCode qr = new QrCodeEncoder()
              .setError(QrCode.ErrorLevel.M)
              .addAutomatic(dto.toJson())
              .fixate();

      QrCodeGeneratorImage render = new QrCodeGeneratorImage(20);
      GrayU8 gray = render.render(qr).getGray();

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        writePGM(gray, baos);
        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        return Result.success(new QR(base64));
      }

    } catch (Throwable e) {
      return Result.failure(new QRGenerationException("Failed to generate QR code."));
    }
  }

  private static void writePGM(GrayU8 gray, ByteArrayOutputStream baos) throws IOException {
    String header = String.format("P5\n%d %d\n255\n", gray.width, gray.height);
    baos.write(header.getBytes(UTF_8));
    baos.write(gray.data);
  }
}
