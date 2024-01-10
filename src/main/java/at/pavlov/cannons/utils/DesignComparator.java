package at.pavlov.cannons.utils;

import java.util.Comparator;

import org.bukkit.block.BlockFace;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.CannonDesign;

public class DesignComparator implements Comparator<CannonDesign>
{

	@Override
	public int compare(CannonDesign design1, CannonDesign design2)
	{
		int amount1 = getCannonBlockAmount(design1);
		int amount2 = getCannonBlockAmount(design2);
		
		return amount2 - amount1;
	}
	
	private Integer getCannonBlockAmount(CannonDesign design)
	{
		if (design == null) return 0;
		//if the design is invalid something goes wrong, message the user
		if (design.getAllCannonBlocks(BlockFace.NORTH) == null) 
		{
			Cannons plugin = Cannons.getPlugin();
			plugin.logSevere("invalid cannon design for " + design.getDesignName());
			return 0;
		}
		
		return design.getAllCannonBlocks(BlockFace.NORTH).size();
	}

}
