package me.superckl.jet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import me.superckl.jet.integration.JEIIntegrationModule;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocus.Mode;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;

/**
 * Derived from {@link mezz.jei.gui.recipes.RecipeGuiLogic} to have only desired functionality.
 * Uses only API classes.
 */
public class RecipeGuiLogic {

	private static class State {
		@Nonnull
		public final IFocus<?> focus;
		@Nonnull
		public final ImmutableList<IRecipeCategory> recipeCategories;

		public int recipeCategoryIndex;
		public int pageIndex;

		public List<IRecipeLayoutDrawable> layouts;

		public State(final IFocus<?> focus, final List<IRecipeCategory> recipeCategories, final int recipeCategoryIndex, final int pageIndex) {
			this.focus = focus;
			this.recipeCategories = ImmutableList.copyOf(recipeCategories);
			this.recipeCategoryIndex = recipeCategoryIndex;
			this.pageIndex = pageIndex;
		}
	}

	@Nonnull
	private final IRecipeRegistry recipeRegistry;

	@Nonnull
	private State state;

	/**
	 * List of recipes for the currently selected recipeClass
	 */
	private List<IRecipeWrapper> recipes = Collections.emptyList();

	public RecipeGuiLogic(final IRecipeRegistry recipeRegistry) {
		this.recipeRegistry = recipeRegistry;
		final IFocus focus = recipeRegistry.createFocus(IFocus.Mode.NONE, null);
		final List<IRecipeCategory> recipeCategories = recipeRegistry.getRecipeCategories();
		this.state = new State(focus, recipeCategories, 0, 0);
		this.updateRecipes();
	}

	public <V> boolean setFocus(final IFocus<V> focus) {
		return this.setFocus(focus, false);
	}

	private <V> boolean setFocus(final IFocus<V> focus, final boolean force){
		final List<IRecipeCategory> recipeCategories = this.recipeRegistry.getRecipeCategories(focus);
		if (recipeCategories.isEmpty() && !force)
			return false;

		final int recipeCategoryIndex = this.getRecipeCategoryIndex(recipeCategories);

		final State state = new State(focus, recipeCategories, recipeCategoryIndex, 0);
		this.setState(state);

		return true;
	}

	private int getRecipeCategoryIndex(final List<IRecipeCategory> recipeCategories) {
		final Container container = Minecraft.getMinecraft().thePlayer.openContainer;
		if (container == null)
			return 0;

		for (int i = 0; i < recipeCategories.size(); i++) {
			final IRecipeCategory recipeCategory = recipeCategories.get(i);
			if (this.recipeRegistry.getRecipeTransferHandler(container, recipeCategory) != null)
				return i;
		}

		return 0;
	}

	private void setState(final State state) {
		this.state = state;
		this.updateRecipes();
	}

	private void updateRecipes() {
		if(this.state.recipeCategories.isEmpty()){
			this.recipes = Collections.EMPTY_LIST;
			this.state.layouts = Collections.EMPTY_LIST;
			return;
		}
		final IRecipeCategory recipeCategory = this.getSelectedRecipeCategory();
		final IFocus<?> focus = this.state.focus;
		//noinspection unchecked
		this.recipes = this.recipeRegistry.getRecipeWrappers(recipeCategory, focus);
		this.state.layouts = this.getRecipeLayouts();
	}


	public IRecipeCategory getSelectedRecipeCategory() {
		return this.state.recipeCategories.get(this.state.recipeCategoryIndex);
	}

	public void nextPage() {
		this.state.pageIndex = (this.state.pageIndex + 1) % this.state.layouts.size();
	}

	public void previousPage() {
		this.state.pageIndex = (this.state.layouts.size() + this.state.pageIndex - 1) % this.state.layouts.size();
	}

	public boolean hasMultipleCategories() {
		return this.state.recipeCategories.size() > 1;
	}

	private List<IRecipeLayoutDrawable> getRecipeLayouts() {
		final List<IRecipeLayoutDrawable> recipeLayouts = new ArrayList<IRecipeLayoutDrawable>();

		if(this.state.recipeCategories.isEmpty())
			return recipeLayouts;
		final IRecipeCategory recipeCategory = this.getSelectedRecipeCategory();

		final int recipeWidgetIndex = 0;
		for (int recipeIndex = 0; recipeIndex < this.recipes.size(); recipeIndex++) { //Replaced number of pages with hard-coded 1
			final IRecipeWrapper recipeWrapper = this.recipes.get(recipeIndex);
			if (recipeWrapper == null)
				continue;
			final IRecipeLayoutDrawable layout = JEIIntegrationModule.jeiRuntime.getRecipeRegistry().createRecipeLayoutDrawable(recipeCategory, recipeWrapper, this.state.focus);
			layout.setPosition(0, 0);
			recipeLayouts.add(layout);
		}

		return recipeLayouts;
	}

	public IRecipeLayoutDrawable getCurrentLayout(){
		return this.state.layouts.isEmpty() ? null:this.state.layouts.get(this.state.pageIndex);
	}

	public void nextRecipeCategory() {
		final int recipesTypesCount = this.state.recipeCategories.size();
		this.state.recipeCategoryIndex = (this.state.recipeCategoryIndex + 1) % recipesTypesCount;
		this.state.pageIndex = 0;
		this.updateRecipes();
	}

	public boolean nextMode(){
		final Mode newMode = this.state.focus.getMode() == Mode.INPUT ? Mode.OUTPUT:Mode.INPUT;
		return this.setFocus(JEIIntegrationModule.jeiRuntime.getRecipeRegistry().createFocus(newMode, this.state.focus.getValue()));
	}

	public <V> void newFocusItem(final V focusItem){
		if(!this.setFocus(JEIIntegrationModule.jeiRuntime.getRecipeRegistry().createFocus(Mode.OUTPUT, focusItem)))
			this.setFocus(JEIIntegrationModule.jeiRuntime.getRecipeRegistry().createFocus(Mode.INPUT, focusItem), true);
	}

	public IFocus getFocus() {
		return this.state.focus;
	}

}
