package me.declipsonator.meteortweaks;

import me.declipsonator.meteortweaks.commands.ReloadBlocks;
import me.declipsonator.meteortweaks.commands.SoftLeave;
import me.declipsonator.meteortweaks.hud.ItemCountHud;
import me.declipsonator.meteortweaks.mixins.ElytraFlyMixin;
import me.declipsonator.meteortweaks.modules.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class MeteorTweaks extends MeteorAddon {
	public static final Logger LOG = LogManager.getLogger();

	@Override
	public void onInitialize() {
		LOG.info("Initializing Meteor Tweaks Addon");

		// Required when using @EventHandler
		MeteorClient.EVENT_BUS.registerLambdaFactory("me.declipsonator.meteortweaks", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));


		// Modules
		Modules.get().add(new Scaffold());
		Modules.get().add(new VelocityBoost());
		Modules.get().add(new GUIMove());
		Modules.get().add(new AutoTool());
		Modules.get().add(new Notifier());
		Modules.get().add(new TpsSync());
		Modules.get().add(new RideStats());
		Modules.get().add(new GroupChat());
		Modules.get().add(new AutoSign());
		Modules.get().add(new DeathExplore());
		Modules.get().add(new EntitySpeed());
		Modules.get().add(new MultiTask());
		Modules.get().add(new Confetti());

		// Commands
		Commands.get().add(new SoftLeave());
		Commands.get().add(new ReloadBlocks());

		HUD hud = Systems.get(HUD.class);
		hud.elements.add(new ItemCountHud(hud));

		Modules.get().get(ElytraFly.class).getInfoString();
	}



}
