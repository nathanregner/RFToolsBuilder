package mcjty.rftoolsbuilder.modules.cc_tweaked;

import dan200.computercraft.api.ComputerCraftAPI;
import mcjty.lib.modules.IModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class CCTweakedModule implements IModule {

    @Override
    public void init(FMLCommonSetupEvent event) {
        registerPeripherals();
    }

    @Override
    public void initClient(FMLClientSetupEvent fmlClientSetupEvent) {
    }

    @Override
    public void initConfig(IEventBus iEventBus) {
    }

    private void registerPeripherals() {
        ComputerCraftAPI.registerGenericSource(new BuilderPeripheral());
    }

}
