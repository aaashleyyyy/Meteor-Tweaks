/*
 * Most of this file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package me.declipsonator.meteortweaks.modules;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.declipsonator.meteortweaks.utils.TweaksUtil;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;
import java.util.UUID;

import static meteordevelopment.meteorclient.utils.player.ChatUtils.formatCoords;

public class Notifier extends Module {

    public enum joinLeave {
        Join,
        Leave,
        Both
    }

    private final SettingGroup sgTotemPops = settings.createGroup("Totem Pops");
    private final SettingGroup sgVisualRange = settings.createGroup("Visual Range");
    private final SettingGroup sgJoinLeave = settings.createGroup("Join/Leave Messages");

    // Totem Pops

    private final Setting<Boolean> totemPops = sgTotemPops.add(new BoolSetting.Builder()
        .name("totem-pops")
        .description("Notifies you when a player pops a totem.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> totemsIgnoreOwn = sgTotemPops.add(new BoolSetting.Builder()
        .name("ignore-own")
        .description("Notifies you of your own totem pops.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> totemsIgnoreFriends = sgTotemPops.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Ignores friends totem pops.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> totemsIgnoreOthers = sgTotemPops.add(new BoolSetting.Builder()
        .name("ignore-others")
        .description("Ignores other players totem pops.")
        .defaultValue(false)
        .build()
    );

    // Visual Range

    private final Setting<Boolean> visualRange = sgVisualRange.add(new BoolSetting.Builder()
        .name("visual-range")
        .description("Notifies you when an entity enters your render distance.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Event> event = sgVisualRange.add(new EnumSetting.Builder<Event>()
        .name("event")
        .description("When to log the entities.")
        .defaultValue(Event.Both)
        .build()
    );

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgVisualRange.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Which entities to nofity about.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Boolean> visualRangeIgnoreFriends = sgVisualRange.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Ignores friends.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> visualRangeIgnoreFakes = sgVisualRange.add(new BoolSetting.Builder()
        .name("ignore-fake-players")
        .description("Ignores fake players.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> joinLeaveMessages = sgJoinLeave.add(new BoolSetting.Builder()
        .name("join-leave-messages")
        .description("Whether or not to notify you of players joining or leaving the server.")
        .defaultValue(false)
        .build()
    );

    private final Setting<joinLeave> joinOrLeave = sgJoinLeave.add(new EnumSetting.Builder<joinLeave>()
        .name("messages")
        .description("What messages to send.")
        .defaultValue(joinLeave.Both)
        .build()
    );

    private final Setting<Boolean> friendsJoinLeave = sgJoinLeave.add(new BoolSetting.Builder()
            .name("ignore-friends")
            .description("Ignore friends.")
            .defaultValue(false)
            .build()
    );


    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap<>();
    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap<>();

    private final Random random = new Random();

    public Notifier() {
        super(Categories.Misc, "notifier", "Notifies you of different events.");
    }


    // Visual Range

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (event.entity.getUuid().equals(mc.player.getUuid()) || !entities.get().getBoolean(event.entity.getType()) || !visualRange.get() || this.event.get() == Event.Despawn) return;

        if (event.entity instanceof PlayerEntity) {
            if ((!visualRangeIgnoreFriends.get() || !Friends.get().isFriend(((PlayerEntity) event.entity))) && (!visualRangeIgnoreFakes.get() || !(event.entity instanceof FakePlayerEntity))) {
                ChatUtils.sendMsg(event.entity.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has entered your visual range!", event.entity.getEntityName());
            }
        }
        else {
            MutableText text = new LiteralText(event.entity.getType().getName().getString()).formatted(Formatting.WHITE);
            text.append(new LiteralText(" has spawned at ").formatted(Formatting.GRAY));
            text.append(formatCoords(event.entity.getPos()));
            text.append(new LiteralText(".").formatted(Formatting.GRAY));
            info(text);
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        if (event.entity.getUuid().equals(mc.player.getUuid()) || !entities.get().getBoolean(event.entity.getType()) || !visualRange.get() || this.event.get() == Event.Spawn) return;

        if (event.entity instanceof PlayerEntity) {
            if ((!visualRangeIgnoreFriends.get() || !Friends.get().isFriend(((PlayerEntity) event.entity))) && (!visualRangeIgnoreFakes.get() || !(event.entity instanceof FakePlayerEntity))) {
                ChatUtils.sendMsg(event.entity.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has left your visual range!", event.entity.getEntityName());
            }
        } else {
            MutableText text = new LiteralText(event.entity.getType().getName().getString()).formatted(Formatting.WHITE);
            text.append(new LiteralText(" has despawned at ").formatted(Formatting.GRAY));
            text.append(formatCoords(event.entity.getPos()));
            text.append(new LiteralText(".").formatted(Formatting.GRAY));
            info(text);
        }
    }

    // Totem Pops

    @Override
    public void onActivate() {
        totemPopMap.clear();
        chatIdMap.clear();
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        totemPopMap.clear();
        chatIdMap.clear();

    }



    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (totemPops.get()) {
            if (event.packet instanceof EntityStatusS2CPacket) {

                EntityStatusS2CPacket p = (EntityStatusS2CPacket) event.packet;
                if (p.getStatus() != 35) {

                    Entity entity = p.getEntity(mc.world);

                    if (entity instanceof PlayerEntity) {

                        if (!((entity.equals(mc.player) && totemsIgnoreOwn.get())
                                || (Friends.get().isFriend(((PlayerEntity) entity)) && totemsIgnoreOthers.get())
                                || (!Friends.get().isFriend(((PlayerEntity) entity)) && totemsIgnoreFriends.get()))
                        ) {

                            synchronized (totemPopMap) {
                                int pops = totemPopMap.getOrDefault(entity.getUuid(), 0);
                                totemPopMap.put(entity.getUuid(), ++pops);

                                ChatUtils.sendMsg(getChatId(entity), Formatting.GRAY, "(highlight)%s (default)popped (highlight)%d (default)%s.", entity.getEntityName(), pops, pops == 1 ? "totem" : "totems");
                            }
                        }
                    }
                }
            }
        }

        if (joinLeaveMessages.get()) {
            if(!(event.packet instanceof PlayerListS2CPacket)) return;
            for(PlayerListS2CPacket.Entry entry : ((PlayerListS2CPacket) event.packet).getEntries()) {

                if(friendsJoinLeave.get() && Friends.get().get(TweaksUtil.uuidToAccount(entry.getProfile().getId()).getName()) != null) continue;

                //2 different methods for join and leave. Using the same results in null for one of the 2
                if(((PlayerListS2CPacket) event.packet).getAction() == PlayerListS2CPacket.Action.ADD_PLAYER && joinOrLeave.get() == joinLeave.Both || joinOrLeave.get() == joinLeave.Join) {
                    ChatUtils.sendMsg("Notifier", Text.of(entry.getProfile().getName() + " joined the server."));
                } else if(((PlayerListS2CPacket) event.packet).getAction() == PlayerListS2CPacket.Action.REMOVE_PLAYER && joinOrLeave.get() == joinLeave.Both || joinOrLeave.get() == joinLeave.Leave) {
                    ChatUtils.sendMsg("Notifier", Text.of(TweaksUtil.uuidToAccount(entry.getProfile().getId()).getName() + " left the server."));
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!totemPops.get()) return;
        synchronized (totemPopMap) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!totemPopMap.containsKey(player.getUuid())) continue;

                if (player.deathTime > 0 || player.getHealth() <= 0) {
                    int pops = totemPopMap.removeInt(player.getUuid());

                    ChatUtils.sendMsg(getChatId(player), Formatting.GRAY, "(highlight)%s (default)died after popping (highlight)%d (default)%s.", player.getEntityName(), pops, pops == 1 ? "totem" : "totems");
                    chatIdMap.removeInt(player.getUuid());
                }
            }
        }
    }

    private int getChatId(Entity entity) {
        return chatIdMap.computeIntIfAbsent(entity.getUuid(), value -> random.nextInt());
    }

    public enum Event {
        Spawn,
        Despawn,
        Both
    }
}