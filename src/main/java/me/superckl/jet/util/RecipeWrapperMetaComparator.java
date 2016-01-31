package me.superckl.jet.util;

import java.util.Comparator;

import lombok.RequiredArgsConstructor;
import me.superckl.jet.recipe.RecipeWrapper;
import net.minecraft.item.ItemStack;

@RequiredArgsConstructor
public class RecipeWrapperMetaComparator implements Comparator<RecipeWrapper>{

	private final int desiredMeta;

	public RecipeWrapperMetaComparator() {
		this(-1);
	}

	@Override
	public int compare(final RecipeWrapper o1, final RecipeWrapper o2) {
		final ItemStack i1 = o1.getOutput().getPrimaryStack();
		final ItemStack i2 = o2.getOutput().getPrimaryStack();
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
