package fluxnetworks.common.block;

import fluxnetworks.FluxNetworks;
import fluxnetworks.common.item.ItemFluxConnector;
import fluxnetworks.common.registry.RegistryBlocks;
import fluxnetworks.common.registry.RegistryItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class BlockCore extends Block {

    public BlockCore(String name, Material materialIn) {
        super(materialIn);
        setUnlocalizedName(FluxNetworks.MODID + "." + name.toLowerCase());
        setRegistryName(name.toLowerCase());
        RegistryBlocks.BLOCKS.add(this);
        RegistryItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
        setCreativeTab(FluxNetworks.proxy.creativeTabs);
    }

    public BlockCore(String name, Material materialIn, boolean s) {
        super(materialIn);
        setUnlocalizedName(FluxNetworks.MODID + "." + name.toLowerCase());
        setRegistryName(name.toLowerCase());
        RegistryBlocks.BLOCKS.add(this);
        RegistryItems.ITEMS.add(new ItemFluxConnector(this).setRegistryName(this.getRegistryName()));
        setCreativeTab(FluxNetworks.proxy.creativeTabs);
    }

    public void registerModels() {

        FluxNetworks.proxy.registerItemModel(Item.getItemFromBlock(this), 0, "inventory");
    }
}