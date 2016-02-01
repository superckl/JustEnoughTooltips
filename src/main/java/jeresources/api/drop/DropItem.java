package jeresources.api.drop;

import java.util.ArrayList;
import java.util.List;

import jeresources.api.conditionals.Conditional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DropItem implements Comparable<DropItem>
{
	public int minDrop, maxDrop;
	public ItemStack item;
	public float chance;
	public List<String> conditionals = new ArrayList<String>();
	private final float sortIndex;

	public DropItem(final ItemStack item)
	{
		this(item, item.stackSize);
	}

	public DropItem(final ItemStack item, final float chance)
	{
		this(item, 0, 1, chance);
	}

	/**
	 * @param item    The dropped {@link net.minecraft.item.ItemStack} (chance for drop will be 100%)
	 * @param minDrop the maximum amount dropped
	 * @param maxDrop the minimum amount dropped
	 */
	public DropItem(final ItemStack item, final int minDrop, final int maxDrop, final Conditional... conditionals)
	{
		this(item, minDrop, maxDrop, 1F, conditionals);
	}

	/**
	 * @param item    The dropped {@link net.minecraft.item.ItemStack}
	 * @param minDrop the maximum amount dropped
	 * @param maxDrop the minimum amount dropped
	 * @param chance  the chance the {@param item} gets dropped
	 */
	public DropItem(final ItemStack item, final int minDrop, final int maxDrop, final float chance, final Conditional... conditionals)
	{
		this.item = item;
		this.minDrop = minDrop;
		this.maxDrop = maxDrop;
		this.chance = chance;
		this.sortIndex = Math.min(chance, 1F) * (minDrop + maxDrop);
		for (final Conditional conditional : conditionals)
			this.conditionals.add(conditional.toString());
	}

	/**
	 * @param item    The dropped {@link net.minecraft.item.Item} (chance for drop will be 100% and the itemDamage will be default)
	 * @param minDrop the maximum amount dropped
	 * @param maxDrop the minimum amount dropped
	 */
	public DropItem(final Item item, final int minDrop, final int maxDrop, final Conditional... conditionals)
	{
		this(new ItemStack(item), minDrop, maxDrop, 1F, conditionals);
	}

	/**
	 * @param item       The dropped {@link net.minecraft.item.Item} (chance for drop will be 100%)
	 * @param itemDamage the damage on the item
	 * @param minDrop    the maximum amount dropped
	 * @param maxDrop    the minimum amount dropped
	 */
	public DropItem(final Item item, final int itemDamage, final int minDrop, final int maxDrop, final Conditional... conditionals)
	{
		this(new ItemStack(item, 1, itemDamage), minDrop, maxDrop, 1F, conditionals);
	}

	/**
	 * @param item    The dropped {@link net.minecraft.item.Item}
	 * @param minDrop the maximum amount dropped
	 * @param maxDrop the minimum amount dropped
	 * @param chance  the chance the {@param item} gets dropped
	 */
	public DropItem(final Item item, final int minDrop, final int maxDrop, final float chance, final Conditional... conditionals)
	{
		this(new ItemStack(item), minDrop, maxDrop, chance, conditionals);
	}

	/**
	 * @param item       The dropped {@link net.minecraft.item.Item}
	 * @param itemDamage the damage on the item
	 * @param minDrop    the maximum amount dropped
	 * @param maxDrop    the minimum amount dropped
	 * @param chance     the chance the {@param item} gets dropped
	 */
	public DropItem(final Item item, final int itemDamage, final int minDrop, final int maxDrop, final float chance, final Conditional... conditionals)
	{
		this(new ItemStack(item, 1, itemDamage), minDrop, maxDrop, chance, conditionals);
	}

	@Override
	public String toString()
	{
		if (this.minDrop == this.maxDrop) return this.minDrop + this.getDropChance();
		return this.minDrop + "-" + this.maxDrop + this.getDropChance();
	}

	private String getDropChance()
	{
		return this.chance < 1F ? " (" + this.formatChance() + "%)" : "";
	}

	private String formatChance()
	{
		final float chance = this.chance * 100;
		if (chance < 10) return String.format("%.1f", chance);
		return String.format("%2d", (int) chance);
	}

	public String chanceString()
	{
		if (this.chance >= 0.995f)
			return String.format("%.2G", this.chance);
		else
			return String.format("%.2G%%", this.chance * 100f);
	}

	public List<String> getTooltipText()
	{
		return this.conditionals;
	}

	public void addConditionals(final List<String> conditionals)
	{
		this.conditionals.addAll(conditionals);
	}

	public float getSortIndex()
	{
		return this.sortIndex;
	}

	@Override
	public int compareTo(final DropItem o)
	{
		final float result = this.getSortIndex() - o.getSortIndex();
		if (Math.round(result) == 0 && this.item.getIsItemStackEqual(o.item)) return 0;
		return result < 0 ? 1 : -1;
	}
}
