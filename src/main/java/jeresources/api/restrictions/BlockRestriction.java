package jeresources.api.restrictions;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BlockRestriction
{
	public static final BlockRestriction STONE = new BlockRestriction(Blocks.stone);
	public static final BlockRestriction NETHER = new BlockRestriction(Blocks.netherrack);
	public static final BlockRestriction END = new BlockRestriction(Blocks.end_stone);

	private final Block block;
	private final int metadata;

	public BlockRestriction(final Block block)
	{
		this(block, 0);
	}

	public BlockRestriction(final Block block, final int metadata)
	{
		this.block = block;
		this.metadata = metadata;
	}

	@Override
	public int hashCode()
	{
		return this.block.hashCode() ^ this.metadata;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof BlockRestriction)
		{
			final BlockRestriction other = (BlockRestriction) obj;
			return other.block == this.block && other.metadata == this.metadata;
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "Block: " + this.block.getUnlocalizedName() + ":" + this.metadata;
	}
}
