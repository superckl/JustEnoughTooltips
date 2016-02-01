package jeresources.api.conditionals;

public class ExtendedConditional extends Conditional
{
	Conditional conditional;
	String value;

	public ExtendedConditional(final Conditional conditional, final String value)
	{
		this.conditional = conditional;
		this.value = value;
	}

	@Override
	public String toString()
	{
		return String.format(this.conditional.toString(), this.value);
	}
}
