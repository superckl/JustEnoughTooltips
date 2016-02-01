package jeresources.api.restrictions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.minecraftforge.common.DimensionManager;

public class DimensionRegistry
{
	private static Map<BlockRestriction, Set<Integer>> registry = new HashMap<BlockRestriction, Set<Integer>>();
	private static Map<Integer, DimInfo> altDimensions = new TreeMap<Integer, DimInfo>();

	private static class DimInfo
	{
		private final int dimId;
		private String name;
		private final boolean age;

		private DimInfo(final int id)
		{
			this(id, false);
		}

		private DimInfo(final int id, final boolean age)
		{
			this(id, null, age);
		}

		private DimInfo(final int id, final String name, final boolean age)
		{
			this.dimId = id;
			this.name = name;
			this.age = age;
		}

		private String getName()
		{
			if (this.name == null && DimensionManager.getWorld(this.dimId) != null && DimensionManager.getProvider(this.dimId) != null)
			{
				this.name = DimensionManager.getProvider(this.dimId).getDimensionName();
				if (this.age && !this.name.startsWith("Age")) this.name += " (Age)";
			}
			return this.name == null ? String.valueOf(this.dimId) : this.name;
		}

		private int getDimId()
		{
			return this.dimId;
		}

		private boolean isAge()
		{
			return this.age;
		}
	}

	static
	{
		DimensionRegistry.registerDimension(BlockRestriction.NETHER, -1);
		DimensionRegistry.registerDimension(BlockRestriction.STONE, 0);
		DimensionRegistry.registerDimension(BlockRestriction.END, 1);
	}

	public static void registerDimension(final BlockRestriction block, final int dim)
	{
		DimensionRegistry.registerDimension(block, dim, false);
	}

	public static void registerDimension(final BlockRestriction block, final int dim, final boolean mystAge)
	{
		Set<Integer> saved = DimensionRegistry.registry.get(block);
		if (saved == null)
			saved = new TreeSet<Integer>();
		saved.add(dim);
		DimensionRegistry.altDimensions.put(dim, new DimInfo(dim, mystAge));
		DimensionRegistry.registry.put(block, saved);
	}

	public static void registerDimension(final BlockRestriction block, final Integer... dims)
	{
		DimensionRegistry.registerDimension(block, Arrays.asList(dims));
	}

	public static void registerDimension(final BlockRestriction block, final List<Integer> dims)
	{
		Set<Integer> saved = DimensionRegistry.registry.get(block);
		if (saved == null)
			saved = new TreeSet<Integer>();
		saved.addAll(dims);
		for (final Integer dim : dims)
			DimensionRegistry.altDimensions.put(dim, new DimInfo(dim));
		DimensionRegistry.registry.put(block, saved);
	}

	public static Set<Integer> getDimensions(final BlockRestriction block)
	{
		if (DimensionRegistry.registry.containsKey(block)) return DimensionRegistry.registry.get(block);
		return null;
	}

	public static Set<Integer> getAltDimensions()
	{
		return DimensionRegistry.altDimensions.keySet();
	}

	public static String getDimensionName(final int dim)
	{
		return DimensionRegistry.altDimensions.get(dim).getName();
	}

	public static boolean contains(final int dimId)
	{
		return DimensionRegistry.altDimensions.containsKey(dimId);
	}
}
