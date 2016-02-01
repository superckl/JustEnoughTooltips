package jeresources.api.restrictions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.util.StatCollector;

public class DimensionRestriction
{
	public static final DimensionRestriction OVERWORLD = new DimensionRestriction(0);
	public static final DimensionRestriction NETHER = new DimensionRestriction(-1);
	public static final DimensionRestriction END = new DimensionRestriction(1);
	public static final DimensionRestriction NONE = new DimensionRestriction();

	private int min;
	private int max;
	private final Type type;

	public DimensionRestriction()
	{
		this.type = Type.NONE;
	}

	public DimensionRestriction(final int dim)
	{
		this(dim, dim);
	}

	public DimensionRestriction(final Type type, final int dim)
	{
		this(type, dim, dim);
	}

	public DimensionRestriction(final int minDim, final int maxDim)
	{
		this(Type.WHITELIST, minDim, maxDim);
	}

	public DimensionRestriction(final Type type, final int minDim, final int maxDim)
	{
		this.type = type;
		this.min = Math.min(minDim, maxDim);
		this.max = Math.max(maxDim, minDim);
	}

	public List<String> getValidDimensions(final BlockRestriction blockRestriction)
	{
		final Set<Integer> dimensions = DimensionRegistry.getDimensions(blockRestriction);
		if (dimensions != null) return this.getDimensionString(dimensions);
		return this.getAltDimensionString(DimensionRegistry.getAltDimensions());
	}

	private Set<Integer> getValidDimensions(final Set<Integer> dimensions)
	{
		if (this.type == Type.NONE) return dimensions;
		final Set<Integer> result = new TreeSet<Integer>();
		for (final int dimension : dimensions)
			if (dimension >= this.min == (this.type == Type.WHITELIST) == dimension <= this.max) result.add(dimension);
		return result;
	}

	private List<String> getDimensionString(final Set<Integer> dimensions)
	{
		return this.getStringList(this.getValidDimensions(dimensions));
	}

	private List<String> getStringList(final Set<Integer> set)
	{
		final List<String> result = new ArrayList<>();
		for (final Integer i : set)
		{
			final String dimName = DimensionRegistry.getDimensionName(i);
			if (dimName != null) result.add("  " + dimName);
		}
		return result;
	}

	private List<String> getAltDimensionString(final Set<Integer> dimensions)
	{
		final Set<Integer> validDimensions = new TreeSet<Integer>();
		int dimMin = Integer.MAX_VALUE;
		int dimMax = Integer.MIN_VALUE;
		for (final Integer dim : dimensions)
		{
			if (dim < dimMin) dimMin = dim;
			if (dim > dimMax) dimMax = dim;
		}
		for (int i = Math.min(this.min, dimMin) - 1; i <= Math.max(this.max, dimMax) + 1; i++)
			if (!dimensions.contains(i)) validDimensions.add(i);
		final List<String> result = this.getStringList(this.getValidDimensions(this.type != Type.NONE ? validDimensions : dimensions));
		if (result.isEmpty()) result.add(StatCollector.translateToLocal("ner.dim.no"));
		switch (this.type)
		{
		default:
			break;
		case NONE:
			result.add(0, StatCollector.translateToLocal("ner.not"));
			break;
		case BLACKLIST:
			result.add(0, "<=");
			result.add(result.size(), "=<");
		}
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof DimensionRestriction)
		{
			final DimensionRestriction other = (DimensionRestriction) obj;
			return other.min == this.min && other.max == this.max && other.type == this.type;
		}
		return false;
	}

	public boolean isMergeable(final DimensionRestriction other)
	{
		if (other.type == Type.NONE) return true;
		final int dimMin = Math.min(this.min, other.min) - 1;
		final int dimMax = Math.max(this.max, other.max) + 1;
		final Set<Integer> testDimensions = new TreeSet<Integer>();
		for (int dim = dimMin; dim <= dimMax; dim++)
			testDimensions.add(dim);
		final Set<Integer> thisValidDimensions = this.getValidDimensions(testDimensions);
		final Set<Integer> otherValidDimensions = other.getValidDimensions(testDimensions);
		return otherValidDimensions.containsAll(thisValidDimensions);
	}

	@Override
	public String toString()
	{
		return "Dimension: " + this.type + (this.type != Type.NONE ? " " + this.min + "-" + this.max : "");
	}

	@Override
	public int hashCode()
	{
		return this.type.hashCode() ^ this.min ^ this.max;
	}
}
