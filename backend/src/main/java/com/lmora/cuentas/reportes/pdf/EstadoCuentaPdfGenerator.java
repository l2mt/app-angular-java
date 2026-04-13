package com.lmora.cuentas.reportes.pdf;

import com.lmora.cuentas.reportes.dto.ReporteCuentaDto;
import com.lmora.cuentas.reportes.dto.ReporteEstadoCuentaResponseDto;
import com.lmora.cuentas.reportes.dto.ReporteMovimientoDto;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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

    private static final PDRectangle PAGE_SIZE = PDRectangle.LETTER;
    private static final float MARGIN = 40f;
    private static final float TOP = PAGE_SIZE.getHeight() - MARGIN;
    private static final float BOTTOM = MARGIN;
    private static final float CONTENT_WIDTH = PAGE_SIZE.getWidth() - (MARGIN * 2f);
    private static final float SECTION_GAP = 18f;
    private static final float CARD_GAP = 12f;
    private static final float SUMMARY_CARD_HEIGHT = 58f;
    private static final float ACCOUNT_HEADER_HEIGHT = 72f;
    private static final float TABLE_HEADER_HEIGHT = 24f;
    private static final float TABLE_ROW_HEIGHT = 22f;
    private static final float NOTICE_BOX_HEIGHT = 28f;
    private static final float CELL_PADDING = 8f;
    private static final float COL_DATE = 165f;
    private static final float COL_TYPE = 95f;
    private static final float COL_VALUE = 120f;
    private static final float COL_BALANCE = CONTENT_WIDTH - COL_DATE - COL_TYPE - COL_VALUE;

    private static final PDFont FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Color COLOR_TEXT = new Color(17, 24, 39);
    private static final Color COLOR_MUTED = new Color(107, 114, 128);
    private static final Color COLOR_BORDER = new Color(209, 213, 219);
    private static final Color COLOR_BRAND = new Color(29, 78, 216);
    private static final Color COLOR_BRAND_LIGHT = new Color(239, 246, 255);
    private static final Color COLOR_TABLE_HEADER = new Color(224, 231, 255);
    private static final Color COLOR_ROW_ALT = new Color(249, 250, 251);
    private static final Color COLOR_NOTICE = new Color(248, 250, 252);

    public byte[] generate(ReporteEstadoCuentaResponseDto reporte) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PageState pageState = newPage(document);
            pageState = drawReportHeader(document, pageState, reporte);

            if (reporte.cuentas().isEmpty()) {
                pageState = drawNoticeBox(document, pageState, "El cliente no tiene cuentas asociadas.");
            }

            for (ReporteCuentaDto cuenta : reporte.cuentas()) {
                pageState = drawCuentaSection(document, pageState, cuenta);
            }

            closePage(pageState);
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte", exception);
        }
    }

    private PageState newPage(PDDocument document) throws IOException {
        PDPage page = new PDPage(PAGE_SIZE);
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

    private PageState drawReportHeader(
            PDDocument document,
            PageState pageState,
            ReporteEstadoCuentaResponseDto reporte
    ) throws IOException {
        PageState adjustedPageState = ensureSpace(document, pageState, 150f);

        drawText(adjustedPageState, MARGIN, adjustedPageState.y, "BANCO", FONT_BOLD, 14f, COLOR_BRAND);
        adjustedPageState.y -= 28f;

        drawText(adjustedPageState, MARGIN, adjustedPageState.y, "Estado de cuenta", FONT_BOLD, 24f, COLOR_TEXT);
        adjustedPageState.y -= 30f;

        drawLabelValue(
                adjustedPageState,
                MARGIN,
                adjustedPageState.y,
                "Cliente",
                reporte.cliente().nombre() + " - " + reporte.cliente().identificacion(),
                10.5f
        );
        adjustedPageState.y -= 16f;

        drawLabelValue(
                adjustedPageState,
                MARGIN,
                adjustedPageState.y,
                "Rango",
                reporte.fechaDesde().format(DATE_FORMATTER) + " a " + reporte.fechaHasta().format(DATE_FORMATTER),
                10.5f
        );
        adjustedPageState.y -= 18f;

        drawHorizontalRule(adjustedPageState, adjustedPageState.y);
        adjustedPageState.y -= 14f;

        float cardWidth = (CONTENT_WIDTH - CARD_GAP) / 2f;
        drawSummaryCard(
                adjustedPageState,
                MARGIN,
                adjustedPageState.y,
                cardWidth,
                "Total creditos",
                formatAmount(reporte.totalCreditos())
        );
        drawSummaryCard(
                adjustedPageState,
                MARGIN + cardWidth + CARD_GAP,
                adjustedPageState.y,
                cardWidth,
                "Total debitos",
                formatAmount(reporte.totalDebitos())
        );
        adjustedPageState.y -= SUMMARY_CARD_HEIGHT + SECTION_GAP;
        return adjustedPageState;
    }

    private PageState drawCuentaSection(
            PDDocument document,
            PageState pageState,
            ReporteCuentaDto cuenta
    ) throws IOException {
        float minimumHeight = ACCOUNT_HEADER_HEIGHT + NOTICE_BOX_HEIGHT + SECTION_GAP;
        PageState adjustedPageState = ensureSpace(document, pageState, minimumHeight);

        drawCuentaHeader(adjustedPageState, cuenta);
        adjustedPageState.y -= ACCOUNT_HEADER_HEIGHT + 12f;

        if (cuenta.movimientos().isEmpty()) {
            adjustedPageState = drawNoticeBox(document, adjustedPageState, "Sin movimientos en el rango consultado.");
            adjustedPageState.y -= SECTION_GAP;
            return adjustedPageState;
        }

        adjustedPageState = drawTableHeader(document, adjustedPageState);

        for (int index = 0; index < cuenta.movimientos().size(); index++) {
            if (adjustedPageState.y - TABLE_ROW_HEIGHT < BOTTOM) {
                adjustedPageState = newPage(document);
                drawText(
                        adjustedPageState,
                        MARGIN,
                        adjustedPageState.y,
                        "Cuenta " + cuenta.numeroCuenta() + " - " + cuenta.tipoCuenta() + " (continuacion)",
                        FONT_BOLD,
                        11f,
                        COLOR_BRAND
                );
                adjustedPageState.y -= 18f;
                adjustedPageState = drawTableHeader(document, adjustedPageState);
            }

            drawTableRow(adjustedPageState, cuenta.movimientos().get(index), index);
            adjustedPageState.y -= TABLE_ROW_HEIGHT;
        }

        adjustedPageState.y -= SECTION_GAP;
        return adjustedPageState;
    }

    private void drawCuentaHeader(PageState pageState, ReporteCuentaDto cuenta) throws IOException {
        float top = pageState.y;
        strokeRect(pageState, MARGIN, top, CONTENT_WIDTH, ACCOUNT_HEADER_HEIGHT, COLOR_BORDER, 1f);
        fillRect(pageState, MARGIN, top, CONTENT_WIDTH, 24f, COLOR_BRAND);

        drawText(
                pageState,
                MARGIN + CELL_PADDING,
                top - 16f,
                "Cuenta " + cuenta.numeroCuenta() + " - " + cuenta.tipoCuenta(),
                FONT_BOLD,
                12f,
                Color.WHITE
        );

        float leftColumnX = MARGIN + CELL_PADDING;
        float rightColumnX = MARGIN + (CONTENT_WIDTH / 2f) + 6f;

        drawLabelValue(pageState, leftColumnX, top - 40f, "Saldo inicial", formatAmount(cuenta.saldoInicial()), 10f);
        drawLabelValue(
                pageState,
                rightColumnX,
                top - 40f,
                "Saldo disponible",
                formatAmount(cuenta.saldoDisponible()),
                10f
        );
        drawLabelValue(pageState, leftColumnX, top - 58f, "Creditos", formatAmount(cuenta.totalCreditos()), 10f);
        drawLabelValue(pageState, rightColumnX, top - 58f, "Debitos", formatAmount(cuenta.totalDebitos()), 10f);
    }

    private PageState drawTableHeader(PDDocument document, PageState pageState) throws IOException {
        PageState adjustedPageState = ensureSpace(document, pageState, TABLE_HEADER_HEIGHT + TABLE_ROW_HEIGHT);
        float top = adjustedPageState.y;

        fillRect(adjustedPageState, MARGIN, top, CONTENT_WIDTH, TABLE_HEADER_HEIGHT, COLOR_TABLE_HEADER);
        strokeRect(adjustedPageState, MARGIN, top, CONTENT_WIDTH, TABLE_HEADER_HEIGHT, COLOR_BORDER, 1f);
        drawTableColumnSeparators(adjustedPageState, top, TABLE_HEADER_HEIGHT);

        drawText(adjustedPageState, MARGIN + CELL_PADDING, top - 15f, "Fecha", FONT_BOLD, 9.5f, COLOR_TEXT);
        drawText(
                adjustedPageState,
                MARGIN + COL_DATE + CELL_PADDING,
                top - 15f,
                "Tipo",
                FONT_BOLD,
                9.5f,
                COLOR_TEXT
        );
        drawText(
                adjustedPageState,
                MARGIN + COL_DATE + COL_TYPE + CELL_PADDING,
                top - 15f,
                "Valor",
                FONT_BOLD,
                9.5f,
                COLOR_TEXT
        );
        drawText(
                adjustedPageState,
                MARGIN + COL_DATE + COL_TYPE + COL_VALUE + CELL_PADDING,
                top - 15f,
                "Saldo",
                FONT_BOLD,
                9.5f,
                COLOR_TEXT
        );

        adjustedPageState.y -= TABLE_HEADER_HEIGHT;
        return adjustedPageState;
    }

    private void drawTableRow(PageState pageState, ReporteMovimientoDto movimiento, int index) throws IOException {
        float top = pageState.y;
        Color background = index % 2 == 0 ? Color.WHITE : COLOR_ROW_ALT;

        fillRect(pageState, MARGIN, top, CONTENT_WIDTH, TABLE_ROW_HEIGHT, background);
        strokeRect(pageState, MARGIN, top, CONTENT_WIDTH, TABLE_ROW_HEIGHT, COLOR_BORDER, 1f);
        drawTableColumnSeparators(pageState, top, TABLE_ROW_HEIGHT);

        drawText(
                pageState,
                MARGIN + CELL_PADDING,
                top - 15f,
                movimiento.fecha().format(DATE_TIME_FORMATTER),
                FONT_REGULAR,
                9f,
                COLOR_TEXT
        );
        drawText(
                pageState,
                MARGIN + COL_DATE + CELL_PADDING,
                top - 15f,
                formatTipoMovimiento(movimiento),
                FONT_REGULAR,
                9f,
                COLOR_TEXT
        );
        drawRightAlignedText(
                pageState,
                MARGIN + COL_DATE + COL_TYPE + COL_VALUE - CELL_PADDING,
                top - 15f,
                formatAmount(movimiento.valor()),
                FONT_REGULAR,
                9f,
                COLOR_TEXT
        );
        drawRightAlignedText(
                pageState,
                MARGIN + CONTENT_WIDTH - CELL_PADDING,
                top - 15f,
                formatAmount(movimiento.saldo()),
                FONT_REGULAR,
                9f,
                COLOR_TEXT
        );
    }

    private PageState drawNoticeBox(PDDocument document, PageState pageState, String text) throws IOException {
        PageState adjustedPageState = ensureSpace(document, pageState, NOTICE_BOX_HEIGHT);
        fillRect(adjustedPageState, MARGIN, adjustedPageState.y, CONTENT_WIDTH, NOTICE_BOX_HEIGHT, COLOR_NOTICE);
        strokeRect(adjustedPageState, MARGIN, adjustedPageState.y, CONTENT_WIDTH, NOTICE_BOX_HEIGHT, COLOR_BORDER, 1f);
        drawText(adjustedPageState, MARGIN + CELL_PADDING, adjustedPageState.y - 18f, text, FONT_REGULAR, 10f, COLOR_MUTED);
        adjustedPageState.y -= NOTICE_BOX_HEIGHT;
        return adjustedPageState;
    }

    private void drawSummaryCard(
            PageState pageState,
            float x,
            float top,
            float width,
            String title,
            String value
    ) throws IOException {
        fillRect(pageState, x, top, width, SUMMARY_CARD_HEIGHT, COLOR_BRAND_LIGHT);
        strokeRect(pageState, x, top, width, SUMMARY_CARD_HEIGHT, COLOR_BORDER, 1f);
        drawText(pageState, x + CELL_PADDING, top - 16f, title, FONT_BOLD, 9f, COLOR_MUTED);
        drawText(pageState, x + CELL_PADDING, top - 38f, value, FONT_BOLD, 16f, COLOR_TEXT);
    }

    private void drawLabelValue(
            PageState pageState,
            float x,
            float baseline,
            String label,
            String value,
            float fontSize
    ) throws IOException {
        String labelText = label + ": ";
        drawText(pageState, x, baseline, labelText, FONT_BOLD, fontSize, COLOR_MUTED);
        float labelWidth = textWidth(labelText, FONT_BOLD, fontSize);
        drawText(pageState, x + labelWidth, baseline, value, FONT_REGULAR, fontSize, COLOR_TEXT);
    }

    private void drawHorizontalRule(PageState pageState, float y) throws IOException {
        pageState.contentStream.setStrokingColor(COLOR_BORDER);
        pageState.contentStream.setLineWidth(1f);
        pageState.contentStream.moveTo(MARGIN, y);
        pageState.contentStream.lineTo(MARGIN + CONTENT_WIDTH, y);
        pageState.contentStream.stroke();
    }

    private void drawTableColumnSeparators(PageState pageState, float top, float height) throws IOException {
        drawVerticalLine(pageState, MARGIN + COL_DATE, top, height);
        drawVerticalLine(pageState, MARGIN + COL_DATE + COL_TYPE, top, height);
        drawVerticalLine(pageState, MARGIN + COL_DATE + COL_TYPE + COL_VALUE, top, height);
    }

    private void drawVerticalLine(PageState pageState, float x, float top, float height) throws IOException {
        pageState.contentStream.setStrokingColor(COLOR_BORDER);
        pageState.contentStream.setLineWidth(1f);
        pageState.contentStream.moveTo(x, top);
        pageState.contentStream.lineTo(x, top - height);
        pageState.contentStream.stroke();
    }

    private void fillRect(PageState pageState, float x, float top, float width, float height, Color color) throws IOException {
        pageState.contentStream.setNonStrokingColor(color);
        pageState.contentStream.addRect(x, top - height, width, height);
        pageState.contentStream.fill();
    }

    private void strokeRect(
            PageState pageState,
            float x,
            float top,
            float width,
            float height,
            Color color,
            float lineWidth
    ) throws IOException {
        pageState.contentStream.setStrokingColor(color);
        pageState.contentStream.setLineWidth(lineWidth);
        pageState.contentStream.addRect(x, top - height, width, height);
        pageState.contentStream.stroke();
    }

    private void drawText(
            PageState pageState,
            float x,
            float y,
            String text,
            PDFont font,
            float fontSize,
            Color color
    ) throws IOException {
        pageState.contentStream.beginText();
        pageState.contentStream.setNonStrokingColor(color);
        pageState.contentStream.setFont(font, fontSize);
        pageState.contentStream.newLineAtOffset(x, y);
        pageState.contentStream.showText(text == null ? "" : text);
        pageState.contentStream.endText();
    }

    private void drawRightAlignedText(
            PageState pageState,
            float rightX,
            float y,
            String text,
            PDFont font,
            float fontSize,
            Color color
    ) throws IOException {
        float textWidth = textWidth(text, font, fontSize);
        drawText(pageState, rightX - textWidth, y, text, font, fontSize, color);
    }

    private void closePage(PageState pageState) throws IOException {
        pageState.contentStream.close();
    }

    private float textWidth(String text, PDFont font, float fontSize) throws IOException {
        String safeText = text == null ? "" : text;
        return (font.getStringWidth(safeText) / 1000f) * fontSize;
    }

    private String formatAmount(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        BigDecimal scaledAmount = safeAmount.setScale(2, RoundingMode.HALF_UP);

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setMaximumFractionDigits(2);
        return currencyFormatter.format(scaledAmount);
    }

    private String formatTipoMovimiento(ReporteMovimientoDto movimiento) {
        if (movimiento.tipoMovimiento() == null) {
            return "";
        }

        return switch (movimiento.tipoMovimiento()) {
            case CREDITO -> "Credito";
            case DEBITO -> "Debito";
        };
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
