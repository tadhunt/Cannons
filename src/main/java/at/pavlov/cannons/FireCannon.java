package at.pavlov.cannons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import at.pavlov.cannons.Enum.*;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.CannonUseEvent;
import at.pavlov.cannons.utils.CannonsUtil;
import at.pavlov.cannons.utils.DelayedTask;
import at.pavlov.cannons.utils.FireTaskWrapper;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.cannon.DesignStorage;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;

public class FireCannon {

    private final Config config;
    private final Cannons plugin;




    public FireCannon(Cannons plugin, Config config)
    {
        this.plugin = plugin;
        this.config = config;
    }


    /**
     * checks all condition but does not fire the cannon
     * @param cannon cannon which is fired
     * @param player operator of the cannon
     * @return message for the player
     */
    private MessageEnum getPrepareFireMessage(Cannon cannon, Player player)
    {
        CannonDesign design = cannon.getCannonDesign();
        if (design == null) return null;
        //if the player is not the owner of this gun
        if (player != null && cannon.getOwner() != null && !cannon.getOwner().equals(player.getUniqueId())  && design.isAccessForOwnerOnly())
            return MessageEnum.ErrorNotTheOwner;
        //Loading in progress
        if (cannon.isLoading() && !design.isFireAfterLoading())
            return MessageEnum.ErrorLoadingInProgress;
        // is the cannon already cleaned?
        if (!cannon.isClean())
            return MessageEnum.ErrorNotCleaned;
        //check if there is some gunpowder in the barrel
        if (!cannon.isGunpowderLoaded())
            return MessageEnum.ErrorNoGunpowder;//is there a projectile
        // is there a projectile in the cannon?
        if (!cannon.isLoaded() && !design.isFireAfterLoading())
            return MessageEnum.ErrorNoProjectile;
        // after cleaning the projectile needs to pushed in the barrel
        if (!cannon.isProjectilePushed())
            return MessageEnum.ErrorNotPushed;
        //Firing in progress
        if (cannon.isFiring())
            return MessageEnum.ErrorFiringInProgress;
        // fee not paid
        if (plugin.getEconomy() != null && !cannon.isPaid())
            return MessageEnum.ErrorNotPaid;
        //Barrel too hot
        if(cannon.barrelTooHot())
            return MessageEnum.ErrorBarrelTooHot;
        //automatic temperature control, prevents overheating of the cannon
        if(design.isAutomaticTemperatureControl() && cannon.isOverheatedAfterFiring())
            return MessageEnum.ErrorBarrelTooHot;
        if (player!= null)
        {
            //if the player has permission to fire
            if (!player.hasPermission(design.getPermissionFire()))
                return MessageEnum.PermissionErrorFire;

            //check if the player has the permission for this projectile
//            Projectile projectile = cannon.getLoadedProjectile();
//            if(projectile != null && !projectile.hasPermission(player))
//                return MessageEnum.PermissionErrorProjectile;
            //check for flint and steel
            if (design.isFiringItemRequired() && !config.getToolFiring().equalsFuzzy(player.getInventory().getItemInMainHand()) )
                return MessageEnum.ErrorNoFlintAndSteel;
        }
        //everything fine fire the damn cannon
        return MessageEnum.CannonFire;
    }

    /**
     * checks if all preconditions for firing are fulfilled and fires the cannon
     * Default fire event for players
     * @param cannon - cannon to fire
     * @param action - how has the player/plugin interacted with the cannon
     * @return - message for the player
     */
    public MessageEnum redstoneFiring(Cannon cannon, InteractAction action)
    {
        CannonDesign design = cannon.getCannonDesign();
        return this.fire(cannon, null, cannon.getCannonDesign().isAutoreloadRedstone(), !design.isAmmoInfiniteForRedstone(), action);
    }

    /**
     * checks if all preconditions for firing are fulfilled and fires the cannon
     * Default fire event for players
     * @param cannon - cannon to fire
     * @param player - operator of the cannon
     * @param action - how has the player/plugin interacted with the cannon
     * @return - message for the player
     */
    public MessageEnum playerFiring(Cannon cannon, Player player, InteractAction action)
    {
        CannonDesign design = cannon.getCannonDesign();
        boolean autoreload = player.isSneaking() && player.hasPermission(design.getPermissionAutoreload());

        //todo add firing of multiple cannons
        if (design.isLinkCannonsEnabled()) {
            int d = design.getLinkCannonsDistance()*2;
            for (Cannon fcannon : CannonManager.getCannonsInBox(cannon.getLocation(), d, d, d)) {
                plugin.logDebug(fcannon.getCannonName() + " is cannon operator: " + fcannon.isCannonOperator(player));
                if (fcannon.isCannonOperator(player) && fcannon.canAimYaw(player.getEyeLocation().getYaw()) &&/*fcannon.isAimingFinished() &&*/ fcannon.getCannonDesign().equals(cannon.getCannonDesign()) &&  (!cannon.getCannonDesign().isAccessForOwnerOnly() || fcannon.getOwner() == player.getUniqueId()))
                    this.fire(fcannon, player.getUniqueId(), autoreload, !design.isAmmoInfiniteForPlayer(), action);
            }
        }

        return this.fire(cannon, player.getUniqueId(), autoreload, !design.isAmmoInfiniteForPlayer(), action);
    }

    /**
     * checks if all preconditions for firing are fulfilled and fires the cannon
     * Default fire event for players
     * @param cannon - cannon to fire
     * @return - message for the player
     */
    public MessageEnum sentryFiring(Cannon cannon)
    {
        CannonDesign design = cannon.getCannonDesign();

        return this.fire(cannon, null, true, !design.isAmmoInfiniteForPlayer(), InteractAction.fireSentry);
    }

    /**
     * checks if all preconditions for firing are fulfilled and fires the cannon
     * @param cannon the cannon which is fired
     * @param playerUid player operating the cannon
     * @param autoload the cannon will autoreload before firing
     * @param consumesAmmo if true ammo will be removed from chest inventories
     * @return message for the player
     */
    public MessageEnum fire(Cannon cannon, UUID playerUid, boolean autoload, boolean consumesAmmo, InteractAction action)
    {
        plugin.logDebug("fire cannon");

        //set some valid shooter is none is given
        if (playerUid == null) {
            playerUid = cannon.getOwner();
        }

        Player player = null;
        if (playerUid != null) {
            player = Bukkit.getPlayer(playerUid);
        }

        //fire event
        CannonUseEvent useEvent = new CannonUseEvent(cannon, playerUid, action);
        Bukkit.getServer().getPluginManager().callEvent(useEvent);

        if (useEvent.isCancelled())
            return null;

        CannonDesign design = cannon.getCannonDesign();

        //if there is no gunpowder needed we set it to the maximum
        if (!design.isGunpowderNeeded() && cannon.getLoadedGunpowder() == 0)
            cannon.setLoadedGunpowder(design.getMaxLoadableGunpowderNormal());


        //no charge try some autoreload from chests
        if (!cannon.isLoaded() && !cannon.isLoading() && autoload)
        {
            MessageEnum messageEnum = cannon.reloadFromChests(playerUid, consumesAmmo);
            //try to load some projectiles
            if (messageEnum.isError())
            {
                //there is not enough gunpowder or no projectile in the chest
                plugin.logDebug("Can't reload cannon, because there is no valid charge in the chests");
                CannonsUtil.playErrorSound(cannon.getMuzzle());
                return messageEnum;
            }
            else
            {
                //everything went fine - next click on torch will fire the cannon
                //if fire after reloading is active, it will fire automatically. This can be a problem for the impact predictor
                if (!design.isFireAfterLoading())
                    return MessageEnum.loadProjectile;
            }
        }

        //check for all permissions
        MessageEnum message = getPrepareFireMessage(cannon, player);

        if (message != null && message.isError())
            CannonsUtil.playErrorSound(cannon.getMuzzle());

        //return if there are permission missing
        if (message != MessageEnum.CannonFire)
            return message;

        CannonFireEvent fireEvent = new CannonFireEvent(cannon, playerUid);
        Bukkit.getServer().getPluginManager().callEvent(fireEvent);

        if (fireEvent.isCancelled())
            return null;

        Projectile projectile = cannon.getLoadedProjectile();
        //reset after firing
        cannon.setLastFired(System.currentTimeMillis());
        //this cannon is now firing
        cannon.setFiring();
        //store spread of cannon operator
        cannon.setLastPlayerSpreadMultiplier(player);

        //Set up smoke effects on the torch
        for (Location torchLoc : design.getFiringIndicator(cannon))
        {
            torchLoc.setX(torchLoc.getX() + 0.5);
            torchLoc.setY(torchLoc.getY() + 1);
            torchLoc.setZ(torchLoc.getZ() + 0.5);
            torchLoc.getWorld().playEffect(torchLoc, Effect.SMOKE, BlockFace.UP);
            //torchLoc.getWorld().playSound(torchLoc, Sound.FUSE , 10f, 1f);
            CannonsUtil.playSound(torchLoc, design.getSoundIgnite());
        }

        final ProjectileCause projectileCause;
        switch (action){
            case fireRightClickTigger:{
                projectileCause = ProjectileCause.PlayerFired;
                break;
            }
            case fireAutoaim:{
                projectileCause = ProjectileCause.PlayerFired;
                break;
            }
            case fireRedstoneTrigger:{
                projectileCause = ProjectileCause.PlayerFired;
                break;
            }
            case fireAfterLoading:{
                projectileCause = ProjectileCause.PlayerFired;
                break;
            }
            case fireRedstone:{
                projectileCause = ProjectileCause.RedstoneFired;
                break;
            }
            case fireSentry:{
                projectileCause = ProjectileCause.SentryFired;
                break;
            }
            default:{
                projectileCause = ProjectileCause.UnknownFired;
                break;
            }

        }

        //set up delayed task with automatic firing. Several bullets with time delay for one loaded projectile
        for(int i=0; i < projectile.getAutomaticFiringMagazineSize(); i++)
        {
            //charge is only removed in the last round fired
            boolean lastRound = i==(projectile.getAutomaticFiringMagazineSize()-1);
            double randomess = 1. + design.getFuseBurnTimeRandomness() * new Random().nextDouble();
            Long delayTime = (long) (randomess * design.getFuseBurnTime() * 20.0 + i*projectile.getAutomaticFiringDelay()*20.0);
            FireTaskWrapper fireTask = new FireTaskWrapper(cannon, playerUid, lastRound, projectileCause);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedTask(fireTask)
            {
                public void run(Object object)
                {
                    FireTaskWrapper fireTask = (FireTaskWrapper) object;
                    fireTask(fireTask.getCannon(), fireTask.getPlayer(), fireTask.isRemoveCharge(), projectileCause);
                }
            }, delayTime);
        }

        return message;
    }


    /**
     * fires a cannon and removes the charge from the player
     * @param cannon - the fired cannon
     * @param shooter - the player firing the cannon
     * @param removeCharge - if the charge is removed after firing
     * @param projectileCause - how the projectile was fired (by a player, redstone, or sentry)
     */
    private void fireTask(Cannon cannon, UUID shooter, boolean removeCharge, ProjectileCause projectileCause)
    {
        CannonDesign design = cannon.getCannonDesign();
        Projectile projectile = cannon.getLoadedProjectile();

        //the player might be null if not online
        Player onlinePlayer = null;
        if (shooter != null) {
            onlinePlayer = Bukkit.getPlayer(shooter);
        }

        // no projectile no cannon firing
        if (projectile == null) {
            return;
        }
        // no gunpowder no cannon firing
        if (cannon.getLoadedGunpowder() <= 0) {
            return;
        }

        //increased fired cannonballs
        cannon.incrementFiredCannonballs();

        //get firing location
        Location firingLoc = design.getMuzzle(cannon);
        World world = cannon.getWorldBukkit();

        //Muzzle flash + effects
        if (projectile.getProjectileEntity().equals(EntityType.ARROW))
            world.playEffect(firingLoc, Effect.BOW_FIRE, 10);
        MuzzleFire(cannon);
        world.playEffect(firingLoc, Effect.SMOKE, cannon.getCannonDirection());

        //increase heat of the cannon
        if (design.isHeatManagementEnabled())
            cannon.setTemperature(cannon.getTemperature()+design.getHeatIncreasePerGunpowder()/projectile.getAutomaticFiringMagazineSize()*cannon.getLoadedGunpowder());
        //automatic cool down
        if (design.isAutomaticCooling())
            cannon.automaticCoolingFromChest();

        //for each bullet, but at least once
        for (int i=0; i < Math.max(projectile.getNumberOfBullets(), 1); i++)
        {
            org.bukkit.projectiles.ProjectileSource source = null;
            Location playerLoc = null;
            if (onlinePlayer != null) {
                source = onlinePlayer;
                playerLoc = onlinePlayer.getLocation();
            }

            Vector vect = cannon.getFiringVector(true, true);

            org.bukkit.entity.Projectile projectileEntity = plugin.getProjectileManager().spawnProjectile(projectile, shooter, source, playerLoc, firingLoc, vect, cannon.getUID(), projectileCause);

            if (i == 0 && projectile.hasProperty(ProjectileProperties.SHOOTER_AS_PASSENGER) && onlinePlayer != null)
                projectileEntity.setPassenger(onlinePlayer);

            //confuse all entity which wear no helmets due to the blast of the cannon
            List<Entity> living = projectileEntity.getNearbyEntities(8, 8, 8);
            //do only once
            if (i == 0) {
                confuseShooter(living, firingLoc, design.getBlastConfusion());
            }
        }

        //check if the temperature exceeds the limit and overloading
        if (cannon.checkHeatManagement() || cannon.isExplodedDueOverloading())
        {
            plugin.getCannonManager().removeCannon(cannon, true, true, BreakCause.Overheating);
            return;
        }

        //reset after firing
        cannon.setLastFired(System.currentTimeMillis());
        cannon.setSoot(cannon.getSoot() + design.getSootPerGunpowder()*cannon.getLoadedGunpowder());

        if (removeCharge)
        {
            cannon.setProjectilePushed(design.getProjectilePushing());

            plugin.logDebug("fire event complete, charge removed from the cannon");
            //removes the gunpowder and projectile loaded in the cannon if not set otherwise
            if (design.isRemoveChargeAfterFiring())
                cannon.removeCharge();
        }
    }

    /**
     * creates a imitated explosion made of blocks which is transmitted to player in a give distance
     * @param c operated cannon
     */
    public void MuzzleFire(Cannon c)
    {
        double minDist = config.getImitatedBlockMinimumDistance();
        double maxDist = config.getImitatedBlockMaximumDistance();
        float maxVol = config.getImitatedSoundMaximumVolume();
        Location loc = c.getMuzzle();

        //simple particle effects for close distance
        //loc.getWorld().createExplosion(loc, 0F, false);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
        //fake blocks effects for far distance
        if (config.isImitatedFiringEffectEnabled()) {
            int maxSoundDist = config.getImitatedSoundMaximumDistance();
            CannonsUtil.imitateSound(loc, c.getCannonDesign().getSoundFiring(), maxSoundDist, maxVol);

            List<Player> players = new ArrayList<Player>();
            for (Player p : loc.getWorld().getPlayers()) {
                Location pl = p.getLocation();
                double distance = pl.distance(loc);

                if (distance >= minDist && distance <= maxDist) {
                    players.add(p);
                }
            }
            imitateSmoke(c, players);
        }
    }


    /**
     * creates a sphere of fake block and sends it to the given player
     * @param cannon - cannon in usage
     */
    public void imitateSmoke(Cannon cannon, List<Player> players)
    {
        if (!config.isImitatedFiringEffectEnabled())
            return;

        Vector aimingVector = cannon.getAimingVector().clone();
        Location loc = cannon.getMuzzle();

        double duration = config.getImitatedFiringTime();

        for(Player name : players)
        {
            //make smoke and fire effects for large distance
            plugin.getFakeBlockHandler().imitateLine(name, loc, aimingVector, 0, 1, config.getImitatedFireMaterial(), FakeBlockType.MUZZLE_FIRE, duration);
            plugin.getFakeBlockHandler().imitatedSphere(name, loc.clone().add(aimingVector.clone().normalize()), 2, config.getImitatedSmokeMaterial(), FakeBlockType.MUZZLE_FIRE, duration);
        }
    }



    /**
     * confuses an entity to simulate the blast of a cannon
     * @param living entity to confuse
     * @param firingLoc distance to the muzzle
     * @param confuseTime how long the enitity is confused in seconds
     */
    private void confuseShooter(List<Entity> living, Location firingLoc, double confuseTime)
    {
        //confuse shooter if he wears no helmet (only for one projectile and if its configured)
        if (confuseTime > 0)
        {
            for (Entity next : living)
            {
                boolean harmEntity = false;
                if (next instanceof Player)
                {

                    Player player = (Player) next;
                    if ( player.isOnline() && !CheckHelmet(player) && player.getGameMode() != GameMode.CREATIVE  )
                    {
                        //if player has no helmet and is not in creative and there are confusion effects - harm him
                        harmEntity = true;
                    }
                }
                else if (next instanceof LivingEntity)
                {
                    //damage entity
                    harmEntity = true;
                }
                if (harmEntity)
                {
                    //damage living entities and unprotected players
                    LivingEntity livingEntity = (LivingEntity) next;
                    if (livingEntity.getLocation().distance(firingLoc) < 5.0)
                        livingEntity.damage(1);
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,(int) confuseTime*20, 0));

                }
            }
        }
    }




    //############## CheckHelmet   ################################
    private boolean CheckHelmet(Player player)
    {
        ItemStack helmet = player.getInventory().getHelmet();
        return helmet != null;
    }
}
