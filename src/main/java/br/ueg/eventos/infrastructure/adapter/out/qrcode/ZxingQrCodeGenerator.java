package br.ueg.eventos.infrastructure.adapter.out.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Gera a imagem PNG exibida no comprovante da inscrição. */
public class ZxingQrCodeGenerator {

    public byte[] gerarPng(String conteudo) throws WriterException, IOException {
        Map<EncodeHintType, Object> opcoes = new HashMap<>();
        opcoes.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        opcoes.put(EncodeHintType.MARGIN, 2);

        var matriz = new QRCodeWriter().encode(
                conteudo,
                BarcodeFormat.QR_CODE,
                320,
                320,
                opcoes
        );

        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matriz, "PNG", saida);
            return saida.toByteArray();
        }
    }
}
