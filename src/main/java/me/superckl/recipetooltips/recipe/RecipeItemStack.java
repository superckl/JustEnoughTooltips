package me.superckl.recipetooltips.recipe;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.superckl.recipetooltips.util.ItemStackHelper;
import net.minecraft.item.ItemStack;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeItemStack {

	private final List<ItemStack> stacks;

	public ItemStack get(final int index){
		return this.stacks.get(index % this.stacks.size());
	}

	public static RecipeItemStack from(final ItemStack stack){
		return RecipeItemStack.from(Lists.newArrayList(stack));
	}

	public static RecipeItemStack from(final List<ItemStack> list){
		return new RecipeItemStack(list);
	}

	public static RecipeItemStack fromOreDict(final ItemStack stack){
		return RecipeItemStack.from(ItemStackHelper.expandItemStack(stack));
	}

	public static RecipeItemStack fromOreDict(final List<ItemStack> stacks){
		final List<ItemStack> newStacks = Lists.newArrayList();
		for(final ItemStack stack:stacks)
			newStacks.addAll(ItemStackHelper.expandItemStack(stack));
		return RecipeItemStack.from(newStacks);
	}

}
