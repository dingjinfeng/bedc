package acquire.core.esc;


import android.graphics.Bitmap;
import android.graphics.Paint;

import java.util.Vector;

import acquire.base.utils.StringUtils;
import acquire.core.esc.EscCommand.ENABLE;
import acquire.core.esc.EscCommand.FONT;
import acquire.core.esc.EscCommand.HEIGHT_ZOOM;
import acquire.core.esc.EscCommand.JUSTIFICATION;
import acquire.core.esc.EscCommand.WIDTH_ZOOM;

/**
 * create esc command data by text or image.
 *
 * @author Janson
 * @date 2023/4/21 10:40
 */
public class EscDraw {
    private final EscCommand esc = new EscCommand();

    /**
     * Indicates how many characters can be stored in each line.
     * Modify it based on the actual character set.
     */
    private final static int PAPER_CHARACTER = 32;

    public byte[] getEscCommand() {
        Vector<Byte> command = esc.getEscCommand();
        byte[] data = new byte[command.size()];
        if (command.size() > 0) {
            for (int i = 0; i < command.size(); ++i) {
                data[i] = command.get(i);
            }
        }
        return data;
    }

    public void text(String text, Paint.Align align,boolean bold, float textSize) {
        esc.addTurnEmphasizedModeOnOrOff(bold? ENABLE.ON:ENABLE.OFF);
        esc.addTurnDoubleStrikeOnOrOff(bold?ENABLE.ON:ENABLE.OFF);
        esc.addSetCharcterSize(pix2EscZoomWidth(textSize), pix2EscZoomHeight(textSize));
        esc.addSetFontForHRICharacter(FONT.FONTA);
        switch (align) {
            case CENTER:
                esc.addSelectJustification(JUSTIFICATION.CENTER);
                break;
            case RIGHT:
                esc.addSelectJustification(JUSTIFICATION.RIGHT);
                break;
            case LEFT:
            default:
                esc.addSelectJustification(JUSTIFICATION.LEFT);
                break;
        }
        esc.addText(text);
        esc.addPrintAndLineFeed();
    }

    public void text(String leftText, String rightText, float textSize, boolean bold) {
        int[] percents = {0, 100};
        String[] texts = {leftText, rightText};
        Paint.Align[] aligns = {Paint.Align.LEFT, Paint.Align.RIGHT};
        textMulti(percents, texts, aligns,textSize,bold);
    }

    public void text(String leftText, String centerText, String rightText, float textSize, boolean bold) {
        int[] percents = {33, 34, 34};
        String[] texts = {leftText, centerText, rightText};
        Paint.Align[] aligns = {Paint.Align.LEFT, Paint.Align.CENTER, Paint.Align.RIGHT};
        textMulti(percents, texts, aligns,textSize,bold);
    }
    public void textMulti(int[] percents, String[] texts, Paint.Align[] aligns, float textSize, boolean bold) {
        esc.addTurnEmphasizedModeOnOrOff(bold?ENABLE.ON:ENABLE.OFF);
        esc.addTurnDoubleStrikeOnOrOff(bold?ENABLE.ON:ENABLE.OFF);
        esc.addSetFontForHRICharacter(FONT.FONTA);
        esc.addSelectJustification(JUSTIFICATION.CENTER);
        esc.addSetCharcterSize(pix2EscZoomWidth(textSize), pix2EscZoomHeight(textSize));

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < texts.length; i++) {
            int length = PAPER_CHARACTER * percents[0];
            String text = texts[i];
            if (text.length()>=length){
                textBuilder.append(text);
            }else{
                int fillLen = length - text.length();
                switch (aligns[i]) {
                    case CENTER:
                        textBuilder.append(StringUtils.fill(""," ",fillLen/2,true))
                                .append(text)
                                .append(StringUtils.fill(""," ",fillLen/2,false));
                        break;
                    case RIGHT:
                        textBuilder.append(text)
                                .append(StringUtils.fill(""," ",fillLen,false));
                        break;
                    case LEFT:
                    default:
                        textBuilder.append(StringUtils.fill(""," ",fillLen,true))
                                .append(text);
                        break;
                }
            }
        }
        esc.addText(textBuilder.toString());
        esc.addPrintAndLineFeed();
    }

    public void image(Bitmap bitmap) {
        esc.addSelectJustification(JUSTIFICATION.CENTER);
        esc.addRastBitImage(bitmap, bitmap.getWidth(), 0);
        esc.addPrintAndLineFeed();
    }

    public void feedPaper(byte height) {
        esc.addPrintAndFeedPaper(height);
    }

    public void barCode(String barcode, Paint.Align align) {
        switch (align) {
            case CENTER:
                esc.addSelectJustification(JUSTIFICATION.CENTER);
                break;
            case RIGHT:
                esc.addSelectJustification(JUSTIFICATION.RIGHT);
                break;
            case LEFT:
            default:
                esc.addSelectJustification(JUSTIFICATION.LEFT);
                break;
        }
        esc.addCODE128(barcode);
    }

    public void qrCode(String qrCode, Paint.Align align) {
        switch (align) {
            case CENTER:
                esc.addSelectJustification(JUSTIFICATION.CENTER);
                break;
            case RIGHT:
                esc.addSelectJustification(JUSTIFICATION.RIGHT);
                break;
            case LEFT:
            default:
                esc.addSelectJustification(JUSTIFICATION.LEFT);
                break;
        }
        esc.addStoreQRCodeData(qrCode);
        esc.addPrintQRCode();
    }

    public void cutPaper(){
        esc.addCutPaper();
    }
    private WIDTH_ZOOM pix2EscZoomWidth(float textSize){
        for (WIDTH_ZOOM value : WIDTH_ZOOM.values()) {
            if (Math.abs(textSize - value.getValue()) <8){
                return value;
            }
        }
        return WIDTH_ZOOM.MUL_2;
    }
    private HEIGHT_ZOOM pix2EscZoomHeight(float textSize){
        for (HEIGHT_ZOOM value : HEIGHT_ZOOM.values()) {
            if (Math.ceil(textSize/8) == value.getValue()){
                return value;
            }
        }
        return HEIGHT_ZOOM.MUL_2;
    }
}
