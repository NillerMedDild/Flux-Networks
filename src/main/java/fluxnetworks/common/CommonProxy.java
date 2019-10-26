package fluxnetworks.common;

import com.google.common.collect.Lists;
import fluxnetworks.FluxConfig;
import fluxnetworks.FluxNetworks;
import fluxnetworks.api.gui.EnumFeedbackInfo;
import fluxnetworks.api.network.IFluxNetwork;
import fluxnetworks.common.capabilities.DefaultSuperAdmin;
import fluxnetworks.common.connection.FluxNetworkInvalid;
import fluxnetworks.common.core.EntityFireItem;
import fluxnetworks.api.utils.NBTType;
import fluxnetworks.common.data.FluxChunkManager;
import fluxnetworks.common.event.FluxConnectionEvent;
import fluxnetworks.common.handler.CapabilityHandler;
import fluxnetworks.common.handler.PacketHandler;
import fluxnetworks.common.handler.TileEntityHandler;
import fluxnetworks.common.connection.FluxNetworkCache;
import fluxnetworks.common.integration.MekanismIntegration;
import fluxnetworks.common.integration.TOPIntegration;
import fluxnetworks.common.integration.oc.OCIntegration;
import fluxnetworks.common.network.PacketNetworkUpdate;
import fluxnetworks.common.network.PacketSuperAdmin;
import fluxnetworks.common.registry.RegistryBlocks;
import fluxnetworks.common.registry.RegistryItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CommonProxy {

    public boolean baublesLoaded;
    public boolean ocLoaded;

    public int admin_viewing_network_id = -1;
    public boolean detailed_network_view;
    public IFluxNetwork admin_viewing_network = FluxNetworkInvalid.instance;

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        PacketHandler.registerMessages();
        TileEntityHandler.registerEnergyHandler();
        FluxConfig.init(event.getModConfigurationDirectory());
        EntityRegistry.registerModEntity(new ResourceLocation(FluxNetworks.MODID, "Flux"), EntityFireItem.class, "Flux", 0, FluxNetworks.instance, 64, 10, true);
        if(Loader.isModLoaded("mekanism")){
            MekanismIntegration.preInit();
        }
        this.ocLoaded = Loader.isModLoaded("opencomputers");
        this.baublesLoaded = Loader.isModLoaded("baubles");
    }

    public void init(FMLInitializationEvent event) {
        DefaultSuperAdmin.register();
        FMLInterModComms.sendMessage("carryon", "blacklistBlock",  FluxNetworks.MODID + ":*");
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", TOPIntegration.class.getName());
        if(ocLoaded) {
            OCIntegration.init();
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        ForgeChunkManager.setForcedChunkLoadingCallback(FluxNetworks.instance, FluxChunkManager::callback);
        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
    }

    public void onServerStarted() {

    }

    public void onServerStopped() {
        FluxNetworkCache.instance.clearNetworks();
        FluxChunkManager.clear();
    }

    public static CreativeTabs creativeTabs = new CreativeTabs(FluxNetworks.MODID) {

        @Override
        public ItemStack createIcon() {
            return new ItemStack(RegistryBlocks.FLUX_PLUG);
        }
    };

    public void registerItemModel(Item item, int meta, String variant) {

    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
        if(event.getSide().isServer()) {
            if(!FluxConfig.enableFluxRecipe) {
                return;
            }
            World world = event.getWorld();
            BlockPos pos = event.getPos();
            if (world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) && world.getBlockState(pos.down(2)).getBlock().equals(Blocks.BEDROCK)) {
                List<EntityItem> entities = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.down()));
                if(entities.isEmpty())
                    return;
                List<EntityItem> s = Lists.newArrayList();
                AtomicInteger count = new AtomicInteger();
                entities.forEach(e -> {
                    if (e.getItem().getItem().equals(Items.REDSTONE)) {
                        s.add(e);
                        count.addAndGet(e.getItem().getCount());
                    }
                });
                if (s.isEmpty())
                    return;
                ItemStack stack = new ItemStack(RegistryItems.FLUX, count.getAndIncrement());
                s.forEach(Entity::setDead);
                world.setBlockToAir(pos);
                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, stack));
                world.setBlockState(pos.down(), Blocks.OBSIDIAN.getDefaultState());
                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        /*if(event.phase == TickEvent.Phase.START) {
            for(IFluxNetwork network : FluxNetworkCache.instance.getAllNetworks()) {
                network.onStartServerTick();
            }
        }*/
        if(event.phase == TickEvent.Phase.END) {
            for(IFluxNetwork network : FluxNetworkCache.instance.getAllNetworks()) {
                network.onEndServerTick();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if(!player.world.isRemote) {
            PacketHandler.network.sendTo(new PacketNetworkUpdate.NetworkUpdateMessage(new ArrayList<>(FluxNetworkCache.instance.getAllNetworks()), NBTType.NETWORK_GENERAL), (EntityPlayerMP) player);
            PacketHandler.network.sendTo(new PacketSuperAdmin.SuperAdminMessage(DefaultSuperAdmin.isPlayerSuperAdmin(player)), (EntityPlayerMP) player);
        }
    }

    @SubscribeEvent
    public void onFluxConnected(FluxConnectionEvent.Connected event) {
        if(!event.flux.getDimension().isRemote) {
            event.flux.connect(event.network);
        }
    }

    @SubscribeEvent
    public void onFluxDisconnect(FluxConnectionEvent.Disconnected event) {
        if(!event.flux.getDimension().isRemote) {
            event.flux.disconnect(event.network);
        }
    }

    public EnumFeedbackInfo getFeedback(boolean operation) {
        return null;
    }

    public void setFeedback(EnumFeedbackInfo info, boolean operation) {}

    public void receiveColorCache(Map<Integer, Tuple<Integer, String>> cache) {}

    public EntityPlayer getPlayer(MessageContext ctx) {
        return ctx.getServerHandler().player;
    }
}
