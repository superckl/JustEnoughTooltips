package jeresources.api.distributions;

public abstract class DistributionBase
{
	private final float[] distribution;
	protected int bestHeight;

	public DistributionBase(final float[] distribution)
	{
		this.distribution = distribution;
	}

	public float[] getDistribution()
	{
		return this.distribution;
	}

	public int getBestHeight()
	{
		return this.bestHeight;
	}
}
