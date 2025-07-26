package mcjty.rftoolsbuilder.modules.cc_tweaked;

import dan200.computercraft.api.lua.GenericSource;
import dan200.computercraft.api.lua.LuaFunction;
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class BuilderPeripheral implements GenericSource {

    @Override
    public String id() {
        return BuilderModule.BUILDER.block().getKey().location().toString();
    }

    // using "detail" for consistency with other CC APIs
    @LuaFunction(mainThread = true)
    public Map<String, ?> getBuilderDetail(BuilderTileEntity te) {
        var res = new HashMap<String, Object>();

        var data = te.getData(BuilderModule.BUILDER_DATA);
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

        var card = te.getCard();
        if (!card.isEmpty()) {
            res.put("shapeCard", shapeCard(card));
        }

        return res;
    }

    @LuaFunction(mainThread = true)
    public void setWaitMode(BuilderTileEntity te, boolean enable) {
        updateData(te, data -> data.withWaitMode(enable));
    }

    @LuaFunction(mainThread = true)
    public void setLoopMode(BuilderTileEntity te, boolean enable) {
        updateData(te, data -> data.withLoopMode(enable));
    }

    @LuaFunction(mainThread = true)
    public void setRotateMode(BuilderTileEntity te, RotateMode mode) {
        updateData(te, data -> data.withRotate(mode));
    }

    @LuaFunction(mainThread = true)
    public void setAnchorMode(BuilderTileEntity te, AnchorMode anchor) {
        updateData(te, data -> data.withAnchor(anchor));
    }

    @LuaFunction(mainThread = true)
    public void setSupportMode(BuilderTileEntity te, boolean enable) {
        te.setSupportMode(enable);
    }

    @LuaFunction(mainThread = true)
    public void restartScan(BuilderTileEntity te) {
        var data = te.getData(BuilderModule.BUILDER_DATA);
        data = te.restartScan(data);
        te.setData(BuilderModule.BUILDER_DATA, data);
    }

    @LuaFunction(mainThread = true)
    public boolean setShape(BuilderTileEntity te, Shape shape, boolean solid) {
        return updateCard(te, card -> ShapeCardItem.setShape(card, shape, solid));
    }

    @LuaFunction(mainThread = true)
    public boolean setDimension(BuilderTileEntity te, int x, int y, int z) {
        return updateCard(te, card -> ShapeCardItem.setDimension(card, x, y, z));
    }

    @LuaFunction(mainThread = true)
    public boolean setOffset(BuilderTileEntity te, int x, int y, int z) {
        return updateCard(te, card -> ShapeCardItem.setOffset(card, x, y, z));
    }

    private boolean updateCard(BuilderTileEntity te, Consumer<ItemStack> update) {
        var card = te.getCard();
        if (card.isEmpty()) {
            return false;
        }
        var data = te.getData(BuilderModule.BUILDER_DATA);
        te.refreshSettings();
        update.accept(card);
        if (data.flags().supportMode()) {
            te.setSupportMode(true);
        }
        return true;
    }

    private void updateData(BuilderTileEntity te, Function<BuilderData, BuilderData> update) {
        var data = te.getData(BuilderModule.BUILDER_DATA);
        te.onDataChanged(data, update.apply(data));
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
