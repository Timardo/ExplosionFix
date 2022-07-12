package net.timardo.explosionfix;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculatorEntity;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.piston.BlockPistonMoving;
import net.minecraft.world.level.block.piston.TileEntityPiston;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public class ExplosionFix extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new ExplosionEventHandler(), this);
    }
    
    private class ExplosionEventHandler implements Listener {
        
        @EventHandler
        public void onExplosion(EntityExplodeEvent explodeEvent) {
            Entity nmsEntity = ((CraftEntity)explodeEvent.getEntity()).getHandle();
            ExplosionDamageCalculatorEntity damageCalculator = new ExplosionDamageCalculatorEntity(nmsEntity);
            
            for (Block block : ((ObjectArrayList<Block>)explodeEvent.blockList()).clone()) {
                CraftBlock craftBlock = (CraftBlock) block;
                var nmsBlock = craftBlock.getNMS().b();
                
                if (nmsBlock instanceof BlockPistonMoving pistonBlock) { // moving piston
                    TileEntity tileEntity = craftBlock.getHandle().c_(craftBlock.getPosition());
                    
                    if (tileEntity instanceof TileEntityPiston movingTile) {
                        IBlockData blockState = movingTile.i();
                        float f = ((Explosive)nmsEntity.getBukkitEntity()).getYield() * (0.7F + ((World)craftBlock.getHandle()).w.i() * 0.6F);
                        Fluid fluidState = ((World)craftBlock.getHandle()).b_(craftBlock.getPosition());
                        Optional<Float> optional = damageCalculator.a(null, craftBlock.getHandle(), craftBlock.getPosition(), blockState, fluidState); // Explosion param is null as it does not seem to be used anywhere
                        
                        if (optional.isPresent()) {
                            f -= (optional.get() + 0.3F) * 0.3F;
                        }
                        
                        if (!(f > 0.0F && damageCalculator.a(null, craftBlock.getHandle(), craftBlock.getPosition(), blockState, f))) {
                            explodeEvent.blockList().remove(block);
                        }
                    }
                }
            }
        }
    }
}
