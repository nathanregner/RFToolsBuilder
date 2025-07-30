package mcjty.rftoolsbuilder.modules.builder.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.BaseBEData;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.varia.RedstoneMode;
import mcjty.lib.varia.TagTools;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.function.Function;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.lib.setup.Registration.BASE_BE_DATA;

public class ComposerTileEntity extends TickingTileEntity {

    public static final int SLOT_TAB = 0;
    public static final int SLOT_COUNT = 9;
    public static final int SLOT_OUT = 0;

    public static final ResourceLocation DONT_REMOVE_ME = ResourceLocation.fromNamespaceAndPath(RFToolsBuilder.MODID, "dontremoveme");
    public static final TagKey<Block> DONT_REMOVE_ME_TAG = TagTools.createBlockTagKey(DONT_REMOVE_ME);

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> {
        // TODO
        return new ContainerFactory(SLOT_COUNT)
                .slot(specific(s -> s.getItem() instanceof ShapeCardItem), 0, 100, 10)
                .slot(specific(s -> s.getItem() instanceof ShapeCardItem), 1, 100, 20)
                .playerSlots(10, 70);
    });

    private final GenericItemHandler items = new GenericItemHandler(this, CONTAINER_FACTORY.get()) {
    };

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private static final Function<ComposerTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;

    @Cap(type = CapType.CONTAINER)
    private static final Function<ComposerTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider<GenericContainer>("Composer")
            .containerSupplier(container(BuilderModule.CONTAINER_COMPOSER, CONTAINER_FACTORY, tile))
            .itemHandler(() -> tile.items)
            // .shortListener(Sync.integer(() -> tile.getScan() == null ? -1 : tile.getScan().getY(), v -> currentLevel = v))
//            .data(BuilderModule.BUILDER_DATA, BuilderData.STREAM_CODEC, BuilderData.CODEC)
            .data(BASE_BE_DATA, BaseBEData.STREAM_CODEC, BaseBEData.CODEC)
            .setupSync(tile);

    public ComposerTileEntity(BlockPos pos, BlockState state) {
        // FIXME
        super(BuilderModule.BUILDER.be().get(), pos, state);
        setRSMode(RedstoneMode.REDSTONE_IGNORED);
    }

    void update() {
    }
}
