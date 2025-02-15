package me.declipsonator.meteortweaks.mixins;

import me.declipsonator.meteortweaks.events.BoatMovementEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    public EntityType<?> getType() {
        return null;
    }


    @Inject(method = "move", at = @At(value = "HEAD"))
    private void onTickInvokeMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if(this.getType() != EntityType.BOAT) return;
        MeteorClient.EVENT_BUS.post(BoatMovementEvent.get((BoatEntity) (Object) this, movement));
    }
}