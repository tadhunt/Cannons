package at.pavlov.cannons.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.container.MaterialHolder;
import org.bukkit.*;
import org.bukkit.Note.Tone;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.material.Torch;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;


public class CannonsUtil
{
	public static boolean hasIdData(Block block, int id, int data)
	{
		if (block.getTypeId() == id && block.getData() == data) { return true; }
		return false;
	}

	// ################# CheckAttachedButton ###########################
	public static boolean CheckAttachedButton(Block block, BlockFace face)
	{
		Block attachedBlock = block.getRelative(face);
		if (attachedBlock.getType() == Material.STONE_BUTTON)
		{
			Button button = (Button) attachedBlock.getState().getData();
			if (button.getAttachedFace() != null)
			{
				if (attachedBlock.getRelative(button.getAttachedFace()).equals(block)) { return true; }
			}
			// attached face not available
			else
			{
				return true;
			}
		}
		return false;
	}

	// ################# CheckAttachedTorch ###########################
	@Deprecated
    public static boolean CheckAttachedTorch(Block block)
	{
		Block attachedBlock = block.getRelative(BlockFace.UP);
		if (attachedBlock.getType() == Material.TORCH)
		{
			Torch torch = (Torch) attachedBlock.getState().getData();
			if (torch.getAttachedFace() != null)
			{
				if (attachedBlock.getRelative(torch.getAttachedFace()).equals(block)) { return true; }
			}
			// attached face not available
			else
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * changes the extension of the a string (e.g. classic.yml to
	 * classic.schematic)
	 * 
	 * @param originalName
	 * @param newExtension
	 * @return
	 */
	public static String changeExtension(String originalName, String newExtension)
	{
		int lastDot = originalName.lastIndexOf(".");
		if (lastDot != -1)
		{
			return originalName.substring(0, lastDot) + newExtension;
		}
		else
		{
			return originalName + newExtension;
		}
	}
	
	/**
	 * removes the extrions of a filename like classic.yml
	 * @param str
	 * @return
	 */
	public static String removeExtension(String str)
	{
		return str.substring(0, str.lastIndexOf('.'));
	}

	/**
	 * return true if the folder is empty
	 * @param folderPath
	 * @return
	 */
	public static boolean isFolderEmpty(String folderPath)
	{
		File file = new File(folderPath);
		if (file.isDirectory())
		{
			if (file.list().length > 0)
			{
				//folder is not empty
				return false;
			}
		}
		return true;
	}
	
	/**
	 * copies a file form the .jar to the disk
	 * @param in
	 * @param file
	 */
	public static void copyFile(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	/**
	 * rotates the direction by 90°
	 * @param face
	 * @return
	 */
	public static BlockFace roatateFace(BlockFace face)
	{
		if (face.equals(BlockFace.NORTH)) return BlockFace.EAST;
		if (face.equals(BlockFace.EAST)) return BlockFace.SOUTH;
		if (face.equals(BlockFace.SOUTH)) return BlockFace.WEST;
		if (face.equals(BlockFace.WEST)) return BlockFace.NORTH;
		return BlockFace.UP;
	}

    /**
     * rotates the direction by -90°
     * @param face
     * @return
     */
    public static BlockFace roatateFaceOpposite(BlockFace face)
    {
        if (face.equals(BlockFace.NORTH)) return BlockFace.WEST;
        if (face.equals(BlockFace.EAST)) return BlockFace.NORTH;
        if (face.equals(BlockFace.SOUTH)) return BlockFace.EAST;
        if (face.equals(BlockFace.WEST)) return BlockFace.SOUTH;
        return BlockFace.UP;
    }
	
	/**
	 * returns a new Itemstack
	 * @param id
	 * @param data
	 * @param amount
	 * @return
	 */
	public static ItemStack newItemStack(int id, int data, int amount)
	{
		return new ItemStack(id, amount, (short) data);
	}
	
	/**
	 * returns a list of MaterialHolder
	 * @param stringList
	 * @return
	 */
	public static List<MaterialHolder> toMaterialHolderList(List<String> stringList)
	{
		List<MaterialHolder> materialList = new ArrayList<MaterialHolder>();
		
		for (String str : stringList)
		{
			MaterialHolder material = new MaterialHolder(str); 
			//if id == -1 the str was invalid
			if (material.getId() >= 0)
				materialList.add(material);
		}
		
		return materialList;
	}
	
	
	/**
	 * get all block next to this block (UP, DOWN, SOUT, WEST, NORTH, EAST)
	 * @param block
	 * @return
	 */
	public static ArrayList<Block> SurroundingBlocks(Block block)
	{
		ArrayList<Block> Blocks = new ArrayList<Block>();

		Blocks.add(block.getRelative(BlockFace.UP));
		Blocks.add(block.getRelative(BlockFace.DOWN));
		Blocks.add(block.getRelative(BlockFace.SOUTH));
		Blocks.add(block.getRelative(BlockFace.WEST));
		Blocks.add(block.getRelative(BlockFace.NORTH));
		Blocks.add(block.getRelative(BlockFace.EAST));
		return Blocks;
	}

	/**
	 * get all block in the horizontal plane next to this block (SOUTH, WEST, NORTH, EAST)
	 * @param block
	 * @return
	 */
	public static ArrayList<Block> HorizontalSurroundingBlocks(Block block)
	{
		ArrayList<Block> Blocks = new ArrayList<Block>();

		Blocks.add(block.getRelative(BlockFace.SOUTH));
		Blocks.add(block.getRelative(BlockFace.WEST));
		Blocks.add(block.getRelative(BlockFace.NORTH));
		Blocks.add(block.getRelative(BlockFace.EAST));
		return Blocks;
	}
	
	
	/**
	 * returns the yaw of a given blockface
	 * @param direction
	 * @return
	 */
    public static int directionToYaw(BlockFace direction) {
        switch (direction) {
            case NORTH: return 180;
            case EAST: return 270;
            case SOUTH: return 0;
            case WEST: return 90;
            case NORTH_EAST: return 135;
            case NORTH_WEST: return 45;
            case SOUTH_EAST: return -135;
            case SOUTH_WEST: return -45;
            default: return 0;
        }
    }

    /**
     * Armor would reduce the damage the player receives
     * @param player - the affected player
     * @return - how much the damage is reduced by the armor
     */
    public static double getArmorDamageReduced(Player player)
    {
        // http://www.minecraftwiki.net/wiki/Armor#Armor_enchantment_effect_calculation

        if (player == null) return 0.0;

        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        if (inv == null) return 0.0;

        ItemStack boots = inv.getBoots();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack pants = inv.getLeggings();
        double red = 0.0;
        if (helmet != null)
        {
            if(helmet.getType() == Material.LEATHER_HELMET)red = red + 0.04;
            else if(helmet.getType() == Material.GOLD_HELMET)red = red + 0.08;
            else if(helmet.getType() == Material.CHAINMAIL_HELMET)red = red + 0.08;
            else if(helmet.getType() == Material.IRON_HELMET)red = red + 0.08;
            else if(helmet.getType() == Material.DIAMOND_HELMET)red = red + 0.12;
        }
        //
        if (boots != null)
        {
            if(boots.getType() == Material.LEATHER_BOOTS)red = red + 0.04;
            else if(boots.getType() == Material.GOLD_BOOTS)red = red + 0.04;
            else if(boots.getType() == Material.CHAINMAIL_BOOTS)red = red + 0.04;
            else if(boots.getType() == Material.IRON_BOOTS)red = red + 0.08;
            else if(boots.getType() == Material.DIAMOND_BOOTS)red = red + 0.12;
        }
        //
        if (pants != null)
        {
            if(pants.getType() == Material.LEATHER_LEGGINGS)red = red + 0.08;
            else if(pants.getType() == Material.GOLD_LEGGINGS)red = red + 0.12;
            else if(pants.getType() == Material.CHAINMAIL_LEGGINGS)red = red + 0.16;
            else if(pants.getType() == Material.IRON_LEGGINGS)red = red + 0.20;
            else if(pants.getType() == Material.DIAMOND_LEGGINGS)red = red + 0.24;
        }
        //
        if (chest != null)
        {
            if(chest.getType() == Material.LEATHER_CHESTPLATE)red = red + 0.12;
            else if(chest.getType() == Material.GOLD_CHESTPLATE)red = red + 0.20;
            else if(chest.getType() == Material.CHAINMAIL_CHESTPLATE)red = red + 0.20;
            else if(chest.getType() == Material.IRON_CHESTPLATE)red = red + 0.24;
            else if(chest.getType() == Material.DIAMOND_CHESTPLATE)red = red + 0.32;
        }
        return red;
    }

    /**
     * returns the total blast protection of the player
     * @param player - the affected player
     */
    public static double getBlastProtection(Player player)
    {
        //http://www.minecraftwiki.net/wiki/Armor#Armor_enchantment_effect_calculation

        if (player == null) return 0.0;

        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        if (inv == null) return 0.0;

        ItemStack boots = inv.getBoots();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack pants = inv.getLeggings();

        int lvl = 0;
        double reduction = 0.0;

        if (boots != null)
        {
            lvl = boots.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 1.5 / 3);
            lvl = boots.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 0.75 / 3);
        }
        if (helmet != null)
        {
            lvl = helmet.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 1.5 / 3);
            lvl = helmet.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 0.75 / 3);
        }
        if (chest != null)
        {
            lvl = chest.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 1.5 / 3);
            lvl = chest.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 0.75 / 3);
        }
        if (pants != null)
        {
            lvl = pants.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 1.5 / 3);
            lvl = pants.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 0.75 / 3);
        }
        //cap it to 25
        if (reduction > 25) reduction = 25;

        //give it some randomness
        Random r = new Random();
        reduction = reduction * (r.nextFloat()/2 + 0.5);

        //cap it to 20
        if (reduction > 20) reduction = 20;

        //1 point is 4%
        return reduction*4/100;
    }

    /**
     * returns the total projectile protection of the player
     * @param player - the affected player
     */
    public static double getProjectileProtection(Player player)
    {
        //http://www.minecraftwiki.net/wiki/Armor#Armor_enchantment_effect_calculation

        if (player == null) return 0.0;

        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        if (inv == null) return 0.0;
        ItemStack boots = inv.getBoots();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack pants = inv.getLeggings();

        int lvl = 1;
        double reduction = 0;

        if (boots != null)
        {
            lvl = boots.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 1.5 / 3);
            lvl = boots.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 0.75 / 3);
        }
        if (helmet != null)
        {
            lvl = helmet.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 1.5 / 3);
            lvl = helmet.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 0.75 / 3);
        }
        if (chest != null)
        {
            lvl = chest.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 1.5 / 3);
            lvl = chest.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 0.75 / 3);
        }
        if (pants != null)
        {
            lvl = pants.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 1.5 / 3);
            lvl = pants.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (lvl > 0)
                reduction += Math.floor((6 + lvl * lvl) * 0.75 / 3);
        }
        //cap it to 25
        if (reduction > 25) reduction = 25;

        //give it some randomness
        Random r = new Random();
        reduction = reduction * (r.nextFloat()/2 + 0.5);

        //cap it to 20
        if (reduction > 20) reduction = 20;

        //1 point is 4%
        return reduction*4/100;
    }

    /**
     * reduces the durability of the player's armor
     * @param player - the affected player
     */
    public static void reduceArmorDurability(Player player)
    {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        if (inv == null) return;

        Random r = new Random();

        for(ItemStack item : inv.getArmorContents())
        {
            if(item != null)
            {
                int lvl = item.getEnchantmentLevel(Enchantment.DURABILITY);
                //chance of breaking in 0-1
                double breakingChance = 0.6+0.4/(lvl+1);

                if (r.nextDouble() < breakingChance)
                {
                    short newDurabiltiy = (short) (item.getDurability() + 1);
                    item.setDurability(newDurabiltiy);
                }
            }
        }
    }

    /**
     * returns a random block face
     * @return - random BlockFace
     */
    public static BlockFace randomBlockFaceNoDown()
    {
        Random r = new Random();
        switch (r.nextInt(5))
        {
            case 0:
                return BlockFace.UP;
            case 1:
                return BlockFace.EAST;
            case 2:
                return BlockFace.SOUTH;
            case 3:
                return BlockFace.WEST;
            case 4:
                return BlockFace.NORTH;
            default:
                return BlockFace.SELF;
        }
    }

    /**
     * adds a little bit random to the location so the effects don't spawn at the same point.
     * @return - randomized location
     */
    public static Location randomLocationOrthogonal(Location loc, BlockFace face)
    {
        Random r = new Random();

        //this is the direction we want to avoid
        Vector vect = new Vector(face.getModX(),face.getModY(),face.getModZ());
        //orthogonal vector - somehow
        vect = vect.multiply(vect).subtract(new Vector(1,1,1));

        loc.setX(loc.getX()+vect.getX()*(r.nextDouble()-0.5));
        loc.setY(loc.getY()+vect.getY()*(r.nextDouble()-0.5));
        loc.setZ(loc.getZ()+vect.getZ()*(r.nextDouble()-0.5));

        return loc;
    }



    /**
     * creates a imitated explosion sound
     * @param loc location of the explosion
     * @param sound sound
     * @param maxDist maximum distance
     */
    public static void imitateSound(Location loc, Sound sound, int maxDist, float pitch)
    {
        //https://forums.bukkit.org/threads/playsound-parameters-volume-and-pitch.151517/
        World w = loc.getWorld();
        w.playSound(loc,sound, 15F, pitch);

        for(Player p : w.getPlayers())
        {
        	Location pl = p.getLocation();
            //readable code
            Vector v = loc.clone().subtract(pl).toVector();
            double d = v.length();
            if(40.0<=d&&d<=maxDist)
            {

                //float volume = 2.1f-(float)(d/maxDist);
                float newPitch = pitch/(float) Math.sqrt(d);
                //p.playSound(p.getEyeLocation().add(v.normalize().multiply(16)), sound, volume, newPitch);
                p.playSound(loc, sound, maxDist/16f, newPitch);
            }
        }
    }
    /**
     * creates a imitated error sound (called when played doing something wrong)
     * @param p player
     */
    public static void playErrorSound(final Player p)
    {
        try
        {
            //p.playSound(p.getEyeLocation(), Sound.VILLAGER_NO, 0.25f, 0.75f);

        	p.playSound(p.getEyeLocation(), Sound.SUCCESSFUL_HIT, 0.25f, 0.75f);
        	Bukkit.getScheduler().scheduleSyncDelayedTask(Cannons.getPlugin(), new Runnable()
        	{
        		@Override public void run()
        		{
        			p.playSound(p.getEyeLocation(), Sound.SUCCESSFUL_HIT, 0.25f, 0.1f);
        			}
        		}
        	, 3);
        }
        catch(Exception e)
        {
        	//Fired if bukkit doen't have this sound, try/catch block is not neccesury
        	p.playSound(p.getEyeLocation(), Sound.NOTE_PIANO, 0.25f, 2f);
        	Bukkit.getScheduler().scheduleSyncDelayedTask(Cannons.getPlugin(), new Runnable()
        	{
        		@Override public void run()
        		{
        			p.playSound(p.getEyeLocation(), Sound.NOTE_PIANO, 0.25f, 0.75f);
        			}
        		}
        	, 3);
        }
    	
    }
    /**
     * find the surface in the given direction
     * @param start starting point
     * @param direction direction
     * @return returns the the location of one block in front of the surface or (if the surface is not found) the start location
     */
    public static Location findSurface(Location start, Vector direction)
    {
        World world = start.getWorld();
        Location surface = start.clone();

        //see if there is a block already - then go back if necessary
        if (!start.getBlock().isEmpty())
            surface.subtract(direction);

        //are we now in air - if not, something is wrong
        if (!start.getBlock().isEmpty())
            return start;

        int length = (int) (direction.length()*3);
        BlockIterator iter = new BlockIterator(world, start.toVector(), direction.clone().normalize(), 0, length);

        //try to find a surface of the
        while (iter.hasNext())
        {
            Block next = iter.next();
            //if there is no block, go further until we hit the surface
            if (next.isEmpty())
                surface = next.getLocation();
            else
                return surface;
        }
        return surface;
    }

    /**
     * returns a random point in a sphere
     * @param center center location
     * @param radius radius of the sphere
     * @return returns a random point in a sphere
     */
    public static Location randomPointInSphere(Location center, double radius)
    {
        Random rand = new Random();
        double r = radius*rand.nextDouble();
        double polar = Math.PI*rand.nextDouble();
        double azi = Math.PI*(rand.nextDouble()*2.0-1.0);
        //sphere coordinates
        double x = r*Math.sin(polar)*Math.cos(azi);
        double y = r*Math.sin(polar)*Math.sin(azi);
        double z = r*Math.cos(polar);
        return center.clone().add(x,z,y);
    }
}
