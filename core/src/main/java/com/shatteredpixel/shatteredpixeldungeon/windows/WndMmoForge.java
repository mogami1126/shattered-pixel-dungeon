package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.mmo.MmoForge;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.ui.Component;
import com.watabou.noosa.Game;
import com.watabou.utils.PointF;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.RectF;
import java.math.BigInteger;
import java.util.*;

/** Click chooses a quantity; paint mode selects whole remaining stacks. */
public class WndMmoForge extends Window {
    private final Hero hero;
    private final Item receiver;
    private final LinkedHashMap<Item,Integer> selected = new LinkedHashMap<>();
    private final ArrayList<Slot> slots = new ArrayList<>();
    private RenderedTextBlock preview;
    private RedButton confirm, mode;
    private boolean paint;
    private boolean committed;
    private static final int CELL=24;

    private String text(String key,Object... args) { return Messages.get(WndMmoForge.class,key,args); }
    public WndMmoForge(Hero hero,Item receiver) {
        this.hero=hero; this.receiver=receiver;
        RectF insets=Game.platform.getSafeInsets(PlatformSupport.INSET_ALL);
        int safeWidth=(int)((Game.width-insets.left-insets.right)/PixelScene.defaultZoom);
        int safeHeight=(int)((Game.height-insets.top-insets.bottom)/PixelScene.defaultZoom);
        int columns=Math.max(1,Math.min(7,(safeWidth-chrome.marginHor()-4)/CELL));
        int gridWidth=columns*CELL;
        RenderedTextBlock title=PixelScene.renderTextBlock(text("title",receiver.title()),8);
        title.maxWidth(gridWidth);add(title);
        preview=PixelScene.renderTextBlock(6);preview.setPos(0,title.bottom()+3);add(preview);
        preview.maxWidth(gridWidth);
        // Reserve fixed preview height, so selection never moves the grid.
        float gridTop=title.bottom()+30;
        Component content=new Component();
        for(Item item:hero.belongings) if(MmoForge.material(receiver,item,hero)) {
            Slot slot=new Slot(item);int i=slots.size();slots.add(slot);content.add(slot);
            slot.setRect((i%columns)*CELL,(i/columns)*CELL,CELL-1,CELL-1);
        }
        content.setSize(gridWidth,Math.max(CELL,((slots.size()+columns-1)/columns)*CELL));
        ScrollPane pane=new ScrollPane(content) {
            protected void createChildren() {
                super.createChildren();remove(controller);controller.destroy();
                controller=new PointerController() {
                    private boolean painted;
                    private PointF previous;
                    protected void onDrag(PointerEvent event) {
                        if(!paint){super.onDrag(event);return;}
                        if(!painted && PointF.distance(event.current,event.start)<PixelScene.defaultZoom*4) return;
                        PointF start=previous==null?event.start:previous;
                        int steps=Math.max(1,(int)(PointF.distance(start,event.current)/(4*PixelScene.defaultZoom)));
                        for(int i=0;i<=steps;i++) {
                            float t=i/(float)steps;
                            PointF p=content.camera.screenToCamera((int)(start.x+(event.current.x-start.x)*t),
                                    (int)(start.y+(event.current.y-start.y)*t));
                            for(Slot slot:slots)if(slot.inside(p.x,p.y)) choose(slot.source,slot.source.quantity());
                        }
                        previous=new PointF(event.current);painted=true;
                    }
                    protected void onPointerUp(PointerEvent event) {
                        if(painted){painted=false;previous=null;curEvent=null;}
                        else super.onPointerUp(event);
                    }
                };add(controller);
            }
            public void onClick(float x,float y) {
                for(Slot slot:slots)if(slot.inside(x,y)){quantityMenu(slot.source);break;}
            }
        };
        add(pane);
        float gridHeight=Math.max(CELL,Math.min(content.height(),safeHeight-chrome.marginVer()-gridTop-45));
        float bottom=gridTop+gridHeight+3;
        mode=new RedButton(text("scroll_mode")) {
            protected void onClick(){paint=!paint;text(WndMmoForge.this.text(paint?"paint_mode":"scroll_mode"));}
        };mode.setRect(0,bottom,gridWidth,18);add(mode);
        RedButton clear=new RedButton(text("clear")) {
            protected void onClick(){selected.clear();refresh();}
        };clear.setRect(0,bottom+20,(gridWidth-2)/2f,18);add(clear);
        confirm=new RedButton(text("confirm")) {
            protected void onClick(){confirmSelection();}
        };confirm.setRect((gridWidth+2)/2f,bottom+20,(gridWidth-2)/2f,18);add(confirm);
        resize(gridWidth,(int)bottom+38);
        camera.x=(int)(insets.left+(Game.width-insets.left-insets.right-camera.screenWidth())/2);
        camera.y=(int)(insets.top+(Game.height-insets.top-insets.bottom-camera.screenHeight())/2);
        shadow.boxRect(camera.x/camera.zoom,camera.y/camera.zoom,chrome.width(),chrome.height());
        // Resizing moves the window camera; place the content camera afterwards.
        pane.setRect(0,gridTop,gridWidth,gridHeight);refresh();
    }
    private class Slot extends InventorySlot {
        final Item source;
        Slot(Item source){super(source);this.source=source;}
        protected void onClick(){quantityMenu(source);}
        void highlight(){if(selected.containsKey(source))sprite.hardlight(0x55ff88);else sprite.resetColor();}
    }
    private void choose(Item item,int count) {
        if(count<=0)selected.remove(item);
        else selected.put(item,Math.min(count,item.quantity()));
        refresh();
    }
    private void quantityMenu(Item source) {
        if(!MmoForge.material(receiver,source,hero))return;
        GameScene.show(new WndOptions(source.title(),text("quantity_desc",selected.getOrDefault(source,0),source.quantity()),
                text("one"),text("half"),text("all"),text("custom"),text("remove")) {
            protected void onSelect(int index) {
                int remaining=source.quantity()-selected.getOrDefault(source,0);
                if(index==4){choose(source,0);return;}
                if(index==3) {
                    GameScene.show(new WndTextInput(text("quantity_title"),text("quantity_input",remaining),"1",10,false,text("add"),text("cancel")) {
                        public void onSelect(boolean positive,String value) {
                            if(!positive)return;
                            try {
                                int count=Integer.parseInt(value.trim());
                                if(count<=0 || count>remaining)throw new NumberFormatException();
                                choose(source,selected.getOrDefault(source,0)+count);
                            } catch(NumberFormatException bad){GLog.w(text("invalid_quantity"));}
                        }
                    });return;
                }
                int count=index==0?Math.min(1,remaining):index==1?Math.min(remaining,Math.max(1,remaining/2)):remaining;
                choose(source,selected.getOrDefault(source,0)+count);
            }
        });
    }
    private void refresh() {
        BigInteger levels=MmoForge.levels(receiver,selected);
        long count=0;for(int n:selected.values())count+=n;
        preview.text(text("preview",count,levels,receiver.mmoLevel().add(levels)),width);
        for(Slot slot:slots)slot.highlight();
        if(confirm!=null)confirm.enable(!selected.isEmpty());
    }
    private void confirmSelection() {
        BigInteger result=MmoForge.levels(receiver,selected);
        GameScene.show(new WndOptions(text("confirm_title"),text("confirm_desc",selected.size(),result),text("confirm"),text("cancel")) {
            protected void onSelect(int index) {
                if(index!=0 || committed)return;
                if(MmoForge.apply(hero,receiver,selected)) {
                    committed=true;WndMmoForge.this.hide();
                    GLog.p(text("done",result));hero.spendAndNext(1f);
                } else GLog.w(text("changed"));
            }
        });
    }
    public void offset(int x,int y) {
        // This full inventory modal is always centered, independently of its item popup.
        super.offset(0,0);
        // Camera positions must follow a window moved into the safe screen bounds.
        for(com.watabou.noosa.Gizmo child:members)if(child instanceof ScrollPane) {
            ScrollPane p=(ScrollPane)child;p.setRect(p.left(),p.top(),p.width(),p.height());
        }
    }
}
