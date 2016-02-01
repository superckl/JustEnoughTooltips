package jeresources.api.restrictions;

import java.util.List;

public class Restriction
{
	public static final Restriction OVERWORLD_LIKE = new Restriction();
	public static final Restriction NETHER_LIKE = new Restriction(BlockRestriction.NETHER);
	public static final Restriction END_LIKE = new Restriction(BlockRestriction.END);

	public static final Restriction OVERWORLD = new Restriction(DimensionRestriction.OVERWORLD);
	public static final Restriction NETHER = new Restriction(BlockRestriction.NETHER, DimensionRestriction.NETHER);
	public static final Restriction END = new Restriction(BlockRestriction.END, DimensionRestriction.END);

	private final BlockRestriction blockRestriction;
	private final BiomeRestriction biomeRestriction;
	private final DimensionRestriction dimensionRestriction;

	public Restriction()
	{
		this(BiomeRestriction.NONE);
	}

	public Restriction(final BlockRestriction blockRestriction)
	{
		this(blockRestriction, BiomeRestriction.NONE, DimensionRestriction.NONE);
	}

	public Restriction(final BiomeRestriction biomeRestriction)
	{
		this(BlockRestriction.STONE, biomeRestriction, DimensionRestriction.NONE);
	}

	public Restriction(final DimensionRestriction dimensionRestriction)
	{
		this(BlockRestriction.STONE, BiomeRestriction.NONE, dimensionRestriction);
	}

	public Restriction(final BlockRestriction blockRestriction, final BiomeRestriction biomeRestriction)
	{
		this(blockRestriction, biomeRestriction, DimensionRestriction.NONE);
	}

	public Restriction(final BlockRestriction blockRestriction, final DimensionRestriction dimensionRestriction)
	{
		this(blockRestriction, BiomeRestriction.NONE, dimensionRestriction);
	}

	public Restriction(final BiomeRestriction biomeRestriction, final DimensionRestriction dimensionRestriction)
	{
		this(BlockRestriction.STONE, biomeRestriction, dimensionRestriction);
	}

	public Restriction(final BlockRestriction blockRestriction, final BiomeRestriction biomeRestriction, final DimensionRestriction dimensionRestriction)
	{
		this.blockRestriction = blockRestriction;
		this.biomeRestriction = biomeRestriction;
		this.dimensionRestriction = dimensionRestriction;
	}

	public List<String> getBiomeRestrictions()
	{
		return this.biomeRestriction.toStringList();
	}

	public List<String> getDimensionRestrictions()
	{
		return this.dimensionRestriction.getValidDimensions(this.blockRestriction);
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof Restriction)) return false;
		final Restriction other = (Restriction) obj;
		if (!other.biomeRestriction.equals(this.biomeRestriction)) return false;
		if (!other.blockRestriction.equals(this.blockRestriction)) return false;
		if (!other.dimensionRestriction.equals(this.dimensionRestriction)) return false;
		return true;
	}

	public boolean isMergeable(final Restriction restriction)
	{
		if (!this.biomeRestriction.isMergeable(restriction.biomeRestriction)) return false;
		if (!this.blockRestriction.equals(restriction.blockRestriction)) return false;
		if (!this.dimensionRestriction.isMergeable(restriction.dimensionRestriction)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return this.blockRestriction.toString() + ", " + this.dimensionRestriction.toString() + ", " + this.biomeRestriction.toString();
	}

	@Override
	public int hashCode()
	{
		return this.blockRestriction.hashCode() ^ this.dimensionRestriction.hashCode() ^ this.biomeRestriction.hashCode();
	}
}
