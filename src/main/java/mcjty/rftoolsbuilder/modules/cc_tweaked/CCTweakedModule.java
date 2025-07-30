package mcjty.rftoolsbuilder.modules.cc_tweaked;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import static mcjty.rftoolsbuilder.modules.builder.BuilderModule.BUILDER;

public class CCTweakedModule implements IModule {

    @Override
    public void init(FMLCommonSetupEvent event) {
        registerPeripherals();
    }

    @Override
    public void initClient(FMLClientSetupEvent fmlClientSetupEvent) {
    }

    @Override
    public void initConfig(IEventBus bus) {
        bus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(PeripheralCapability.get(), BUILDER.be().get(), (b, d) -> new BuilderPeripheral(b));
        });
    }

    private void registerPeripherals() {
//        ComputerCraftAPI.registerGenericSource(new BuilderPeripheral());
    }

}
