package com.lmora.cuentas.reportes.pdf;

import com.lmora.cuentas.reportes.dto.ReporteCuentaDto;
import com.lmora.cuentas.reportes.dto.ReporteEstadoCuentaResponseDto;
import com.lmora.cuentas.reportes.dto.ReporteMovimientoDto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

@Component
public class EstadoCuentaPdfGenerator {

    private static final float MARGIN = 50f;
    private static final float TOP = 750f;
    private static final float BOTTOM = 50f;
    private static final float DEFAULT_LINE_HEIGHT = 14f;
    private static final PDFont FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] generate(ReporteEstadoCuentaResponseDto reporte) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PageState pageState = newPage(document);

            pageState = writeLine(document, pageState, "Estado de cuenta", 16f, FONT_BOLD, 22f);
            pageState = writeLine(
                    document,
                    pageState,
                    "Cliente: " + reporte.cliente().nombre() + " - " + reporte.cliente().identificacion(),
                    11f,
                    FONT_REGULAR,
                    DEFAULT_LINE_HEIGHT
            );
            pageState = writeLine(
                    document,
                    pageState,
                    "Rango: " + reporte.fechaDesde().format(DATE_FORMATTER) + " a "
                            + reporte.fechaHasta().format(DATE_FORMATTER),
                    11f,
                    FONT_REGULAR,
                    DEFAULT_LINE_HEIGHT
            );
            pageState = writeLine(
                    document,
                    pageState,
                    "Total creditos: " + formatAmount(reporte.totalCreditos())
                            + " | Total debitos: " + formatAmount(reporte.totalDebitos()),
                    11f,
                    FONT_BOLD,
                    18f
            );

            if (reporte.cuentas().isEmpty()) {
                pageState = writeLine(
                        document,
                        pageState,
                        "El cliente no tiene cuentas asociadas.",
                        11f,
                        FONT_REGULAR,
                        DEFAULT_LINE_HEIGHT
                );
            }

            for (ReporteCuentaDto cuenta : reporte.cuentas()) {
                pageState = writeLine(document, pageState, "", 10f, FONT_REGULAR, 8f);
                pageState = writeLine(
                        document,
                        pageState,
                        "Cuenta " + cuenta.numeroCuenta() + " - " + cuenta.tipoCuenta(),
                        12f,
                        FONT_BOLD,
                        16f
                );
                pageState = writeLine(
                        document,
                        pageState,
                        "Saldo inicial: " + formatAmount(cuenta.saldoInicial())
                                + " | Saldo disponible: " + formatAmount(cuenta.saldoDisponible()),
                        10f,
                        FONT_REGULAR,
                        DEFAULT_LINE_HEIGHT
                );
                pageState = writeLine(
                        document,
                        pageState,
                        "Creditos: " + formatAmount(cuenta.totalCreditos())
                                + " | Debitos: " + formatAmount(cuenta.totalDebitos()),
                        10f,
                        FONT_REGULAR,
                        DEFAULT_LINE_HEIGHT
                );

                if (cuenta.movimientos().isEmpty()) {
                    pageState = writeLine(
                            document,
                            pageState,
                            "Sin movimientos en el rango consultado.",
                            10f,
                            FONT_REGULAR,
                            DEFAULT_LINE_HEIGHT
                    );
                    continue;
                }

                pageState = writeLine(
                        document,
                        pageState,
                        "Fecha              Tipo      Valor        Saldo",
                        10f,
                        FONT_BOLD,
                        DEFAULT_LINE_HEIGHT
                );

                for (ReporteMovimientoDto movimiento : cuenta.movimientos()) {
                    pageState = writeLine(
                            document,
                            pageState,
                            formatMovimiento(movimiento),
                            9f,
                            FONT_REGULAR,
                            12f
                    );
                }
            }

            closePage(pageState);
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte", exception);
        }
    }

    private PageState newPage(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);
        return new PageState(new PDPageContentStream(document, page), TOP);
    }

    private PageState ensureSpace(PDDocument document, PageState pageState, float requiredHeight) throws IOException {
        if (pageState.y - requiredHeight >= BOTTOM) {
            return pageState;
        }

        closePage(pageState);
        return newPage(document);
    }

    private PageState writeLine(
            PDDocument document,
            PageState pageState,
            String text,
            float fontSize,
            PDFont font,
            float lineHeight
    ) throws IOException {
        PageState adjustedPageState = ensureSpace(document, pageState, lineHeight);
        adjustedPageState.contentStream.beginText();
        adjustedPageState.contentStream.setFont(font, fontSize);
        adjustedPageState.contentStream.newLineAtOffset(MARGIN, adjustedPageState.y);
        adjustedPageState.contentStream.showText(text == null ? "" : text);
        adjustedPageState.contentStream.endText();
        adjustedPageState.y -= lineHeight;
        return adjustedPageState;
    }

    private void closePage(PageState pageState) throws IOException {
        pageState.contentStream.close();
    }

    private String formatMovimiento(ReporteMovimientoDto movimiento) {
        return String.format(
                "%s  %-8s  %10s  %10s",
                movimiento.fecha().format(DATE_TIME_FORMATTER),
                movimiento.tipoMovimiento(),
                formatAmount(movimiento.valor()),
                formatAmount(movimiento.saldo())
        );
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }

        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static final class PageState {
        private final PDPageContentStream contentStream;
        private float y;

        private PageState(PDPageContentStream contentStream, float y) {
            this.contentStream = contentStream;
            this.y = y;
        }
    }
}
