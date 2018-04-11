package org.alixia.games.diplomacy;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public final class Board extends Pane {

	private DoubleProperty widthProperty = new SimpleDoubleProperty(), heightProperty = new SimpleDoubleProperty();

	private final ImageView grid = new ImageView("org/alixia/games/diplomacy/_resources/graphics/grid.png"),
			background = new ImageView("org/alixia/games/diplomacy/_resources/graphics/bg.png"),
			hit_tint = new ImageView("org/alixia/games/diplomacy/_resources/graphics/frame_dmg.png");

	// Setting sizing for images
	{
		// TODO
	}

	// Drawing nodes to pane
	{
		getChildren().addAll(background, grid, hit_tint);
		hit_tint.setOpacity(0);
	}

	@Override
	protected void setWidth(double value) {
		super.setWidth(value);
		widthProperty.set(value);
	}

	@Override
	protected void setHeight(double value) {
		super.setHeight(value);
		heightProperty.set(value);
	}

	private final BoardEntity[][] entityMap;

	/**
	 * For now, the board will be a size of eight. Once gridlines are drawn by the
	 * program and not rendered by image, this can change.
	 * 
	 * @param size
	 *            The size of the game board.
	 */
	private Board(int size) {
		entityMap = new BoardEntity[size][size];
	}

	public Board() {
		this(8);
	}

	public boolean outsideBorders(int row, int col) {
		return row > entityMap.length || row < 0 || col > entityMap.length || col < entityMap.length;
	}

}
