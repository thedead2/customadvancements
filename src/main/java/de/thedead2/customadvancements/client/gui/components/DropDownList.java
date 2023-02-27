package de.thedead2.customadvancements.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DropDownList<T> extends AbstractWidget {

    private final ImmutableList<T> listEntries;
    private final List<T> searchResults;
    private final List<ListEntryButton> listEntryButtons = Lists.newArrayList();
    private static final int BUTTON_HEIGHT = Minecraft.getInstance().font.lineHeight + 4;
    private static final int SMALL_PADDING = 2;
    private float scrollOffs = 0.0F;
    private final EditBox editBox;


    public DropDownList(Collection<T> list, EditBox editBox, int listHeight) {
        super(editBox.x, editBox.y + editBox.getHeight() + SMALL_PADDING, editBox.getWidth(), listHeight, Component.empty());
        this.editBox = editBox;
        this.listEntries = ImmutableList.copyOf(list);
        this.searchResults = new ArrayList<>(this.listEntries);
        this.init();
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(this.editBox.isFocused()){
            this.active = true;
            this.renderListContainer(poseStack, mouseX, mouseY, partialTick);
        }
        else {
            this.active = false;
        }
    }


    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (ListEntryButton entryButton: this.listEntryButtons) {
            if(entryButton.isMouseOver(pMouseX, pMouseY) && this.isActive()){
                entryButton.onClick(pMouseX, pMouseY);
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void init(){
        int numberOfVisibleListEntries = this.height /BUTTON_HEIGHT;
        this.height = numberOfVisibleListEntries * BUTTON_HEIGHT;

        for(int i = 0; i < numberOfVisibleListEntries; i++){
            if(this.listEntries.size() > i){
                this.addButton(this.listEntries.get(i), i);
            }
            else {
                break;
            }
        }

        this.height = this.listEntryButtons.size() * BUTTON_HEIGHT;
        this.scrollTo(0.0F);

        this.editBox.setResponder(s -> {
            this.searchForMatching(s);
            this.scrollTo(this.scrollOffs);
        });
        this.editBox.setMaxLength(Integer.MAX_VALUE);
    }


    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if(this.isActive()){
            float f = (float) pDelta;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.scrollTo(this.scrollOffs);
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    private void renderListContainer(PoseStack poseStack, int mouseX, int mouseY, float partialTick){
        //Renders frame with specified height and the contents of the list
        this.renderListContents(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderListContents(PoseStack poseStack, int mouseX, int mouseY, float partialTick){
        for(ListEntryButton button : this.listEntryButtons) {
            button.render(poseStack, mouseX, mouseY, partialTick);
            //this.renderListEntry(button);
        }
    }

    private void renderListEntry(ListEntryButton entryButton){
        //renders frame with Button content
        //entryButton.renderButton();
    }

    private void addButton(T t, int i){
        int yPos = this.y + (BUTTON_HEIGHT + SMALL_PADDING) * i;
        int padding = 2;
        ListEntryButton listEntryButton = new ListEntryButton(this.x + padding, yPos, this.width - padding, Component.literal(t.toString()), this, (button) -> {
            this.editBox.setValue(button.getMessage().getString());
        });
        this.listEntryButtons.add(listEntryButton);
    }

    public void scrollTo(float index){ //index between 0 and 1 (0 % - 100 %)
        int i = this.searchResults.size();
        int j = (int)((double)(index * (float)i));
        if (j < 0) {
            j = 0;
        }

        for(ListEntryButton button : this.listEntryButtons){
            if (j >= 0 && j < this.searchResults.size()) {
                button.setMessage(Component.literal(this.searchResults.get(j).toString()));
                button.active = true;
                button.visible = true;
            }
            else {
                button.setMessage(Component.empty());
                button.active = false;
                button.visible = false;
            }
            j++;
        }
    }

    private void searchForMatching(String in){
        this.searchResults.clear();
        this.listEntries.forEach(entry -> {
            String temp = entry.toString();

            if(temp.contains(in) && !this.searchResults.contains(entry)){
                this.searchResults.add(entry);
            }
        });
    }

    public float getScrollOffs() {
        return this.scrollOffs;
    }

    public EditBox getEditBox() {
        return this.editBox;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    private static class ListEntryButton extends net.minecraft.client.gui.components.Button {

        private final DropDownList<?> list;
        public ListEntryButton(int pX, int pY, int pWidth, Component pMessage, DropDownList<?> list, OnPress pOnPress) {
            super(pX, pY, pWidth, BUTTON_HEIGHT, pMessage, pOnPress);
            this.list = list;
        }

        @Override
        public boolean isMouseOver(double pMouseX, double pMouseY) {
            int leftXCorner = this.x;
            int rightXCorner = leftXCorner + this.width;
            int topYCorner = this.y;
            int bottomYCorner = topYCorner + this.height;

            return pMouseX >= leftXCorner && pMouseX <= rightXCorner && pMouseY >= topYCorner && pMouseY <= bottomYCorner;
        }

        @Override
        public void onClick(double pMouseX, double pMouseY) {
            if(this.isMouseOver(pMouseX, pMouseY) && this.active && this.list.isActive()){
                this.onPress();
            }
        }
    }
}
