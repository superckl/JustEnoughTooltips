package jeresources.api.distributions;

public class DistributionCustom extends DistributionBase
{

	public DistributionCustom(final float[] distribution)
	{
		super(distribution);
		this.bestHeight = DistributionHelpers.calculateMeanLevel(this.getDistribution(), distribution.length / 2);
	}

	public DistributionCustom(final float[] distribution, final int bestHeight)
	{
		super(distribution);
		this.bestHeight = bestHeight;
	}

}
