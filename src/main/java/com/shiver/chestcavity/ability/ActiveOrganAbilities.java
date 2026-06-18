package com.shiver.chestcavity.ability;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.entity.EntityForcefulSpit;
import com.shiver.chestcavity.integration.crafttweaker.callback.AbilityCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.runtime.AbilityDefinition;
import com.shiver.chestcavity.integration.crafttweaker.runtime.AbilityRegistry;
import com.shiver.chestcavity.potion.FurnacePower;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ActiveOrganAbilities {

    public static final ResourceLocation FURNACE_POWERED = CCOrganScores.FURNACE_POWERED;
    public static final ResourceLocation GRAZING = CCOrganScores.GRAZING;
    public static final ResourceLocation IRON_REPAIR = CCOrganScores.IRON_REPAIR;
    public static final ResourceLocation BUOYANT = CCOrganScores.BUOYANT;
    public static final ResourceLocation CREEPY = CCOrganScores.CREEPY;
    public static final ResourceLocation PYROMANCY = CCOrganScores.PYROMANCY;
    public static final ResourceLocation DRAGON_BOMBS = CCOrganScores.DRAGON_BOMBS;
    public static final ResourceLocation FORCEFUL_SPIT = CCOrganScores.FORCEFUL_SPIT;
    public static final ResourceLocation GHASTLY = CCOrganScores.GHASTLY;
    public static final ResourceLocation SHULKER_BULLETS = CCOrganScores.SHULKER_BULLETS;
    public static final ResourceLocation SILK = CCOrganScores.SILK;

    private static final float BUOYANT_AIR_COST = 4.5F;
    private static final float DRAGON_BOMB_EXHAUSTION = 0.6F;
    private static final double DRAGON_BOMB_RECOIL = 0.2D;
    private static final float FORCEFUL_SPIT_EXHAUSTION = 0.1F;
    private static final double FORCEFUL_SPIT_RECOIL = 0.1D;
    private static final float FORCEFUL_SPIT_VELOCITY = 2.0F;
    private static final float GHASTLY_EXHAUSTION = 0.3F;
    private static final double GHASTLY_RECOIL = 0.8D;
    private static final float PYROMANCY_EXHAUSTION = 0.1F;
    private static final double PYROMANCY_RECOIL = 0.2D;
    private static final float SHULKER_BULLET_EXHAUSTION = 0.3F;
    private static final Map<ResourceLocation, ActiveOrganAbility> ABILITIES = new LinkedHashMap<ResourceLocation, ActiveOrganAbility>();

    static {
        register(FURNACE_POWERED, ActiveOrganAbilities::activateFurnacePowered);
        register(GRAZING, ActiveOrganAbilities::activateGrazing);
        register(IRON_REPAIR, ActiveOrganAbilities::activateIronRepair);
        register(BUOYANT, ActiveOrganAbilities::activateBuoyant);
        register(CREEPY, ActiveOrganAbilities::activateCreepy);
        register(PYROMANCY, ActiveOrganAbilities::activatePyromancy);
        register(DRAGON_BOMBS, ActiveOrganAbilities::activateDragonBombs);
        register(FORCEFUL_SPIT, ActiveOrganAbilities::activateForcefulSpit);
        register(GHASTLY, ActiveOrganAbilities::activateGhastly);
        register(SHULKER_BULLETS, ActiveOrganAbilities::activateShulkerBullets);
        register(SILK, ActiveOrganAbilities::activateSilk);
    }

    private ActiveOrganAbilities() {
    }

    public static void register(ResourceLocation id, ActiveOrganAbility ability) {
        ABILITIES.put(id, ability);
    }

    public static boolean activate(EntityPlayerMP player, IChestCavity chestCavity, ResourceLocation abilityId) {
        ActiveOrganAbility ability = ABILITIES.get(abilityId);
        if (chestCavity.getOrganScore(abilityId) <= 0.0F) {
            ChestCavityLegacy.LOGGER.debug("Ignoring inactive organ ability {} for {}.", abilityId, player.getName());
            return false;
        }
        if (ability == null) {
            return activateScriptAbility(player, chestCavity, abilityId);
        }
        return ability.activate(player, chestCavity);
    }

    public static List<ResourceLocation> getRegisteredAbilityIds() {
        List<ResourceLocation> result = new java.util.ArrayList<ResourceLocation>(ABILITIES.keySet());
        for (ResourceLocation id : AbilityRegistry.getDefinitions().keySet()) {
            if (!result.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    public static boolean fireQueuedProjectile(EntityLivingBase entity, IChestCavity chestCavity, ResourceLocation abilityId) {
        if (!(entity instanceof EntityPlayerMP)) {
            return false;
        }
        EntityPlayerMP player = (EntityPlayerMP) entity;
        if (PYROMANCY.equals(abilityId)) {
            return spawnPyromancyFireball(player);
        }
        if (DRAGON_BOMBS.equals(abilityId)) {
            return spawnDragonBomb(player);
        }
        if (FORCEFUL_SPIT.equals(abilityId)) {
            return spawnForcefulSpit(player);
        }
        if (GHASTLY.equals(abilityId)) {
            return spawnGhastlyFireball(player);
        }
        if (SHULKER_BULLETS.equals(abilityId)) {
            return spawnShulkerBullet(player);
        }
        return false;
    }

    private static boolean activateFurnacePowered(EntityPlayerMP player, IChestCavity chestCavity) {
        float furnacePoweredScore = chestCavity.getOrganScore(FURNACE_POWERED);
        if (furnacePoweredScore <= 0.0F) {
            return false;
        }
        int furnacePowered = Math.max(1, Math.round(furnacePoweredScore));

        if (FurnacePower.getActiveLayerCount(player) >= furnacePowered) {
            return false;
        }

        FuelStack fuel = findFuel(player);
        if (fuel == null) {
            return false;
        }

        if (!FurnacePower.addFuelLayer(player, fuel.burnTime, furnacePowered)) {
            return false;
        }
        consumeFuel(player, fuel);
        return true;
    }

    private static boolean activateGrazing(EntityPlayerMP player, IChestCavity chestCavity) {
        float grazing = chestCavity.getOrganScore(GRAZING);
        if (grazing <= 0.0F) {
            return false;
        }

        World world = player.world;
        BlockPos blockPos = player.getPosition().down();
        IBlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        if (block != Blocks.GRASS && block != Blocks.MYCELIUM) {
            return false;
        }

        world.playEvent(2001, blockPos, Block.getStateId(state));
        world.setBlockState(blockPos, Blocks.DIRT.getDefaultState(), 2);

        PotionEffect current = player.getActivePotionEffect(CCPotions.RUMINATING);
        int grassDuration = Math.max(1, CCConfig.RUMINATION_TIME * CCConfig.RUMINATION_GRASS_PER_SQUARE);
        int maxDuration = Math.max(grassDuration,
                Math.round(grazing * grassDuration * CCConfig.RUMINATION_SQUARES_PER_STOMACH));
        int duration = current == null ? grassDuration : Math.min(maxDuration, current.getDuration() + grassDuration);
        player.addPotionEffect(new PotionEffect(CCPotions.RUMINATING, duration, 0, false, true));
        return true;
    }

    private static boolean activateBuoyant(EntityPlayerMP player, IChestCavity chestCavity) {
        if (player.getAir() <= 0) {
            return false;
        }

        float buoyancy = chestCavity.getOrganScore(BUOYANT);
        if (buoyancy <= 0.0F) {
            return false;
        }

        float airLoss = buoyancy * BUOYANT_AIR_COST + chestCavity.getLungRemainder();
        int wholeAirLoss = (int) airLoss;
        chestCavity.setLungRemainder(airLoss - wholeAirLoss);
        if (wholeAirLoss <= 0) {
            return false;
        }

        player.setAir(Math.max(0, player.getAir() - wholeAirLoss));
        player.motionY -= Math.min(0.5D, buoyancy * CCConfig.BUOYANCY_LIFT * BUOYANT_AIR_COST);
        player.velocityChanged = true;
        return true;
    }

    private static boolean activateCreepy(EntityPlayerMP player, IChestCavity chestCavity) {
        float creepy = chestCavity.getOrganScore(CREEPY);
        if (creepy <= 0.0F || player.isPotionActive(CCPotions.EXPLOSION_COOLDOWN)) {
            return false;
        }

        float explosive = chestCavity.getOrganScore(CCOrganScores.EXPLOSIVE);
        if (explosive <= 0.0F) {
            return false;
        }

        float strength = MathHelper.sqrt(explosive);
        player.world.createExplosion(player, player.posX, player.posY, player.posZ, strength, false);
        ChestCavityHelper.destroyOrgansWithScore(chestCavity, CCOrganScores.EXPLOSIVE);
        if (player.isEntityAlive()) {
            player.addPotionEffect(new PotionEffect(CCPotions.EXPLOSION_COOLDOWN,
                    CCConfig.EXPLOSION_COOLDOWN, 0, false, false));
        }
        return true;
    }

    private static boolean activateIronRepair(EntityPlayerMP player, IChestCavity chestCavity) {
        float ironRepair = chestCavity.getOrganScore(IRON_REPAIR)
                - ChestCavityHelper.getChestCavityType(chestCavity).getDefaultOrganScore(IRON_REPAIR);
        if (ironRepair <= 0.0F
                || player.isPotionActive(CCPotions.IRON_REPAIR_COOLDOWN)
                || player.getHealth() >= player.getMaxHealth()) {
            return false;
        }

        ItemStack material = findIronRepairMaterial(player);
        if (material == null) {
            return false;
        }

        float healAmount = player.getMaxHealth() * CCConfig.IRON_REPAIR_PERCENT * getIronRepairMaterialValue(material);
        player.heal(healAmount);
        player.addPotionEffect(new PotionEffect(CCPotions.IRON_REPAIR_COOLDOWN,
                Math.max(1, Math.round(CCConfig.IRON_REPAIR_COOLDOWN / ironRepair)), 0, false, false));
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 0.75F, 1.0F);
        material.shrink(1);
        return true;
    }

    private static boolean activatePyromancy(EntityPlayerMP player, IChestCavity chestCavity) {
        float pyromancy = chestCavity.getOrganScore(PYROMANCY);
        if (pyromancy <= 0.0F || player.isPotionActive(CCPotions.PYROMANCY_COOLDOWN)) {
            return false;
        }

        int fireballs = Math.max(1, (int) pyromancy);
        Vec3d look = player.getLookVec().normalize();
        if (look == Vec3d.ZERO) {
            return false;
        }

        for (int i = 0; i < fireballs; i++) {
            chestCavity.enqueueProjectileAbility(PYROMANCY);
        }

        player.addExhaustion(fireballs * PYROMANCY_EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.PYROMANCY_COOLDOWN,
                CCConfig.PYROMANCY_COOLDOWN, 0, false, false));
        player.motionX -= look.x * PYROMANCY_RECOIL;
        player.motionY -= look.y * PYROMANCY_RECOIL;
        player.motionZ -= look.z * PYROMANCY_RECOIL;
        player.velocityChanged = true;
        return true;
    }

    private static boolean activateDragonBombs(EntityPlayerMP player, IChestCavity chestCavity) {
        float dragonBombs = chestCavity.getOrganScore(DRAGON_BOMBS);
        if (dragonBombs <= 0.0F || player.isPotionActive(CCPotions.DRAGON_BOMB_COOLDOWN)) {
            return false;
        }

        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }

        int bombs = Math.max(1, (int) dragonBombs);
        for (int i = 0; i < bombs; i++) {
            chestCavity.enqueueProjectileAbility(DRAGON_BOMBS);
        }

        player.addExhaustion(bombs * DRAGON_BOMB_EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.DRAGON_BOMB_COOLDOWN,
                CCConfig.DRAGON_BOMB_COOLDOWN, 0, false, false));
        applyRecoil(player, look, DRAGON_BOMB_RECOIL);
        return true;
    }

    private static boolean activateForcefulSpit(EntityPlayerMP player, IChestCavity chestCavity) {
        float forcefulSpit = chestCavity.getOrganScore(FORCEFUL_SPIT);
        if (forcefulSpit <= 0.0F || player.isPotionActive(CCPotions.FORCEFUL_SPIT_COOLDOWN)) {
            return false;
        }

        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }

        int projectiles = Math.max(1, (int) forcefulSpit);
        for (int i = 0; i < projectiles; i++) {
            chestCavity.enqueueProjectileAbility(FORCEFUL_SPIT);
        }

        player.addExhaustion(projectiles * FORCEFUL_SPIT_EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.FORCEFUL_SPIT_COOLDOWN,
                CCConfig.FORCEFUL_SPIT_COOLDOWN, 0, false, false));
        applyRecoil(player, look, FORCEFUL_SPIT_RECOIL);
        return true;
    }

    private static boolean activateGhastly(EntityPlayerMP player, IChestCavity chestCavity) {
        float ghastly = chestCavity.getOrganScore(GHASTLY);
        if (ghastly <= 0.0F || player.isPotionActive(CCPotions.GHASTLY_COOLDOWN)) {
            return false;
        }

        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }

        int fireballs = Math.max(1, (int) ghastly);
        for (int i = 0; i < fireballs; i++) {
            chestCavity.enqueueProjectileAbility(GHASTLY);
        }

        player.addExhaustion(fireballs * GHASTLY_EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.GHASTLY_COOLDOWN,
                CCConfig.GHASTLY_COOLDOWN, 0, false, false));
        applyRecoil(player, look, GHASTLY_RECOIL);
        return true;
    }

    private static boolean activateShulkerBullets(EntityPlayerMP player, IChestCavity chestCavity) {
        float shulkerBullets = chestCavity.getOrganScore(SHULKER_BULLETS);
        if (shulkerBullets <= 0.0F || player.isPotionActive(CCPotions.SHULKER_BULLET_COOLDOWN)) {
            return false;
        }

        EntityLivingBase target = findNearestTarget(player, CCConfig.SHULKER_BULLET_TARGETING_RANGE);
        if (target == null) {
            return false;
        }

        int bullets = Math.max(1, (int) shulkerBullets);
        for (int i = 0; i < bullets; i++) {
            chestCavity.enqueueProjectileAbility(SHULKER_BULLETS);
        }

        player.addExhaustion(bullets * SHULKER_BULLET_EXHAUSTION);
        player.addPotionEffect(new PotionEffect(CCPotions.SHULKER_BULLET_COOLDOWN,
                CCConfig.SHULKER_BULLET_COOLDOWN, 0, false, false));
        return true;
    }

    private static boolean activateSilk(EntityPlayerMP player, IChestCavity chestCavity) {
        float silk = chestCavity.getOrganScore(SILK);
        if (silk <= 0.0F || player.isPotionActive(CCPotions.SILK_COOLDOWN)) {
            return false;
        }
        if (player.getFoodStats().getFoodLevel() < 6) {
            return false;
        }

        int exhaustionCost = 0;
        float remainingSilk = silk;
        BlockPos pos = new BlockPos(player).offset(player.getHorizontalFacing().getOpposite());
        if (remainingSilk >= 2.0F && player.world.isAirBlock(pos)) {
            if (remainingSilk >= 3.0F && placeSilkBlock(player, Blocks.WOOL, pos)) {
                remainingSilk -= 3.0F;
                exhaustionCost += 16;
            } else if (placeSilkBlock(player, Blocks.WEB, pos)) {
                remainingSilk -= 2.0F;
                exhaustionCost += 8;
            }
        }

        int strings = 0;
        while (remainingSilk >= 1.0F) {
            remainingSilk -= 1.0F;
            strings++;
            exhaustionCost += 4;
        }
        if (strings > 0) {
            player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY + 0.5D, player.posZ,
                    new ItemStack(Items.STRING, strings)));
        }
        if (exhaustionCost <= 0) {
            return false;
        }

        player.addExhaustion(exhaustionCost);
        player.addPotionEffect(new PotionEffect(CCPotions.SILK_COOLDOWN,
                CCConfig.SILK_COOLDOWN, 0, false, false));
        return true;
    }

    private static boolean spawnPyromancyFireball(EntityPlayerMP player) {
        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }
        EntitySmallFireball fireball = new EntitySmallFireball(player.world, player, look.x, look.y, look.z);
        setProjectileStart(player, fireball, look);
        return player.world.spawnEntity(fireball);
    }

    private static boolean spawnDragonBomb(EntityPlayerMP player) {
        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }
        EntityDragonFireball fireball = new EntityDragonFireball(player.world, player, look.x, look.y, look.z);
        setProjectileStart(player, fireball, look);
        return player.world.spawnEntity(fireball);
    }

    private static boolean spawnForcefulSpit(EntityPlayerMP player) {
        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }
        EntityForcefulSpit spit = new EntityForcefulSpit(player.world, player);
        spit.setPosition(player.posX + look.x, player.posY + player.getEyeHeight() - 0.1D, player.posZ + look.z);
        spit.shoot(look.x, look.y, look.z, FORCEFUL_SPIT_VELOCITY, 0.0F);
        return player.world.spawnEntity(spit);
    }

    private static boolean spawnGhastlyFireball(EntityPlayerMP player) {
        Vec3d look = getNormalizedLook(player);
        if (look == null) {
            return false;
        }
        EntityLargeFireball fireball = new EntityLargeFireball(player.world, player, look.x, look.y, look.z);
        fireball.explosionPower = 1;
        setProjectileStart(player, fireball, look);
        return player.world.spawnEntity(fireball);
    }

    private static boolean spawnShulkerBullet(EntityPlayerMP player) {
        EntityLivingBase target = findNearestTarget(player, CCConfig.SHULKER_BULLET_TARGETING_RANGE);
        if (target == null) {
            return false;
        }
        EntityShulkerBullet bullet = new EntityShulkerBullet(player.world, player, target, EnumFacing.Axis.Y);
        return player.world.spawnEntity(bullet);
    }

    private static boolean activateScriptAbility(EntityPlayerMP player, IChestCavity chestCavity, ResourceLocation abilityId) {
        AbilityDefinition definition = AbilityRegistry.get(abilityId);
        if (definition == null) {
            ChestCavityLegacy.LOGGER.debug("Ignoring unknown active organ ability {}.", abilityId);
            return false;
        }
        Object callback = definition.getActivateCallback();
        if (!(callback instanceof AbilityCallbacks.OnActivate)) {
            ChestCavityLegacy.LOGGER.debug("Ignoring script organ ability {} because no activate callback is registered.", abilityId);
            return false;
        }
        return ((AbilityCallbacks.OnActivate) callback).handle(
                CraftTweakerMC.getIPlayer(player),
                abilityId.toString(),
                chestCavity.getOrganScore(abilityId));
    }

    private static Vec3d getNormalizedLook(EntityPlayerMP player) {
        Vec3d look = player.getLookVec();
        if (look == null || look.lengthSquared() < 1.0E-4D) {
            return null;
        }
        return look.normalize();
    }

    private static void setProjectileStart(EntityPlayerMP player, Entity projectile, Vec3d look) {
        projectile.setPosition(player.posX + look.x,
                player.posY + player.getEyeHeight() - 0.1D,
                player.posZ + look.z);
    }

    private static void applyRecoil(EntityPlayerMP player, Vec3d look, double recoil) {
        player.motionX -= look.x * recoil;
        player.motionY -= look.y * recoil;
        player.motionZ -= look.z * recoil;
        player.velocityChanged = true;
    }

    private static EntityLivingBase findNearestTarget(EntityPlayerMP player, double range) {
        AxisAlignedBB box = player.getEntityBoundingBox().grow(range);
        List<EntityLivingBase> targets = player.world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        EntityLivingBase nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (EntityLivingBase target : targets) {
            if (target == player || !target.isEntityAlive()) {
                continue;
            }
            if (target instanceof EntityPlayer && ((EntityPlayer) target).isSpectator()) {
                continue;
            }

            double distance = player.getDistanceSq(target);
            if (distance < nearestDistance) {
                nearest = target;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static boolean placeSilkBlock(EntityPlayerMP player, Block block, BlockPos pos) {
        World world = player.world;
        ItemStack blockStack = new ItemStack(Item.getItemFromBlock(block));
        if (!player.canPlayerEdit(pos, EnumFacing.UP, blockStack)) {
            return false;
        }
        if (!world.mayPlace(block, pos, false, EnumFacing.UP, player)) {
            return false;
        }
        return world.setBlockState(pos, block.getDefaultState(), 3);
    }

    private static FuelStack findFuel(EntityPlayerMP player) {
        FuelStack mainHand = findFuel(player, EnumHand.MAIN_HAND);
        return mainHand == null ? findFuel(player, EnumHand.OFF_HAND) : mainHand;
    }

    private static FuelStack findFuel(EntityPlayerMP player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return null;
        }
        int burnTime = TileEntityFurnace.getItemBurnTime(stack);
        return burnTime > 0 ? new FuelStack(hand, stack, burnTime) : null;
    }

    private static void consumeFuel(EntityPlayerMP player, FuelStack fuel) {
        ItemStack stack = fuel.stack;
        if (stack.getCount() == 1 && stack.getItem().hasContainerItem(stack)) {
            player.setHeldItem(fuel.hand, stack.getItem().getContainerItem(stack));
            return;
        }
        stack.shrink(1);
    }

    private static ItemStack findIronRepairMaterial(EntityPlayerMP player) {
        ItemStack mainHand = findIronRepairMaterial(player, EnumHand.MAIN_HAND);
        return mainHand == null ? findIronRepairMaterial(player, EnumHand.OFF_HAND) : mainHand;
    }

    private static ItemStack findIronRepairMaterial(EntityPlayerMP player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return null;
        }

        return isIronRepairMaterial(stack) ? stack : null;
    }

    private static boolean isIronRepairMaterial(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.IRON_NUGGET || item == Items.IRON_INGOT || item == Item.getItemFromBlock(Blocks.IRON_BLOCK);
    }

    private static float getIronRepairMaterialValue(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.IRON_NUGGET) {
            return 1.0F / 9.0F;
        }
        if (item == Item.getItemFromBlock(Blocks.IRON_BLOCK)) {
            return 9.0F;
        }
        return 1.0F;
    }

    private static final class FuelStack {
        private final EnumHand hand;
        private final ItemStack stack;
        private final int burnTime;

        private FuelStack(EnumHand hand, ItemStack stack, int burnTime) {
            this.hand = hand;
            this.stack = stack;
            this.burnTime = burnTime;
        }
    }

}
