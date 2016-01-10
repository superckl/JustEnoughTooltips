package me.superckl.recipetooltips.util;

import java.util.List;

import com.google.common.collect.Lists;

import me.superckl.recipetooltips.recipe.RecipeItemStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemStackHelper {

	public static List<ItemStack> expandItemStack(final ItemStack stack){
		final Item item = stack.getItem();
		if(item.getMetadata(stack) != OreDictionary.WILDCARD_VALUE)
			return Lists.newArrayList(stack);
		final List<ItemStack> stacks = Lists.newArrayList();
		final List<ItemStack> subItems = Lists.newArrayList();
		for(final CreativeTabs itemTab: item.getCreativeTabs()){
			subItems.clear();
			item.getSubItems(item, itemTab, subItems);
			stacks.addAll(subItems);
		}

		if(stacks.isEmpty())
			stacks.add(stack);
		return stacks;
	}

	public static RecipeItemStack[] fromItemStacks(final ItemStack[] stacks){
		final RecipeItemStack[] rStacks = new RecipeItemStack[stacks.length];
		for(int i = 0; i < stacks.length; i++)
			if(stacks[i] == null)
				rStacks[i] = null;
			else
				rStacks[i] = RecipeItemStack.from(stacks[i]);
		return rStacks;
	}

}
