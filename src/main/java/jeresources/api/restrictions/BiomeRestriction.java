package jeresources.api.restrictions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;


public class BiomeRestriction
{
	public static final BiomeRestriction NONE = new BiomeRestriction();
	public static final BiomeRestriction OCEAN = new BiomeRestriction(BiomeDictionary.Type.OCEAN);
	public static final BiomeRestriction PLAINS = new BiomeRestriction(BiomeDictionary.Type.PLAINS);
	public static final BiomeRestriction FOREST = new BiomeRestriction(BiomeDictionary.Type.FOREST);
	public static final BiomeRestriction SANDY = new BiomeRestriction(BiomeDictionary.Type.FOREST);
	public static final BiomeRestriction SNOWY = new BiomeRestriction(BiomeDictionary.Type.FOREST);
	public static final BiomeRestriction HILLS = new BiomeRestriction(BiomeDictionary.Type.HILLS);
	public static final BiomeRestriction MUSHROOM = new BiomeRestriction(BiomeDictionary.Type.MUSHROOM);

	public static final BiomeRestriction HOT = new BiomeRestriction(BiomeDictionary.Type.HOT);
	public static final BiomeRestriction COLD = new BiomeRestriction(BiomeDictionary.Type.COLD);
	public static final BiomeRestriction TEMPERATE = new BiomeRestriction(Type.BLACKLIST, BiomeDictionary.Type.HOT, BiomeDictionary.Type.COLD);

	public static final BiomeRestriction EXTREME_HILLS = new BiomeRestriction(Type.WHITELIST, BiomeGenBase.extremeHills, BiomeGenBase.extremeHillsEdge);

	private ArrayList<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
	private final Type type;

	public BiomeRestriction()
	{
		this.type = Type.NONE;
	}

	public BiomeRestriction(final BiomeGenBase biome)
	{
		this(Type.WHITELIST, biome);
	}

	public BiomeRestriction(final Type type, final BiomeGenBase biome)
	{
		this(type, biome, new BiomeGenBase[0]);
	}

	public BiomeRestriction(final BiomeGenBase biome, final BiomeGenBase... moreBiomes)
	{
		this(Type.WHITELIST, biome, moreBiomes);
	}

	public BiomeRestriction(final Type type, final BiomeGenBase biome, final BiomeGenBase... moreBiomes)
	{
		this.type = type;
		switch (type)
		{
		case NONE:
			break;
		case WHITELIST:
			this.biomes.add(biome);
			this.biomes.addAll(Arrays.asList(moreBiomes));
			break;
		default:
			this.biomes = new ArrayList<BiomeGenBase>(Arrays.asList(BiomeGenBase.getBiomeGenArray()));
			this.biomes.remove(biome);
			this.biomes.removeAll(Arrays.asList(moreBiomes));
		}
	}

	public BiomeRestriction(final BiomeDictionary.Type type, final BiomeDictionary.Type... biomeTypes)
	{
		this(Type.WHITELIST, type, biomeTypes);
	}

	public BiomeRestriction(final Type type, final BiomeDictionary.Type biomeType, final BiomeDictionary.Type... biomeTypes)
	{
		this.type = type;
		switch (type)
		{
		case NONE:
			break;
		case WHITELIST:
			this.biomes = this.getBiomes(biomeType, biomeTypes);
			break;
		default:
			this.biomes = new ArrayList<BiomeGenBase>(Arrays.asList(BiomeGenBase.getBiomeGenArray()));
			this.biomes.removeAll(this.getBiomes(biomeType, biomeTypes));
		}
	}

	private ArrayList<BiomeGenBase> getBiomes(final BiomeDictionary.Type biomeType, final BiomeDictionary.Type... biomeTypes)
	{
		ArrayList<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
		biomes.addAll(Arrays.asList(BiomeDictionary.getBiomesForType(biomeType)));
		for (int i = 1; i < biomeTypes.length; i++)
		{
			final ArrayList<BiomeGenBase> newBiomes = new ArrayList<BiomeGenBase>();
			for (final BiomeGenBase biome : BiomeDictionary.getBiomesForType(biomeTypes[i]))
				if (biomes.remove(biome)) newBiomes.add(biome);
			biomes = newBiomes;
		}
		return biomes;
	}

	public List<String> toStringList()
	{
		final List<String> result = new ArrayList<String>();
		for (final BiomeGenBase biome : this.biomes)
			if (!biome.biomeName.equals("")) result.add("  " + biome.biomeName);
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof BiomeRestriction)
		{
			final BiomeRestriction other = (BiomeRestriction) obj;
			return other.biomes.size() == this.biomes.size() && other.biomes.containsAll(this.biomes);
		}
		return false;
	}

	public boolean isMergeable(final BiomeRestriction other)
	{
		return other.type == Type.NONE || (this.type != Type.NONE && !this.biomes.isEmpty() && other.biomes.containsAll(this.biomes));
	}

	@Override
	public String toString()
	{
		return "Biomes: " + this.type + (this.type != Type.NONE ? " - " + this.biomes.size() : "");
	}

	@Override
	public int hashCode()
	{
		return this.type.hashCode() ^ this.biomes.hashCode();
	}
}
