package me.superckl.recipetooltips.util;

import java.util.Comparator;

import lombok.RequiredArgsConstructor;
import me.superckl.recipetooltips.recipe.CraftingRecipeWrapper;
import net.minecraft.item.ItemStack;

@RequiredArgsConstructor
public class CraftingRecipeMetaComparator implements Comparator<CraftingRecipeWrapper>{

	private final int desiredMeta;

	public CraftingRecipeMetaComparator() {
		this(-1);
	}

	@Override
	public int compare(final CraftingRecipeWrapper o1, final CraftingRecipeWrapper o2) {
		final ItemStack i1 = o1.getOutput().get(0);
		final ItemStack i2 = o2.getOutput().get(0);
		if(this.desiredMeta >= 0)
			if(i1.getItemDamage() == this.desiredMeta){
				if(i1.getItemDamage() == i2.getItemDamage())
					return 0;
				else
					return 1;
			}else if(i2.getItemDamage() == this.desiredMeta)
				return -1;
		return i1.getItemDamage() == i2.getItemDamage() ? 0:i1.getItemDamage() > i2.getItemDamage() ? 1:-1;
	}

}
