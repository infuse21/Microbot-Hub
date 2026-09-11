package net.runelite.client.plugins.microbot.simplemining;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.simplemining.enums.InventoryMode;
import net.runelite.client.plugins.microbot.simplemining.enums.MiningLocation;
import net.runelite.client.plugins.microbot.simplemining.enums.MiningStage;
import net.runelite.client.plugins.microbot.simplemining.enums.PickaxeType;
import net.runelite.client.plugins.microbot.simplemining.enums.SimpleMiningState;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.TimeUnit;

@Slf4j
public class SimpleMiningScript extends Script {
    private static final int INTERACT_RANGE = 12;
    private static final int LOCATION_RADIUS = 28;
    private static final int WALK_TOLERANCE = 6;
    private static final int[] CHARGED_WATERSKINS = {
            ItemID.WATERSKIN4, ItemID.WATERSKIN3, ItemID.WATERSKIN2, ItemID.WATERSKIN1
    };

    private SimpleMiningConfig config;

    @Getter
    private volatile SimpleMiningState state = SimpleMiningState.IDLE;
    @Getter
    private volatile MiningStage activeStage = MiningStage.COPPER_TIN;
    @Getter
    private volatile MiningLocation displayLocation;
    @Getter
    private volatile boolean paused;
    @Getter
    private volatile String stopReason;

    private MiningStage targetStage;
    private MiningLocation travelTarget;
    private WorldPoint customAreaAnchor;
    private boolean customAreaValidated;
    private long lastMineTime;

    public boolean run(SimpleMiningConfig config) {
        this.config = config;
        paused = false;
        stopReason = null;
        state = SimpleMiningState.IDLE;
        clearTravelTarget();
        customAreaAnchor = isCustomArea() ? Rs2Player.getWorldLocation() : null;
        customAreaValidated = false;

        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyMiningSetup();
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
                this::loop, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    public void togglePause() {
        paused = !paused;
    }

    private void loop() {
        try {
            if (paused || !super.run() || !Microbot.isLoggedIn()) {
                return;
            }

            MiningStage resolved = resolveStage();
            if (resolved != activeStage) {
                activeStage = resolved;
                clearTravelTarget();
            }
            state = determineState();

            if (state == SimpleMiningState.MINING
                    && Rs2Player.isAnimating()
                    && System.currentTimeMillis() - lastMineTime < 15000) {
                return;
            }
            if (Rs2Player.isMoving() && state != SimpleMiningState.TRAVELING) {
                return;
            }

            switch (state) {
                case GEARING:   handleGearing(); break;
                case TRAVELING: handleTraveling(); break;
                case MINING:    handleMining(); break;
                case BANKING:   handleBanking(); break;
                case DROPPING:  handleDropping(); break;
                case HOPPING:   handleHopping(); break;
                case STOPPED:   handleStopped(); break;
                default: break;
            }
        } catch (Exception ex) {
            log.error("SimpleMining loop error", ex);
        }
    }

    private MiningStage resolveStage() {
        if (!config.autoProgress()) {
            return config.manualStage();
        }
        int level = Rs2Player.getRealSkillLevel(Skill.MINING);
        int sailingLevel = Rs2Player.getRealSkillLevel(Skill.SAILING);
        return MiningStage.bestFor(level, sailingLevel, SimpleMiningPlugin.isMembersWorld(config),
                SimpleMiningPlugin.QUEST_STATES);
    }

    private SimpleMiningState determineState() {
        if (stopReason != null) {
            return SimpleMiningState.STOPPED;
        }
        int level = Rs2Player.getRealSkillLevel(Skill.MINING);
        int sailingLevel = Rs2Player.getRealSkillLevel(Skill.SAILING);
        if (level >= config.targetLevel()) {
            requestStop("Target level " + config.targetLevel() + " reached");
            return SimpleMiningState.STOPPED;
        }
        String lock = activeStage.lockReason(level, sailingLevel,
                SimpleMiningPlugin.isMembersWorld(config),
                SimpleMiningPlugin.QUEST_STATES);
        if (lock != null) {
            requestStop("Cannot mine " + activeStage.getDisplayName() + ": " + lock);
            return SimpleMiningState.STOPPED;
        }
        if (Rs2Inventory.isFull()) {
            return config.inventoryMode() == InventoryMode.DROP
                    ? SimpleMiningState.DROPPING : SimpleMiningState.BANKING;
        }
        if (PickaxeType.bestHeld() == null) {
            return SimpleMiningState.GEARING;
        }
        MiningLocation intendedLocation = chooseTravelTarget(Rs2Player.getWorldLocation());
        if (needsHeatProtection(intendedLocation) && !hasHeatProtection()) {
            return SimpleMiningState.GEARING;
        }
        if (shouldHop()) {
            return SimpleMiningState.HOPPING;
        }
        return isAtMiningArea() ? SimpleMiningState.MINING : SimpleMiningState.TRAVELING;
    }

    private void handleGearing() {
        if (!Rs2Bank.walkToBankAndUseBank()) {
            return;
        }
        PickaxeType pickaxe = bestBankOrHeldPickaxe();
        if (pickaxe == null) {
            Rs2Bank.closeBank();
            requestStop("No usable pickaxe in inventory or bank");
            return;
        }
        if (!pickaxe.isHeld() && Rs2Bank.hasBankItem(pickaxe.getItemName())) {
            Rs2Bank.withdrawItem(pickaxe.getItemName());
            sleepUntil(() -> Rs2Inventory.hasItem(pickaxe.getItemName()), 3000);
        }
        // Wield when possible; if Attack requirements prevent it, the inventory copy still works.
        if (Rs2Inventory.hasItem(pickaxe.getItemName())) {
            Rs2Inventory.wield(pickaxe.getItemName());
            sleep(400);
        }
        MiningLocation intendedLocation = chooseTravelTarget(Rs2Player.getWorldLocation());
        if (needsHeatProtection(intendedLocation) && !prepareHeatProtection()) {
            Rs2Bank.closeBank();
            requestStop("Granite requires a worn Desert amulet 4, charged circlet of water, "
                    + "or at least three charged waterskins");
            return;
        }
        Rs2Bank.closeBank();
        sleepUntil(() -> !Rs2Bank.isOpen(), 3000);
        if (PickaxeType.bestHeld() == null) {
            requestStop("Could not withdraw a usable pickaxe");
        }
    }

    private void handleTraveling() {
        WorldPoint player = Rs2Player.getWorldLocation();
        if (isCustomArea()) {
            WorldPoint anchor = ensureCustomAreaAnchor();
            if (anchor == null || player == null) {
                requestStop("Could not capture the custom mining area");
                return;
            }
            if (player.distanceTo(anchor) > LOCATION_RADIUS) {
                Rs2Walker.walkTo(anchor);
                return;
            }
            Rs2TileObjectModel customRock = findTargetRock();
            if (customRock == null) {
                if (customAreaValidated) {
                    return;
                }
                requestStop("No nearby " + activeStage.getDisplayName()
                        + " rocks found in the current area");
                return;
            }
            customAreaValidated = true;
            if (!isInRange(customRock)) {
                Rs2Walker.walkTo(customRock.getWorldLocation(), WALK_TOLERANCE);
            }
            return;
        }
        MiningLocation target = chooseTravelTarget(player);
        if (target == null) {
            requestStop("No accessible location for " + activeStage.getDisplayName());
            return;
        }
        displayLocation = target;
        Rs2TileObjectModel rock = findTargetRock();
        if (isInRange(rock)) {
            return;
        }
        if (rock != null && rock.getWorldLocation() != null
                && player != null && player.distanceTo(target.getPoint()) <= LOCATION_RADIUS) {
            Rs2Walker.walkTo(rock.getWorldLocation(), WALK_TOLERANCE);
            return;
        }
        Rs2Walker.walkTo(target.getPoint());
    }

    private void handleMining() {
        usePickaxeSpecial();
        Rs2TileObjectModel rock = findTargetRock();
        if (!isInRange(rock)) {
            if (rock != null && rock.getWorldLocation() != null) {
                Rs2Walker.walkTo(rock.getWorldLocation(), WALK_TOLERANCE);
            }
            return;
        }
        if (rock.click("Mine")) {
            lastMineTime = System.currentTimeMillis();
            Rs2Player.waitForXpDrop(Skill.MINING, true);
            safeAntibanCooldown();
        }
    }

    private void handleBanking() {
        if (!Rs2Bank.walkToBankAndUseBank()) {
            return;
        }
        for (String output : activeStage.getOutputNames()) {
            Rs2Bank.depositAll(output);
        }
        // Random gems and clue/geode drops can fill the final slots; preserve only pickaxes
        // and useful containers rather than returning to the rocks still full.
        Rs2Bank.depositAllExcept("pickaxe", "gem bag", "waterskin", "circlet of water",
                "desert amulet 4");
        Rs2Inventory.waitForInventoryChanges(1800);
        Rs2Bank.closeBank();
        sleepUntil(() -> !Rs2Bank.isOpen(), 3000);
        clearTravelTarget();
        safeAntibanCooldown();
    }

    private void handleDropping() {
        for (String output : activeStage.getOutputNames()) {
            Rs2Inventory.dropAll(output);
        }
        Rs2Inventory.waitForInventoryChanges(1800);
        safeAntibanCooldown();
    }

    private boolean hasHeatProtection() {
        if (Rs2Equipment.isWearing(ItemID.DESERT_AMULET_4, ItemID.CIRCLET_OF_WATER)) {
            return true;
        }
        return chargedWaterskinCount() > 0;
    }

    private boolean prepareHeatProtection() {
        if (Rs2Equipment.isWearing(ItemID.DESERT_AMULET_4, ItemID.CIRCLET_OF_WATER)) {
            return true;
        }
        if (withdrawAndWear(ItemID.DESERT_AMULET_4)
                || withdrawAndWear(ItemID.CIRCLET_OF_WATER)) {
            return true;
        }

        Rs2Bank.depositAll(ItemID.WATERSKIN0);
        int available = chargedWaterskinCount();
        for (int waterskin : CHARGED_WATERSKINS) {
            int needed = 3 - available;
            if (needed <= 0) {
                break;
            }
            int banked = Rs2Bank.count(waterskin);
            if (banked <= 0) {
                continue;
            }
            int before = available;
            Rs2Bank.withdrawX(waterskin, Math.min(needed, banked));
            sleepUntil(() -> chargedWaterskinCount() > before, 2500);
            available = chargedWaterskinCount();
        }
        return available >= 3;
    }

    private boolean withdrawAndWear(int itemId) {
        if (!Rs2Inventory.hasItem(itemId)) {
            if (!Rs2Bank.hasBankItem(itemId, 1) || !Rs2Bank.withdrawItem(itemId)) {
                return false;
            }
            sleepUntil(() -> Rs2Inventory.hasItem(itemId), 2500);
        }
        if (!Rs2Inventory.hasItem(itemId)) {
            return false;
        }
        Rs2Inventory.wield(itemId);
        return sleepUntil(() -> Rs2Equipment.isWearing(itemId), 2500);
    }

    private int chargedWaterskinCount() {
        int count = 0;
        for (int waterskin : CHARGED_WATERSKINS) {
            count += Rs2Inventory.itemQuantity(waterskin);
        }
        return count;
    }

    private void handleHopping() {
        int world = Login.getRandomWorld(SimpleMiningPlugin.isMembersWorld(config));
        if (Microbot.hopToWorld(world)) {
            clearTravelTarget();
            sleepUntil(Microbot::isLoggedIn, 12000);
        }
    }

    private void handleStopped() {
        log.info("SimpleMining stopped: {}", stopReason == null ? "requested" : stopReason);
        shutdown();
    }

    private boolean shouldHop() {
        int limit = config.maxPlayers();
        if (limit <= 0 || state == SimpleMiningState.TRAVELING || !isAtMiningArea()) {
            return false;
        }
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null) {
            return false;
        }
        long nearby = Microbot.getClientThread().runOnClientThreadOptional(() ->
                Microbot.getClient().getTopLevelWorldView().players().stream()
                        .filter(p -> p != null && p != Microbot.getClient().getLocalPlayer())
                        .filter(p -> p.getWorldLocation().distanceTo(player) <= INTERACT_RANGE)
                        .count()).orElse(0L);
        return nearby >= limit;
    }

    private void usePickaxeSpecial() {
        if (!config.usePickaxeSpec()) {
            return;
        }
        PickaxeType pickaxe = PickaxeType.bestHeld();
        if (pickaxe == PickaxeType.DRAGON || pickaxe == PickaxeType.DRAGON_OR
                || pickaxe == PickaxeType.INFERNAL || pickaxe == PickaxeType.THIRD_AGE
                || pickaxe == PickaxeType.CRYSTAL) {
            Rs2Combat.setSpecState(true, 1000);
        }
    }

    private MiningLocation pinnedLocation() {
        if (config == null || config.autoProgress()) {
            return null;
        }
        MiningLocation pinned = activeStage.findLocation(config.manualLocation());
        int level = Rs2Player.getRealSkillLevel(Skill.MINING);
        int sailingLevel = Rs2Player.getRealSkillLevel(Skill.SAILING);
        if (pinned == null || !pinned.isUnlocked(level, sailingLevel,
                SimpleMiningPlugin.isMembersWorld(config),
                SimpleMiningPlugin.QUEST_STATES)) {
            return null;
        }
        return pinned;
    }

    private MiningLocation chooseTravelTarget(WorldPoint player) {
        if (isCustomArea()) {
            ensureCustomAreaAnchor();
            travelTarget = null;
            targetStage = activeStage;
            return null;
        }
        MiningLocation pinned = pinnedLocation();
        if (pinned != null) {
            travelTarget = pinned;
            targetStage = activeStage;
            return pinned;
        }
        if (travelTarget != null && targetStage == activeStage) {
            return travelTarget;
        }
        int level = Rs2Player.getRealSkillLevel(Skill.MINING);
        int sailingLevel = Rs2Player.getRealSkillLevel(Skill.SAILING);
        travelTarget = activeStage.fastestLocation(player, level, sailingLevel,
                SimpleMiningPlugin.isMembersWorld(config), SimpleMiningPlugin.QUEST_STATES,
                SimpleMiningScript::pathTiles, config.autoProgress());
        targetStage = activeStage;
        return travelTarget;
    }

    private boolean isAtMiningArea() {
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null) {
            return false;
        }
        if (isCustomArea()) {
            WorldPoint anchor = ensureCustomAreaAnchor();
            Rs2TileObjectModel rock = findTargetRock();
            if (rock != null) {
                customAreaValidated = true;
            }
            return anchor != null && player.distanceTo(anchor) <= LOCATION_RADIUS
                    && (customAreaValidated || rock != null);
        }
        MiningLocation target = chooseTravelTarget(player);
        if (target != null && player.distanceTo(target.getPoint()) <= LOCATION_RADIUS) {
            displayLocation = target;
            return true;
        }
        return isInRange(findNearestRock());
    }

    private Rs2TileObjectModel findNearestRock() {
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }
        try {
            Rs2TileObjectModel rock = Microbot.getRs2TileObjectCache().query()
                    .withIds(activeStage.getObjectIds())
                    .nearestOnClientThread();
            if (rock != null || activeStage.getObjectNames().length == 0) {
                return rock;
            }
            // Named fallback covers future cache-revision variants without ever treating a
            // generic depleted quarry rock as mineable (Granite deliberately has no fallback).
            return Microbot.getRs2TileObjectCache().query()
                    .withNames(activeStage.getObjectNames())
                    .nearestOnClientThread();
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null
                    && ex.getMessage().toLowerCase().contains("interrupted waiting for client thread")) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    private boolean isInRange(Rs2TileObjectModel rock) {
        WorldPoint player = Rs2Player.getWorldLocation();
        return rock != null && player != null && rock.getWorldLocation() != null
                && player.distanceTo(rock.getWorldLocation()) <= INTERACT_RANGE;
    }

    private Rs2TileObjectModel findTargetRock() {
        Rs2TileObjectModel rock = findNearestRock();
        if (!isCustomArea()) {
            return rock;
        }
        WorldPoint anchor = ensureCustomAreaAnchor();
        return rock != null && anchor != null && rock.getWorldLocation() != null
                && rock.getWorldLocation().distanceTo(anchor) <= LOCATION_RADIUS
                ? rock : null;
    }

    private boolean isCustomArea() {
        return config != null && !config.autoProgress()
                && SimpleMiningConfig.CURRENT_AREA.equals(config.manualLocation());
    }

    private WorldPoint ensureCustomAreaAnchor() {
        if (customAreaAnchor == null && isCustomArea()) {
            customAreaAnchor = Rs2Player.getWorldLocation();
        }
        return customAreaAnchor;
    }

    private boolean needsHeatProtection(MiningLocation intendedLocation) {
        if (intendedLocation != null) {
            return intendedLocation.isDesertHeat();
        }
        if (!isCustomArea() || activeStage != MiningStage.GRANITE) {
            return false;
        }
        WorldPoint anchor = ensureCustomAreaAnchor();
        if (anchor == null) {
            return false;
        }
        for (MiningLocation location : MiningStage.GRANITE.getLocations()) {
            if (location.isDesertHeat()
                    && anchor.distanceTo(location.getPoint()) <= LOCATION_RADIUS * 2) {
                return true;
            }
        }
        return false;
    }

    private PickaxeType bestBankOrHeldPickaxe() {
        PickaxeType best = null;
        for (PickaxeType pickaxe : PickaxeType.values()) {
            if (pickaxe.meetsLevel()
                    && (pickaxe.isHeld() || Rs2Bank.hasBankItem(pickaxe.getItemName()))) {
                best = pickaxe;
            }
        }
        return best;
    }

    private static int pathTiles(WorldPoint from, WorldPoint to) {
        try {
            return Rs2Walker.getTotalTiles(from, to);
        } catch (Exception ignored) {
            return Integer.MAX_VALUE;
        }
    }

    private void requestStop(String reason) {
        if (stopReason == null) {
            stopReason = reason;
            log.warn("SimpleMining stopping: {}", reason);
        }
        state = SimpleMiningState.STOPPED;
    }

    private void safeAntibanCooldown() {
        try {
            Rs2Antiban.actionCooldown();
            Rs2Antiban.takeMicroBreakByChance();
        } catch (IllegalArgumentException ex) {
            log.debug("Antiban cooldown skipped: {}", ex.getMessage());
        }
    }

    private void clearTravelTarget() {
        travelTarget = null;
        targetStage = null;
        displayLocation = null;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        paused = false;
        clearTravelTarget();
        customAreaAnchor = null;
        customAreaValidated = false;
        Rs2Antiban.resetAntibanSettings();
        state = SimpleMiningState.IDLE;
    }
}
