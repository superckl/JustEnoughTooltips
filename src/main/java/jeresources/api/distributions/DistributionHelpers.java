package jeresources.api.distributions;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class DistributionHelpers
{
	public static final float PI = 3.14159265359F;

	/**
	 * @param midY      the top, middle of the triangle
	 * @param range     length of the sides
	 * @param maxChance chance at the top
	 * @return an array of 256 floats in triangular distribution
	 */
	public static float[] getTriangularDistribution(final int midY, final int range, final float maxChance)
	{
		return DistributionHelpers.getTriangularDistribution(midY - range, range, range, maxChance);
	}

	public static float[] getTriangularDistribution(final int minY, final int rand1, final int rand2, final float maxChance)
	{
		final float[] triangle = new float[rand1 + rand2 + 1];
		final float modChance = maxChance / Math.min(rand1, rand2);
		for (int i = 0; i < rand1; i++)
			for (int j = 0; j < rand2; j++)
				triangle[i + j] += modChance;
		final float[] result = new float[256];
		for (int i = 0; i < triangle.length; i++)
		{
			final int mapToPos = i + minY;
			if (mapToPos < 0) continue;
			if (mapToPos == result.length) break;
			result[mapToPos] = triangle[i];
		}
		return result;
	}

	/**
	 * @param minY   first occurrence
	 * @param maxY   last occurrence
	 * @param chance the chance
	 * @return an array of 256 floats in square distribution
	 */
	public static float[] getSquareDistribution(final int minY, final int maxY, final float chance)
	{
		final float[] result = new float[256];
		for (int i = minY; i <= maxY; i++)
			result[i] = chance;
		return result;
	}

	/**
	 * @param min0   start of the ramp
	 * @param minY   end of the ramp up
	 * @param maxY   start of the ramp down
	 * @param max0   end of ramp down
	 * @param chance the chance at the top
	 * @return an array of 256 floats in square distribution
	 */
	public static float[] getRoundedSquareDistribution(final int min0, final int minY, final int maxY, final int max0, final float chance)
	{
		final float[] result = new float[256];
		DistributionHelpers.addDistribution(result, DistributionHelpers.getRampDistribution(min0, minY, chance), min0);
		DistributionHelpers.addDistribution(result, DistributionHelpers.getSquareDistribution(minY, maxY, chance));
		DistributionHelpers.addDistribution(result, DistributionHelpers.getRampDistribution(max0, maxY, chance), maxY);
		return result;
	}

	public static float[] getUnderwaterDistribution(final float chance)
	{
		final float[] result = DistributionHelpers.getTriangularDistribution(47, 8, chance / 7);
		DistributionHelpers.addDistribution(result, DistributionHelpers.getRampDistribution(57, 62, chance), 57);
		result[62] = chance;
		DistributionHelpers.addDistribution(result, DistributionHelpers.getTriangularDistribution(55, 4, chance / 3));
		return result;
	}

	/**
	 * @param minY      first occurrence
	 * @param maxY      last occurrence
	 * @param minChance change at the bottom of the ramp
	 * @param maxChance chance at the top of the ramp
	 * @return an array of floats with length |maxY - minY| in ramp distribution
	 */
	public static float[] getRampDistribution(final int minY, final int maxY, final float minChance, final float maxChance)
	{
		if (minY == maxY) return new float[0];
		if (minY > maxY) return DistributionHelpers.reverse(DistributionHelpers.getRampDistribution(maxY, minY, minChance, maxChance));

		final int range = maxY - minY;
		final float chanceDiff = maxChance - minChance;
		final float[] result = new float[range + 1];
		for (int i = 0; i < range; i++)
			result[i] = minChance + (chanceDiff * i) / range;
		return result;
	}

	public static float[] getRampDistribution(final int minY, final int maxY, final float maxChance)
	{
		return DistributionHelpers.getRampDistribution(minY, maxY, 0, maxChance);
	}

	public static float[] getOverworldSurfaceDistribution(final int oreDiameter)
	{
		final float[] result = new float[256];
		final float[] triangularDist = DistributionHelpers.getOverworldSurface();
		final float chance = oreDiameter / 256F;
		for (int i = 0; i < result.length - oreDiameter; i++)
		{
			if (i == triangularDist.length) break;
			if (triangularDist[i] == 0) continue;
			for (int j = 0; j < oreDiameter; j++)
				result[i + j] += triangularDist[i] * chance;
		}
		return result;
	}

	public static float[] getOverworldSurface()
	{
		return DistributionHelpers.getTriangularDistribution(69, 5, 1F / 11F);
	}

	/**
	 * @param base base distribution
	 * @param add  the to add distribution
	 * @return the sum of both distributions
	 */
	public static float[] addDistribution(final float[] base, final float[] add)
	{
		return DistributionHelpers.addDistribution(base, add, 0);
	}

	public static DistributionBase addDistribution(final DistributionBase base, final DistributionBase add)
	{
		return new DistributionCustom(DistributionHelpers.addDistribution(base.getDistribution(), add.getDistribution()));
	}

	/**
	 * @param base   base distribution
	 * @param add    the to add distribution
	 * @param offset the first element from the base array to start adding to
	 * @return the sum of both distributions
	 */
	public static float[] addDistribution(final float[] base, final float[] add, final int offset)
	{
		int addCount = 0;
		for (int i = offset; i < Math.min(base.length, add.length + offset); i++)
			base[i] += add[addCount++];
		return base;
	}

	/**
	 * @param array
	 * @return a reversed version of the given array
	 */
	public static float[] reverse(final float[] array)
	{
		final float[] result = new float[array.length];
		for (int i = 0; i < array.length; i++)
			result[array.length - 1 - i] = array[i];
		return result;
	}

	@Deprecated
	public static int calculateMeanLevel(final float[] distribution, final int mid, final int oldMid, final float difference)
	{
		return DistributionHelpers.calculateMeanLevel(distribution, mid);
	}

	/**
	 * @param distribution the target array
	 * @param mid          the "best guess" of the midpoint
	 * @return the mid level of the distribution
	 */
	public static int calculateMeanLevel(final float[] distribution, int mid)
	{
		float adjacent = 0;
		float maxAdjacent = 0;
		int consecutive = 0;
		mid = 0;
		for (int i = 0; i < 4 && i < distribution.length; i++) adjacent += distribution[i];
		for (int i = 0; i < distribution.length - 4; i++)
		{
			adjacent -= distribution[i] - distribution[i + 4];
			if (adjacent > maxAdjacent)
			{
				mid = i + 2;
				maxAdjacent = adjacent + 0.00001f;
				consecutive = 0;
			} else if (adjacent > maxAdjacent - 0.00002f)
				consecutive++;
			else
			{
				mid += consecutive / 2;
				consecutive = 0;
			}
		}
		return mid;
	}

	/**
	 * @param array the to divide array
	 * @param num   the denominator
	 * @return the divided array
	 */
	public static float[] divideArray(final float[] array, final float num)
	{
		final float[] result = new float[array.length];
		for (int i = 0; i < array.length; i++)
			result[i] = array[i] / num;
		return result;
	}

	/**
	 * @param array the to multiply array
	 * @param num   the multiplier
	 * @return the divided array
	 */
	public static float[] multiplyArray(final float[] array, final float num)
	{
		final float[] result = new float[array.length];
		for (int i = 0; i < array.length; i++)
			result[i] = array[i] * num;
		return result;
	}

	public static float[] maxJoinArray(final float[] array1, final float[] array2)
	{
		final float[] result = new float[array1.length];
		if (array1.length != array2.length) return result;
		for (int i = 0; i < array1.length; i++)
			result[i] = Math.max(array1[i], array2[i]);
		return result;
	}

	public static float sum(final float[] distribution)
	{
		float result = 0;
		for (final float val : distribution)
			result += val;
		return result;
	}

	/**
	 * @param veinCount the amount of veins per chunk
	 * @param veinSize  the amount of blocks per vein
	 * @param minY      the lowest Y value for a vein
	 * @param maxY      the highest Y value for a vein
	 * @return the chance that a block appears within the specified Y boundaries
	 */
	public static float calculateChance(final int veinCount, final int veinSize, final int minY, final int maxY)
	{
		return ((float) veinCount * veinSize) / ((maxY - minY + 1) * 256);
	}

	public static float[] getDistributionFromPoints(OrePoint... points)
	{
		final Set<OrePoint> set = new TreeSet<>();
		Collections.addAll(set, points);
		points = set.toArray(new OrePoint[set.size()]);
		final float[] array = new float[256];
		DistributionHelpers.addDistribution(array, DistributionHelpers.getRampDistribution(0, points[0].level, points[0].chance));
		for (int i = 1; i < points.length; i++)
		{
			OrePoint min, max;
			if (points[i - 1].chance <= points[i].chance)
			{
				min = points[i - 1];
				max = points[i];
			} else
			{
				max = points[i - 1];
				min = points[i];
			}
			final float[] ramp = DistributionHelpers.getRampDistribution(min.level, max.level, min.chance, max.chance);
			DistributionHelpers.addDistribution(array, ramp, points[i - 1].level);
			array[points[i - 1].level] = points[i - 1].chance;
			array[points[i].level] = points[i].chance;
		}
		return array;
	}

	public static class OrePoint implements Comparable<OrePoint>
	{
		private final int level;
		private final float chance;

		public OrePoint(final int level, final float chance)
		{
			this.level = level;
			this.chance = chance;
		}

		@Override
		public int compareTo(final OrePoint o)
		{
			return this.level - o.level;
		}
	}
}
