package fluxnetworks.client.gui.basic;

import com.google.common.collect.Lists;
import fluxnetworks.common.tileentity.TileFluxCore;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.List;

public abstract class GuiTabPages<T> extends GuiTabCore {

    public List<T> elements = Lists.newArrayList();
    protected List<T> current = Lists.newArrayList();
    protected SortType sortType = SortType.ID;

    public int page = 1, currentPages = 1, pages = 1, gridPerPage = 1, gridStartX = 0, gridStartY = 0, gridHeight = 0, elementHeight = 0, elementWidth = 0;

    public GuiTabPages(EntityPlayer player, TileFluxCore tileEntity) {
        super(player, tileEntity);
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        int i = 0;
        for(T s : current) {
            renderElement(s, gridStartX, gridStartY + gridHeight * i);
            i++;
        }
        if(pages > 1) {
            fontRenderer.drawString(page + " / " + pages, 76, 156, 0xffffff);
        }
    }

    public <T> T getHoveredElement(int mouseX, int mouseY) {
        for(int i = 0; i < currentPages; i++) {
            int y = (gridStartY + gridHeight * i);
            if(mouseX >= gridStartX && mouseY >= y && mouseX < (gridStartX + elementWidth) && mouseY < y + elementHeight) {
                if(current.get(i) != null) {
                    return (T) current.get(i);
                }
            }
        }
        return null;
    }

    protected abstract void onElementClicked(T element, int mouseButton);

    @Override
    protected void mouseMainClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseMainClicked(mouseX, mouseY, mouseButton);
        T e = getHoveredElement(mouseX - guiLeft, mouseY - guiTop);
        if(e != null) {
            onElementClicked(e, mouseButton);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void mouseScroll(int mouseX, int mouseY, int scroll) throws IOException {
        super.mouseScroll(mouseX, mouseY, scroll);

        if(scroll == -1 && page < pages) {
            page++;
            refreshCurrentPage();
        } else if (scroll == 1 && page > 1) {
            page--;
            refreshCurrentPage();
        }
    }

    public abstract void renderElement(T element, int x, int y);

    protected void refreshPages(List<T> elements) {
        this.elements = elements;
        pages = (int) Math.ceil(elements.size() / (double) gridPerPage);
        sortGrids(sortType);
        refreshCurrentPage();
    }

    protected void refreshCurrentPage() {
        if(elements.size() == 0)
            return;

        current.clear();
        int a = (page - 1) * gridPerPage;
        int b = Math.min(elements.size(), page * gridPerPage);
        currentPages = b - a;
        /*if(page == pages) {
            for(int i = (page - 1) * gridPerPage; i < elements.size(); i++) {
                current.add(elements.get(i));
            }
        } else {
            for (int i = (page - 1) * gridPerPage; i < page * gridPerPage; i++) {
                current.add(elements.get(i));
            }
        }*/
        for(int i = a; i < b; i++) {
            current.add(elements.get(i));
        }

    }

    protected void sortGrids(SortType sortType) {

    }

    public enum SortType {
        ID("ID"),
        NAME("Name");

        public String name;

        SortType(String name) {
            this.name = name;
        }
    }

}