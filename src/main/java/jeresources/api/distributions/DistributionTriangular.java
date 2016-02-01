package jeresources.api.distributions;

public class DistributionTriangular extends DistributionBase
{
	/**
	 * @param midY      top of the triangular distribution
	 * @param range     length of the sides
	 * @param maxChance chance at the top
	 */
	public DistributionTriangular(final int midY, final int range, final float maxChance)
	{
		super(DistributionHelpers.getTriangularDistribution(midY, range, maxChance));
		this.bestHeight = midY;
	}
}
