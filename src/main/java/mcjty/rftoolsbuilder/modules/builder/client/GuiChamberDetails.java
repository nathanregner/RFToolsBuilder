package mcjty.rftoolsbuilder.modules.builder.client;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.*;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.*;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.network.PacketUpdateCardInPlayer;
import mcjty.rftoolsbuilder.setup.CommandHandler;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

import static mcjty.lib.gui.layout.AbstractLayout.DEFAULT_SPACING;
import static mcjty.lib.gui.widgets.Widgets.*;

public class GuiChamberDetails extends GuiItemScreen implements IKeyReceiver {

    private static final int CHAMBER_XSIZE = 390;
    private static final int CHAMBER_YSIZE = 210;

    private static Map<BlockState, Integer> items = null;
    private static Map<BlockState, Integer> costs = null;
    private static Map<BlockState, ItemStack> stacks = null;
    private static Map<String, Integer> entities = null;
    private static Map<String, Integer> entityCosts = null;
    private static Map<String, CompoundTag> realEntities = null;
    private static Map<String, String> playerNames = null;

    private WidgetList blockList;
    private Label infoLabel;
    private Label info2Label;
    private TextField offsetX;
    private TextField offsetY;
    private TextField offsetZ;

    public GuiChamberDetails() {
        super(CHAMBER_XSIZE, CHAMBER_YSIZE,  /* @todo 1.14 GuiProxy.GUI_MANUAL_SHAPE*/ ManualEntry.EMPTY);
        requestChamberInfoFromServer();
    }

    public static void setItemsWithCount(Map<BlockState, Integer> items, Map<BlockState, Integer> costs,
                                         Map<BlockState, ItemStack> stacks,
                                         Map<String, Integer> entities, Map<String, Integer> entityCosts,
                                         Map<String, CompoundTag> realEntities,
                                         Map<String, String> playerNames) {
        GuiChamberDetails.items = new HashMap<>(items);
        GuiChamberDetails.costs = new HashMap<>(costs);
        GuiChamberDetails.stacks = new HashMap<>(stacks);
        GuiChamberDetails.entities = new HashMap<>(entities);
        GuiChamberDetails.entityCosts = new HashMap<>(entityCosts);
        GuiChamberDetails.realEntities = new HashMap<>(realEntities);
        GuiChamberDetails.playerNames = new HashMap<>(playerNames);
    }

    private void requestChamberInfoFromServer() {
        RFToolsBuilderMessages.sendToServer(CommandHandler.CMD_GET_CHAMBER_INFO);
    }

    @Override
    public void init() {
        super.init();

        ItemStack heldItem = getStackToEdit();
        if (heldItem.isEmpty()) {
            // Cannot happen!
            return;
        }

        blockList = new WidgetList().name("blocks");
        Slider listSlider = new Slider().desiredWidth(10).vertical().scrollableName("blocks");
        Panel listPanel = horizontal(3, 1).children(blockList, listSlider);

        infoLabel = new Label().horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        infoLabel.desiredWidth(380).desiredHeight(14);
        info2Label = new Label().horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        info2Label.desiredWidth(380).desiredHeight(14);

        BlockPos offset = ShapeCardItem.getOffset(heldItem);

        offsetX = new TextField().event((newText) -> updateSettings()).text(String.valueOf(offset.getX()));
        offsetY = new TextField().event((newText) -> updateSettings()).text(String.valueOf(offset.getY()));
        offsetZ = new TextField().event((newText) -> updateSettings()).text(String.valueOf(offset.getZ()));
        Panel offsetPanel = horizontal(0, DEFAULT_SPACING).desiredHeight(18).children(
                label("Offset:").horizontalAlignment(HorizontalAlignment.ALIGN_RIGHT).desiredWidth(40),
                offsetX, offsetY, offsetZ);

        Panel toplevel = vertical(3, 1).filledRectThickness(2).children(offsetPanel, listPanel, infoLabel, info2Label);
        toplevel.bounds(guiLeft, guiTop, xSize, ySize);

        window = new Window(this, toplevel);
    }

    private void populateLists() {
        blockList.removeChildren();
        if (items == null) {
            return;
        }

        int totalCost = 0;
        for (Map.Entry<BlockState, Integer> entry : items.entrySet()) {
            BlockState bm = entry.getKey();
            int count = entry.getValue();
            int cost = costs.get(bm);
            Panel panel = horizontal().desiredHeight(16);
            ItemStack stack;
            if (stacks.containsKey(bm)) {
                stack = stacks.get(bm);
            } else {
                stack = bm.getBlock().getCloneItemStack(Minecraft.getInstance().level, BlockPos.ZERO, bm);
                if (stack.isEmpty()) {
                    stack = new ItemStack(bm.getBlock(), 0);
                }
            }
            BlockRender blockRender = new BlockRender().renderItem(stack).offsetX(-1).offsetY(-1);

            Label nameLabel = new Label().horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).color(StyleConfig.colorTextInListNormal);
            stack.getItem();
            nameLabel.text(stack.getHoverName().getString()).desiredWidth(160);   // @todo getFormattedText

            Label countLabel = label(String.valueOf(count)).color(StyleConfig.colorTextInListNormal);
            countLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).desiredWidth(50);

            Label costLabel = new Label().color(StyleConfig.colorTextInListNormal);
            costLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);

            if (cost == -1) {
                costLabel.text("NOT MOVABLE!");
            } else {
                costLabel.text("Move Cost " + cost + " RF");
                totalCost += cost;
            }
            panel.children(blockRender, nameLabel, countLabel, costLabel);
            blockList.children(panel);
        }

        int totalCostEntities = 0;
        RenderHelper.rot += .5f;
        for (Map.Entry<String, Integer> entry : entities.entrySet()) {
            String id = entry.getKey();
            int count = entry.getValue();
            int cost = entityCosts.get(id);
            Panel panel = horizontal().desiredHeight(16);

            String entityName = "<?>";
            Entity entity = null;
            if (realEntities.containsKey(id)) {
                CompoundTag tag = realEntities.get(id);
                EntityType<?> value = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));

                entity = value.create(minecraft.level);
                entity.load(tag);
                entityName = entity.getDisplayName().getString();
                if (entity instanceof ItemEntity entityItem) {
                    if (!entityItem.getItem().isEmpty()) {
                        String displayName = entityItem.getItem().getDisplayName().getString();
                        entityName += " (" + displayName + ")";
                    }
                }
            } else {
                EntityType<?> value = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
                entity = value.create(minecraft.level);
                entityName = entity.getDisplayName().getString();
            }

            if (playerNames.containsKey(id)) {
                entityName = playerNames.get(id);
            }

            BlockRender blockRender = new BlockRender().renderItem(entity).offsetX(-1).offsetY(-1);

            Label nameLabel = label(entityName).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).desiredWidth(160);
            Label countLabel = label(String.valueOf(count));
            countLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).desiredWidth(50);

            Label costLabel = new Label();
            costLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);

            if (cost == -1) {
                costLabel.text("NOT MOVABLE!");
            } else {
                costLabel.text("Move Cost " + cost + " RF");
                totalCostEntities += cost;
            }
            panel.children(blockRender, nameLabel, countLabel, costLabel);
            blockList.children(panel);
        }


        infoLabel.text("Total cost blocks: " + totalCost + " RF");
        info2Label.text("Total cost entities: " + totalCostEntities + " RF");
    }

    @Override
    protected void renderInternal(GuiGraphics graphics, int pMouseX, int pMouseY, float partialTick) {
         populateLists();
        drawWindow(graphics, pMouseX, pMouseY, partialTick);
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuiChamberDetails());
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private ItemStack getStackToEdit() {
        return getMinecraft().player.getItemInHand(InteractionHand.MAIN_HAND);
    }

    private void updateSettings() {
        ItemStack stack = getStackToEdit();
        if (!stack.isEmpty()) {
            int x = parseInt(offsetX.getText());
            System.out.println(x);
            ShapeCardItem.setOffset(stack, x, parseInt(offsetY.getText()), parseInt(offsetZ.getText()));
            RFToolsBuilderMessages.sendToServer(PacketUpdateCardInPlayer.create(stack));
        }
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public void keyTypedFromEvent(int keyCode, int scanCode) {
        if (window != null) {
            if (window.keyTyped(keyCode, scanCode)) {
                super.keyPressed(keyCode, scanCode, 0); // @todo 1.14: modifiers?
            }
        }
    }

    @Override
    public void charTypedFromEvent(char codePoint) {
        if (window != null) {
            if (window.charTyped(codePoint)) {
                super.charTyped(codePoint, 0); // @todo 1.14: modifiers?
            }
        }
    }

    @Override
    public boolean mouseClickedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseClicked(x, y, button);
        return true;
    }

    @Override
    public boolean mouseReleasedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseReleased(x, y, button);
        return true;
    }

    @Override
    public boolean mouseScrolledFromEvent(double x, double y, double dx, double dy) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseScrolled(x, y, dx, dy);
        return true;
    }

}
