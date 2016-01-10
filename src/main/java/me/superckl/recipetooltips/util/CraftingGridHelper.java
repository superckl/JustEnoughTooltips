package me.superckl.recipetooltips.util;

public class CraftingGridHelper {

	public static int getWidthHeight(final int size){
		if(size > 4)
			return 3;
		else if(size > 1)
			return 2;
		else
			return 1;
	}

}
