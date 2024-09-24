package de.thedead2.customadvancements.client.components;

import de.thedead2.customadvancements.client.Alignment;
import de.thedead2.customadvancements.client.Area;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class TextBox extends AbstractTextField {

    private final List<DisplayLine> displayLines;

    private final Alignment.XAlign textAlign;

    private Alignment.YAlign verticalAlignment = Alignment.YAlign.TOP;


    public TextBox(Area area, Font font) {
        this(area, font, "");
    }


    public TextBox(Area area, Font font, String val) {
        this(area, font, Alignment.XAlign.LEFT, val, 0);
    }


    public TextBox(Area area, Font font, Alignment.XAlign textAlign, String val, int borderColor) {
        super(area, font, ScrollDirection.VERTICAL, ScrollBar.Visibility.IF_NECESSARY, borderColor);
        this.displayLines = new ArrayList<>();
        this.textAlign = textAlign;

        this.setValue(val);
    }


    public TextBox(Area area, Font font, String val, int borderColor) {
        this(area, font, Alignment.XAlign.LEFT, val, borderColor);
    }


    public void setVerticalTextAlignment(Alignment.YAlign verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }


    @Override
    public void seekCursorToPoint(double x, double y) {
        int j = this.getLineIndexByYPos(y);
        DisplayLine line = this.displayLines.get(Mth.clamp(j, 0, this.displayLines.size() - 1));
        float widthDif = (float) (x - line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), line.width()));
        int i = this.font.plainSubstrByWidth(line.getContent(), Math.round(widthDif)).length();

        this.changeCursorPosition(Whence.ABSOLUTE, line.beginIndex + i);
    }


    @Override
    protected void onValueChange() {
        this.recalculateDisplayLines();

        super.onValueChange();
    }


    private void recalculateDisplayLines() {
        this.displayLines.clear();

        if (this.value.isEmpty()) {
            this.displayLines.add(new DisplayLine(0, 0, Alignment.XAlign.LEFT));
        }
        else {
            this.splitLines(this.value, this.font, this.contentArea.getInnerWidth(), (i, j) -> this.displayLines.add(new DisplayLine(i, j, this.textAlign)));
        }
    }


    private void splitLines(String value, Font font, float maxWidth, LinePosConsumer consumer) {
        if (maxWidth <= 0) {
            throw new IllegalArgumentException("Max width must be positive and greater than zero!");
        }

        int j = 0;
        int i;
        float width = 0;
        int lastSpacePos = -1;

        char[] chars = value.toCharArray();

        for (i = j; i < chars.length; i++) {
            char c = chars[i];

            if (Character.isWhitespace(c)) {
                lastSpacePos = i;
            }

            width += font.width(String.valueOf(c));

            if (width >= maxWidth || c == '\n') {
                int splitPos = ((lastSpacePos != -1) ? lastSpacePos : i - 1);

                consumer.accept(j, splitPos);

                width = 0;
                //Keep the width of the string that has been split
                for (int k = splitPos; k < i; k++) {
                    width += font.width(String.valueOf(chars[k]));
                }

                //Continue at the next char
                j = splitPos + 1;
                lastSpacePos = -1;
            }
        }

        consumer.accept(j, i);
    }


    @Override
    protected void updateCursorDisplayPos() {
        DisplayLine line = this.getCursorLine();

        this.cursor.setDisplayPos(line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), line.width()) + this.width(this.value.substring(line.beginIndex, this.cursor.getCharPos())) - (this.cursor.getLineWidth() / 2), this.getYPosByLineIndex(this.displayLines.indexOf(line)), this.contentArea.getZ());
    }


    private DisplayLine getLineWithOffsetFromCursorLine(int offset) {
        int i = this.getCursorLineIndex(); //returns -1 if an \n is the last char

        if (i < 0) {
            throw new IllegalStateException("Cursor is not within text (cursor = " + this.cursor.getCharPos() + ", length = " + this.value.length() + ")");
        }
        else {
            return this.displayLines.get(Mth.clamp(i + offset, 0, this.displayLines.size() - 1));
        }
    }


    public int getCursorLineIndex() {
        return this.getLineIndexByCharPos(this.cursor.getCharPos());
    }


    @Override
    protected void scrollToCursor() { //FIXME: When text gets deleted
        double amount = this.yScrollBar.getScrollAmount();
        int i = this.getCursorLineIndex();
        float contentHeight = this.font.lineHeight;
        float yPos = this.getYPosByLineIndex(i);

        if (yPos + contentHeight - amount >= this.contentArea.getInnerYMax()) {
            amount = yPos + contentHeight - this.contentArea.getInnerYMax();
        }
        else if (yPos <= this.contentArea.getInnerY() + amount) {
            amount = yPos - this.contentArea.getInnerY();
        }

        this.yScrollBar.setScrollAmount(amount);
    }


    @Override
    protected void onTab() {
        this.insertText("    ");
    }


    @Override
    protected void onEnter() {
        this.insertText("\n");
    }


    @Override
    protected DisplayLine getCursorLine() {
        return this.getLineWithOffsetFromCursorLine(0);
    }


    @Override
    protected void onUp() {
        this.changeCursorLinePos(-1);
    }


    @Override
    protected void onDown() {
        this.changeCursorLinePos(1);
    }


    protected int getLineIndexByYPos(double y) {
        float yStart = (float) (this.verticalAlignment.getYPos(this.contentArea.getInnerY(), this.contentArea.getInnerHeight(), Mth.clamp(this.contentHeight(), 1, this.contentArea.getInnerHeight())) - this.yScrollBar.getScrollAmount());

        if (y < yStart) {
            return 0;
        }

        for (int i = 0; i < this.displayLines.size(); i++) {
            float lineHeight = this.font.lineHeight;

            if (y >= yStart && y < yStart + lineHeight) {
                return i;
            }

            yStart += lineHeight;
        }

        return this.displayLines.size();
    }


    @Override
    protected double scrollRate() {
        return 9.0D / 2.0D;
    }


    @Override
    public void renderContents(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.value.isEmpty() && this.suggestion != null) {
            guiGraphics.drawWordWrap(this.font, this.suggestion, (int) this.contentArea.getInnerX(), (int) this.verticalAlignment.getYPos(this.contentArea.getInnerY(), this.contentArea.getInnerHeight(), Mth.clamp(this.contentHeight(), 1, this.contentArea.getInnerHeight())), (int) this.contentArea.getInnerWidth(), new Color(122, 122, 122, (int) 255 * 0.75f).getRGB());
        }
        else {
            for (int i = 0; i < this.displayLines.size(); i++) {
                float yPos = this.getYPosByLineIndex(i);
                DisplayLine line = this.displayLines.get(i);
                String lineContent = line.getContent();

                if (!this.withinContentAreaTopBottom(yPos, yPos + this.font.lineHeight)) {
                    continue;
                }

                float xPos = line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), line.width());

                guiGraphics.drawString(this.font, lineContent.replace('\n', ' '), (int) xPos, (int) yPos, Color.WHITE.getRGB()); //Don't render \n !!!
            }
        }

        if (this.hasSelection() && this.isEditable()) {
            StringSelection selection = this.getSelected();

            for (int i = this.getLineIndexByCharPos(selection.beginIndex); i < Mth.clamp(this.getLineIndexByCharPos(selection.endIndex) + 1, 0, this.displayLines.size()); i++) {
                DisplayLine line = this.displayLines.get(i);
                float yStart = this.getYPosByLineIndex(i) - 1;
                float yStop = yStart + this.font.lineHeight + 2;

                if (!this.withinContentAreaTopBottom(yStart, yStop)) {
                    continue;
                }

                float lineWidth = line.width();
                float xStart = line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), lineWidth) - 1;

                if (line.contains(selection.beginIndex)) {
                    xStart += this.width(this.value.substring(line.beginIndex, selection.beginIndex));
                }

                float xStop = line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), lineWidth) + lineWidth;

                if (line.contains(selection.endIndex)) {
                    xStop -= this.width(this.value.substring(selection.endIndex, line.endIndex));
                }

                this.renderHighlight(guiGraphics.pose(), xStart, yStart, xStop, yStop);
            }
        }

        this.renderCursor(guiGraphics, mouseX, mouseY, partialTick);
    }


    protected float contentHeight() {
        float height = 0;

        for (DisplayLine line : this.displayLines) {
            String lineContent = line.getContent();
            height += lineContent.isEmpty() ? (this.font.lineHeight + 3) : this.font.lineHeight;
        }

        return height;
    }


    protected float getYPosByLineIndex(int index) {
        float yStart = this.verticalAlignment.getYPos(this.contentArea.getInnerY(), this.contentArea.getInnerHeight(), Mth.clamp(this.contentHeight(), 1, this.contentArea.getInnerHeight()));

        for (int i = 0; i < Mth.clamp(index, 0, this.displayLines.size()); i++) {
            float lineHeight = this.font.lineHeight;
            yStart += lineHeight;
        }

        return yStart;
    }


    //FIXME: Doesn't work properly
    protected int getLineIndexByCharPos(int charPos) {
        for (int i = 0; i < this.displayLines.size(); ++i) {
            DisplayLine line = this.displayLines.get(i);

            if (line.contains(charPos)) {
                return i;
            }
        }

        return -1;
    }


    private int width(String in) {
        return this.font.width(in);
    }


    public int getLineCount() {
        return this.displayLines.size();
    }


    public int getSelectionLineIndex() {
        return this.getLineIndexByCharPos(this.cursor.getSelectPos());
    }


    protected DisplayLine getLine(int index) {
        return this.displayLines.get(Mth.clamp(index, 0, this.displayLines.size() - 1));
    }


    public void changeCursorLinePos(int offset) {
        if (offset != 0) {
            DisplayLine lineAtOffset = this.getLineWithOffsetFromCursorLine(offset);
            DisplayLine currentCursorLine = this.getCursorLine();
            int dif = this.value.substring(currentCursorLine.beginIndex, this.cursor.getCharPos()).length();

            this.changeCursorPosition(Whence.ABSOLUTE, lineAtOffset.beginIndex + Math.min(dif, lineAtOffset.endIndex - lineAtOffset.beginIndex));
        }
    }


    protected Iterable<DisplayLine> iterateLines() {
        return this.displayLines;
    }


    @FunctionalInterface
    public interface LinePosConsumer {

        void accept(int beginIndex, int endIndex);
    }


    protected class DisplayLine extends StringSelection {

        private final Alignment.XAlign textAlignment;


        DisplayLine(int beginIndex, int endIndex, Alignment.XAlign textAlignment) {
            super(beginIndex, endIndex);
            this.textAlignment = textAlignment;
        }


        int width() {
            return TextBox.this.width(this.getContent());
        }
    }
}
