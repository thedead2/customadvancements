package de.thedead2.customadvancements.client.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.thedead2.customadvancements.client.Area;
import de.thedead2.customadvancements.client.RenderUtil;
import de.thedead2.customadvancements.util.core.ModHelper;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;


public abstract class AbstractTextField extends ScrollableScreenComponent {

    private static final int MAX_STATES = 16;

    protected final Deque<ValueState> undoStates = new ArrayDeque<>();

    protected final Deque<ValueState> redoStates = new ArrayDeque<>();

    protected final Cursor cursor;

    final Font font;

    private final int borderColor;

    protected String value;

    protected Consumer<String> valueListener = s -> {};

    @Nullable
    protected Component suggestion; //Color: 122, 122, 122, 255 * 0.75f

    private boolean selecting;

    private int characterLimit = Integer.MAX_VALUE;

    private boolean editable = false;

    protected BooleanSupplier shouldRenderCursor = this::isEditable;

    protected Runnable cursorListener = () -> {
        if (this.shouldRenderCursor.getAsBoolean() && this.isFocused()) {
            this.scrollToCursor();
        }
    };

    private long lastClicked = 0;


    public AbstractTextField(Area area, Font font, ScrollDirection scrollDirection, ScrollBar.Visibility scrollbarVisibility) {
        this(area, font, scrollDirection, scrollbarVisibility, 0);
    }


    public AbstractTextField(Area area, Font font, ScrollDirection scrollDirection, ScrollBar.Visibility scrollbarVisibility, int borderColor) {
        super(area, scrollDirection, scrollbarVisibility);
        this.cursor = new Cursor(area.getInnerX(), area.getInnerY(), this.contentArea.getZ(), 0);
        this.borderColor = borderColor;
        this.font = font;
    }


    //FIXME: Take to account that for non english layout keys, GLFW_KEY_Z is actually KEY_Y!!
    public static boolean isUndo(int keyCode) {
        return keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_Z && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }


    public static boolean isRedo(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_Z && Screen.hasControlDown() && Screen.hasShiftDown() && !Screen.hasAltDown();
    }


    public void setSuggestion(@Nullable Component suggestion) {
        this.suggestion = suggestion;
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        else if (this.withinContentArea(mouseX, mouseY) && button == 0) {
            if (System.currentTimeMillis() - this.lastClicked < 500) {
                this.setSelecting(true);
                this.seekCursorToPoint(mouseX, mouseY);

                StringSelection selection = this.getCursorLine();

                if (this.cursor.getCharPos() != selection.endIndex) {
                    selection = this.getCurrentWord();
                }

                this.cursor.setSelectPos(selection.beginIndex);
                this.setCursorPos(selection.endIndex);
            }
            else {
                this.setSelecting(Screen.hasShiftDown());
                this.seekCursorToPoint(mouseX, mouseY);
            }

            this.lastClicked = System.currentTimeMillis();

            return true;
        }
        else {
            return false;
        }
    }


    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        else if (this.withinContentArea(mouseX, mouseY) && button == 0) {
            this.setSelecting(true);
            this.seekCursorToPoint(mouseX, mouseY);
            this.setSelecting(Screen.hasShiftDown());

            return true;
        }
        else {
            return false;
        }
    }


    @Override
    protected void renderDecorations(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.hasCharacterLimit()) {
            int i = this.characterLimit();
            Component component = Component.translatable("gui.multiLineEditBox.character_limit", this.value.length(), i);

            guiGraphics.drawString(this.font, component, (int) this.area.getXMax() - (font.width(component) + 2), (int) this.area.getYMax() - (font.lineHeight + 2), -857677600);
        }

        if (this.borderColor != 0) {
            RenderUtil.renderArea(guiGraphics.pose(), this.area, RenderUtil.changeAlpha(borderColor, this.alpha), 0);
        }
    }


    public boolean hasCharacterLimit() {
        return this.characterLimit != Integer.MAX_VALUE;
    }


    public int characterLimit() {
        return this.characterLimit;
    }


    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }


    public abstract void seekCursorToPoint(double x, double y);


    protected abstract StringSelection getCursorLine();


    protected StringSelection getCurrentWord() {
        if (this.value.isEmpty()) {
            return new StringSelection(0, 0);
        }
        else {
            int begin;

            for (begin = Mth.clamp(this.cursor.getCharPos(), 0, this.value.length() - 1); begin > 0; --begin) {
                if (Character.isWhitespace(this.value.charAt(begin - 1))) {
                    break;
                }
            }

            int end;

            for (end = Mth.clamp(this.cursor.getCharPos(), 0, this.value.length() - 1); end < this.value.length(); ++end) {
                if (Character.isWhitespace(this.value.charAt(end))) {
                    break;
                }
            }

            return new StringSelection(begin, end);
        }
    }


    protected void setCursorPos(int charPos) {
        this.cursor.setCharPos(Mth.clamp(charPos, 0, this.value.length()));
        this.updateCursorDisplayPos();
        this.cursorListener.run();
    }


    protected abstract void updateCursorDisplayPos();


    public void setCharacterLimit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Character limit cannot be negative!");
        }
        else {
            this.characterLimit = limit;
        }
    }


    public void setValueListener(Consumer<String> listener) {
        this.valueListener = listener;
    }


    public void setCursorListener(Runnable listener) {
        this.cursorListener = listener;
    }


    private void addRedoState(int beginIndex, int endIndex, String value) {
        this.addState(this.redoStates, beginIndex, endIndex, value);
    }


    public void deleteText(int len) {
        if (!this.hasSelection()) {
            this.cursor.setSelectPos(Mth.clamp(this.cursor.getCharPos() + len, 0, this.value.length()));
        }

        this.insertText("");
    }


    public Cursor cursor() {
        return this.cursor;
    }


    public void changeCursorPosition(Whence whence, int amount) {
        switch (whence) {
            case ABSOLUTE:
                this.setCursorPos(amount);
                break;
            case RELATIVE:
                this.moveCursorPos(amount);
                break;
            case END:
                this.setCursorPos(this.value.length());
        }

        if (!this.selecting) {
            this.cursor.setSelectToCurrentPos();
        }
    }


    protected void moveCursorPos(int amount) {
        this.cursor.moveCharPos(amount);
        this.setCursorPos(Mth.clamp(this.cursor.getCharPos(), 0, this.value.length()));
    }


    protected abstract void scrollToCursor();


    protected void renderHighlight(PoseStack poseStack, float startX, float startY, float endX, float endY) {
        Matrix4f matrix4f = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor((float) 0, (float) 102 / 255, 1, 0.5f);
        RenderSystem.enableBlend();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR);

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(matrix4f, startX, endY, this.contentArea.getZ()).endVertex();
        bufferbuilder.vertex(matrix4f, endX, endY, this.contentArea.getZ()).endVertex();
        bufferbuilder.vertex(matrix4f, endX, startY, this.contentArea.getZ()).endVertex();
        bufferbuilder.vertex(matrix4f, startX, startY, this.contentArea.getZ()).endVertex();
        tesselator.end();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableColorLogicOp();
        RenderSystem.disableBlend();
    }


    protected void renderCursor(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.shouldRenderCursor.getAsBoolean() && this.isFocused() && this.withinContentAreaTopBottom(this.cursor.getYPos() - 2, this.cursor.getYPos() + this.font.lineHeight + 2)) {
            this.updateCursorDisplayPos();
            this.cursor.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }


    public String getSelectedText() {
        StringSelection selected = this.getSelected();

        return selected.getContent();
    }


    protected StringSelection getPreviousWord() {
        if (this.value.isEmpty()) {
            return new StringSelection(0, 0);
        }
        else {
            int i;

            for (i = Mth.clamp(this.cursor.getCharPos(), 0, this.value.length() - 1); i > 0; --i) {
                if (!Character.isWhitespace(this.value.charAt(i - 1))) {
                    break;
                }
            }

            while (i > 0 && !Character.isWhitespace(this.value.charAt(i - 1))) {
                --i;
            }

            return new StringSelection(i, this.getWordEndPosition(i));
        }
    }


    protected StringSelection getNextWord() {
        if (this.value.isEmpty()) {
            return new StringSelection(0, 0);
        }
        else {
            int i;

            for (i = Mth.clamp(this.cursor.getCharPos(), 0, this.value.length() - 1); i < this.value.length(); ++i) {
                if (Character.isWhitespace(this.value.charAt(i))) {
                    break;
                }
            }

            while (i < this.value.length() && Character.isWhitespace(this.value.charAt(i))) {
                ++i;
            }

            return new StringSelection(i, this.getWordEndPosition(i));
        }
    }


    private int getWordEndPosition(int cursorPos) {
        int i;

        for (i = cursorPos; i < this.value.length(); ++i) {
            if (Character.isWhitespace(this.value.charAt(i))) {
                break;
            }
        }

        return i;
    }


    //FIXME: Sometimes crashes due to beginIndex > endIndex when deleting a value
    //FIXME: When doing multiple times in a row, only works occasionally
    //FIXME: Sometimes adds/ deletes wrong text
    private boolean handleUndoOrRedoRequest(Deque<ValueState> states, boolean updateUndoStates, StatesUpdater updater) {
        ValueState state = states.poll();

        if (state == null) {
            return false;
        }

        this.cursor.setSelectPos(state.beginIndex);
        this.setCursorPos(state.endIndex);

        updater.update(state.beginIndex, state.endIndex, this.getSelectedText());

        this.insertText(state.value, updateUndoStates);

        return true;
    }


    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.selecting = Screen.hasShiftDown();

        if (Screen.isSelectAll(keyCode)) {
            this.setCursorPos(this.value.length());
            this.cursor.setSelectPos(0);

            return true;
        }
        else if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());

            return true;
        }
        else if (this.editable) {
            if (Screen.isPaste(keyCode)) {
                this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());

                return true;
            }
            else if (Screen.isCut(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
                this.insertText("");

                return true;
            }
            //TODO: Remove log
            else if (isUndo(keyCode)) {
                ModHelper.LOGGER.debug("Undo requested!");

                return this.handleUndoOrRedoRequest(this.undoStates, false, this::addRedoState);
            }
            else if (isRedo(keyCode)) {
                ModHelper.LOGGER.debug("Redo requested!");

                return this.handleUndoOrRedoRequest(this.redoStates, true, this::addUndoState);
            }
            else {
                return switch (keyCode) {
                    case GLFW_KEY_ENTER, GLFW_KEY_KP_ENTER -> {
                        this.onEnter();

                        yield true;
                    }
                    case GLFW_KEY_TAB -> {
                        this.onTab();

                        yield true;
                    }
                    case GLFW_KEY_BACKSPACE -> {
                        if (Screen.hasControlDown()) {
                            StringSelection previousWord = this.getPreviousWord();

                            this.deleteText(previousWord.beginIndex - this.cursor.getCharPos());
                        }
                        else {
                            this.deleteText(-1);
                        }

                        yield true;
                    }
                    case GLFW_KEY_DELETE -> {
                        if (Screen.hasControlDown()) {
                            StringSelection nextWord = this.getNextWord();

                            this.deleteText(nextWord.beginIndex - this.cursor.getCharPos());
                        }
                        else {
                            this.deleteText(1);
                        }

                        yield true;
                    }
                    case GLFW_KEY_RIGHT -> {
                        if (Screen.hasControlDown()) {
                            StringSelection nextWord = this.getNextWord();

                            this.changeCursorPosition(Whence.ABSOLUTE, nextWord.beginIndex);
                        }
                        else {
                            this.changeCursorPosition(Whence.RELATIVE, 1);
                        }

                        yield true;
                    }
                    case GLFW_KEY_LEFT -> {
                        if (Screen.hasControlDown()) {
                            StringSelection previousWord = this.getPreviousWord();

                            this.changeCursorPosition(Whence.ABSOLUTE, previousWord.beginIndex);
                        }
                        else {
                            this.changeCursorPosition(Whence.RELATIVE, -1);
                        }

                        yield true;
                    }
                    case GLFW_KEY_DOWN -> {
                        if (!Screen.hasControlDown()) {
                            this.onDown();
                        }

                        yield true;
                    }
                    case GLFW_KEY_UP -> {
                        if (!Screen.hasControlDown()) {
                            this.onUp();
                        }

                        yield true;
                    }
                    case GLFW_KEY_PAGE_UP -> {
                        this.changeCursorPosition(Whence.ABSOLUTE, 0);

                        yield true;
                    }
                    case GLFW_KEY_PAGE_DOWN -> {
                        this.changeCursorPosition(Whence.END, 0);

                        yield true;
                    }
                    case GLFW_KEY_HOME -> {
                        if (Screen.hasControlDown()) {
                            this.changeCursorPosition(Whence.ABSOLUTE, 0);
                        }
                        else {
                            this.changeCursorPosition(Whence.ABSOLUTE, this.getCursorLine().beginIndex);
                        }

                        yield true;
                    }
                    case GLFW_KEY_END -> {
                        if (Screen.hasControlDown()) {
                            this.changeCursorPosition(Whence.END, 0);
                        }
                        else {
                            this.changeCursorPosition(Whence.ABSOLUTE, this.getCursorLine().endIndex);
                        }

                        yield true;
                    }
                    default -> false;
                };
            }
        }
        else {
            return false;
        }
    }


    public boolean charTyped(char codePoint, int modifiers) {
        if (this.editable && SharedConstants.isAllowedChatCharacter(codePoint)) {
            this.insertText(Character.toString(codePoint));

            return true;
        }
        else {
            return false;
        }
    }


    public void insertText(String text) {
        this.insertText(text, true);
    }


    private void insertText(String text, boolean updateUndoStates) {
        if (!text.isEmpty() || this.hasSelection()) {
            String s = this.trimInsertionText(SharedConstants.filterText(text, true));
            StringSelection selected = this.getSelected();

            if (updateUndoStates) {
                if (text.isEmpty() || this.hasSelection()) {
                    this.addUndoState(selected.beginIndex, selected.endIndex, selected.getContent());
                }
                else {
                    this.addUndoState(selected.beginIndex, selected.beginIndex + s.length(), "");
                }
            }

            if (text.isEmpty()) {
                this.setCursorPos(selected.beginIndex);

                this.value = (new StringBuilder(this.value)).replace(selected.beginIndex, selected.endIndex, s).toString();

                this.onValueChange();
                //Update the position again as the line positions might have changed!
                this.updateCursorDisplayPos();
            }
            else {
                if (this.hasSelection()) {
                    this.value = (new StringBuilder(this.value)).replace(selected.beginIndex, selected.endIndex, s).toString();
                }
                else {
                    this.value = (new StringBuilder(this.value)).insert(selected.beginIndex, s).toString();
                }

                this.onValueChange();
                this.setCursorPos(selected.beginIndex + s.length());
            }

            this.cursor.setSelectToCurrentPos();
        }
    }


    public boolean hasSelection() {
        return this.cursor.hasSelection();
    }


    private String trimInsertionText(String text) {
        if (this.hasCharacterLimit()) {
            int i = this.characterLimit - this.value.length();
            text = StringUtil.truncateStringIfNecessary(text, i, false);
        }

        return text;
    }


    protected StringSelection getSelected() {
        return new StringSelection(Math.min(this.cursor.getSelectPos(), this.cursor.getCharPos()), Math.max(this.cursor.getSelectPos(), this.cursor.getCharPos()));
    }


    private void addUndoState(int beginIndex, int endIndex, String value) {
        this.addState(this.undoStates, beginIndex, endIndex, value);
    }


    protected void onValueChange() {
        this.valueListener.accept(this.value);
    }


    private void addState(Deque<ValueState> states, int beginIndex, int endIndex, String value) {
        this.addState(states, new ValueState(beginIndex, endIndex, value));
    }


    private void addState(Deque<ValueState> states, ValueState value) {
        if (states.offerFirst(value) && states.size() > MAX_STATES) {
            states.pollLast();
        }
    }


    protected void onTab() {}


    protected void onEnter() {}


    protected void onUp() {}


    protected void onDown() {}


    @Override
    public void updateNarration(@Nonnull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.editBox", "", this.value()));
    }


    public String value() {
        return this.value;
    }


    public AbstractTextField setEditable() {
        this.editable = true;

        return this;
    }


    public AbstractTextField setUnEditable() {
        this.editable = false;

        return this;
    }


    public void clear() {
        this.setValue("");
    }


    public void setValue(String text) {
        this.value = this.trimTextIfNecessary(text);

        this.onValueChange();
        this.setCursorPos(this.value.length());
        this.cursor.setSelectToCurrentPos();
    }


    private String trimTextIfNecessary(String text) {
        if (this.hasCharacterLimit()) {
            text = StringUtil.truncateStringIfNecessary(text, this.characterLimit, false);
        }

        return text;
    }


    public void setRenderCursor(BooleanSupplier renderCursor) {
        this.shouldRenderCursor = renderCursor;
    }


    public void resetRenderCursor() {
        this.shouldRenderCursor = this::isEditable;
    }


    public boolean isEditable() {
        return this.editable;
    }


    @FunctionalInterface
    private interface StatesUpdater {

        void update(int beginIndex, int endIndex, String value);
    }


    protected record ValueState(int beginIndex, int endIndex, String value) {}


    @OnlyIn(Dist.CLIENT)
    protected class StringSelection {

        protected final int beginIndex, endIndex;


        StringSelection(int beginIndex, int endIndex) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }


        public String getContent() {
            return AbstractTextField.this.value.substring(Math.max(this.beginIndex, 0), Math.min(this.endIndex + 1, AbstractTextField.this.value.length()));
        }


        public boolean contains(int charPos) {
            return charPos >= this.beginIndex && charPos <= this.endIndex;
        }
    }
}
