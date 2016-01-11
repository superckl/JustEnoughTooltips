package me.superckl.recipetooltips.recipe;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.superckl.recipetooltips.util.ItemStackHelper;
import net.minecraft.item.ItemStack;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeMultiItemStack extends RecipeStack{

	private final List<ItemStack> stacks;

	public ItemStack get(final int index){
		return this.stacks.get(index % this.stacks.size());
	}

	public static RecipeMultiItemStack from(final ItemStack stack){
		return RecipeMultiItemStack.from(Lists.newArrayList(stack));
	}

	public static RecipeMultiItemStack from(final List<ItemStack> list){
		return new RecipeMultiItemStack(list);
	}

	public static RecipeMultiItemStack fromOreDict(final ItemStack stack){
		return stack == null ? null:RecipeMultiItemStack.from(ItemStackHelper.expandItemStack(stack));
	}

	public static RecipeMultiItemStack fromOreDict(final List<ItemStack> stacks){
		final List<ItemStack> newStacks = Lists.newArrayList();
		for(final ItemStack stack:stacks)
			if(stack != null)
				newStacks.addAll(ItemStackHelper.expandItemStack(stack));
		return newStacks.isEmpty() ? null:RecipeMultiItemStack.from(newStacks);
	}

}
