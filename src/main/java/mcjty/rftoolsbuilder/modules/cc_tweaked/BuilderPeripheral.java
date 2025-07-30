package mcjty.rftoolsbuilder.modules.cc_tweaked;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.AnchorMode;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.RotateMode;
import mcjty.rftoolsbuilder.modules.builder.data.BuilderData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.shapes.Shape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class BuilderPeripheral implements IPeripheral {

    private final BuilderTileEntity builder;
    private final AttachedComputerSet attachedComputers = new AttachedComputerSet();

    public BuilderPeripheral(BuilderTileEntity builder) {
        this.builder = builder;
        this.builder.addListener(() -> attachedComputers.queueEvent(RFToolsBuilder.MODID + ":scan_complete"));
    }

    @Override
    public String getType() {
        return RFToolsBuilder.MODID + ":builder";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof BuilderPeripheral b && b.builder == this.builder;
    }

    @Override
    public void attach(IComputerAccess computer) {
        attachedComputers.add(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        attachedComputers.remove(computer);
    }

    @Override
    public @Nullable Object getTarget() {
        return builder;
    }

    // using "detail" for consistency with other CC APIs
    @LuaFunction(mainThread = true)
    public final Map<String, ?> getBuilderDetail() {
        var res = new HashMap<String, Object>();

        var data = builder.getData(BuilderModule.BUILDER_DATA);
        res.put("anchor", data.anchor());
        res.put("lastError", data.lastError());
        res.put("maxBox", pos(data.minBox()));
        res.put("minBox", pos(data.minBox()));
        res.put("scan", pos(data.scan()));
        res.put("mode", Map.of(
                "loop", data.flags().loopMode(),
                "support", data.flags().supportMode(),
                "wait", data.flags().waitMode()
        ));

        var card = builder.getCard();
        if (!card.isEmpty()) {
            res.put("shapeCard", shapeCard(card));
        }

        return res;
    }

    @LuaFunction(mainThread = true)
    public final void setWaitMode(boolean enable) {
        updateData(data -> data.withWaitMode(enable));
    }

    @LuaFunction(mainThread = true)
    public final void setLoopMode(boolean enable) {
        updateData(data -> data.withLoopMode(enable));
    }

    @LuaFunction(mainThread = true)
    public final void setRotateMode(RotateMode mode) {
        updateData(data -> data.withRotate(mode));
    }

    @LuaFunction(mainThread = true)
    public final void setAnchorMode(AnchorMode anchor) {
        updateData(data -> data.withAnchor(anchor));
    }

    @LuaFunction(mainThread = true)
    public final void setSupportMode(boolean enable) {
        builder.setSupportMode(enable);
    }

    @LuaFunction(mainThread = true)
    public final void restartScan() {
        var data = builder.getData(BuilderModule.BUILDER_DATA);
        data = builder.restartScan(data);
        builder.setData(BuilderModule.BUILDER_DATA, data);
    }

    @LuaFunction(mainThread = true)
    public final boolean setShape(Shape shape, boolean solid) {
        return updateCard(card -> ShapeCardItem.setShape(card, shape, solid));
    }

    @LuaFunction(mainThread = true)
    public final boolean setDimension(int x, int y, int z) {
        return updateCard(card -> ShapeCardItem.setDimension(card, x, y, z));
    }

    @LuaFunction(mainThread = true)
    public final boolean setOffset(int x, int y, int z) {
        return updateCard(card -> ShapeCardItem.setOffset(card, x, y, z));
    }

    private boolean updateCard(Consumer<ItemStack> update) {
        var card = builder.getCard();
        if (card.isEmpty()) {
            return false;
        }
        var data = builder.getData(BuilderModule.BUILDER_DATA);
        builder.refreshSettings();
        update.accept(card);
        if (data.flags().supportMode()) {
            builder.setSupportMode(true);
        }
        return true;
    }

    private void updateData(Function<BuilderData, BuilderData> update) {
        var data = builder.getData(BuilderModule.BUILDER_DATA);
        builder.onDataChanged(data, update.apply(data));
    }

    private static Map<String, Object> shapeCard(ItemStack card) {
        var res = new HashMap<String, Object>();
        res.put("dimension", pos(ShapeCardItem.getDimension(card)));
        res.put("maxDimension", BuilderConfiguration.maxBuilderDimension.get());
        res.put("offset", pos(ShapeCardItem.getOffset(card)));
        res.put("maxOffset", BuilderConfiguration.maxBuilderOffset.get());
        res.put("shape", ShapeCardItem.getShape(card));
        res.put("type", ShapeCardItem.getType(card));
        return res;
    }

    private static Map<String, Object> pos(BlockPos pos) {
        if (pos == null) return null;
        return Map.of(
                "x", pos.getX(),
                "y", pos.getY(),
                "z", pos.getZ()
        );
    }

}
