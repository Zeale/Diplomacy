package org.alixia.games.diplomacy;

import java.util.Objects;

import org.alixia.games.diplomacy.BoardEntity.Type;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public final class Board extends Pane {

	// Update piece sizes when added
	{
		getChildren().addListener(new ListChangeListener<Node>() {

			@Override
			public void onChanged(Change<? extends Node> c) {
				while (c.next())
					if (c.wasAdded())
						for (Node n : c.getAddedSubList())
							if (n instanceof ImageView && BoardEntity.isBoardEntityImageView((ImageView) n)) {
								ImageView img = (ImageView) n;
								img.fitWidthProperty().bind(widthProperty.divide(getBoardSize()));
								img.fitHeightProperty().bind(heightProperty.divide(getBoardSize()));
							} else
								;
					else
						for (Node n : c.getRemoved())
							if (n instanceof ImageView && BoardEntity.isBoardEntityImageView((ImageView) n)) {

								ImageView img = (ImageView) n;

								img.fitWidthProperty().unbind();
								img.fitHeightProperty().unbind();

								// Also unbind the position that it has been bound to.

								n.layoutXProperty().unbind();
								n.layoutYProperty().unbind();
							}
			}
		});
	}

	private DoubleProperty widthProperty = new SimpleDoubleProperty(), heightProperty = new SimpleDoubleProperty();

	private final ImageView grid = new ImageView("/org/alixia/games/diplomacy/_resources/graphics/grid.png"),
			background = new ImageView("/org/alixia/games/diplomacy/_resources/graphics/bg.png"),
			hit_tint = new ImageView("/org/alixia/games/diplomacy/_resources/graphics/frame_dmg.png");

	// Setting sizing for images
	{
		grid.fitHeightProperty().bind(heightProperty);
		grid.fitWidthProperty().bind(widthProperty);
		background.fitHeightProperty().bind(heightProperty);
		background.fitWidthProperty().bind(widthProperty);
		hit_tint.fitHeightProperty().bind(heightProperty);
		hit_tint.fitWidthProperty().bind(widthProperty);
	}

	public int getBoardSize() {
		return entityMap.length;
	}

	// Building game board (laying out images)
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

		// Adding default pieces ~ this can only be done after entityMap is set and,
		// therefore, can't be done during object initialization.

		// blue pieces in top left
		put(new BoardEntity(Type.BLUE_PIECE), 0, 0);// This is NOT the first square, since board indices start @ 0.
		put(new BoardEntity(Type.BLUE_PIECE), 0, 1);
		put(new BoardEntity(Type.BLUE_PIECE), 1, 0);

		// white pieces in bottom left
		put(new BoardEntity(Type.WHITE_PIECE), size - 1, 0);
		put(new BoardEntity(Type.WHITE_PIECE), size - 2, 0);
		put(new BoardEntity(Type.WHITE_PIECE), size - 1, 1);

		// red pieces in bottom right
		put(new BoardEntity(Type.RED_PIECE), size - 1, size - 1);
		put(new BoardEntity(Type.RED_PIECE), size - 2, size - 1);
		put(new BoardEntity(Type.RED_PIECE), size - 1, size - 2);

	}

	public Board() {
		this(8);
	}

	private boolean outsideBorders(int row, int col) {
		return row > entityMap.length || row < 0 || col > entityMap.length || col < 0;
	}

	public BoardEntity put(BoardEntity entity, int row, int col) {
		Objects.requireNonNull(entity);
		if (outsideBorders(row, col))
			throw new RuntimeException("Position outside of board borders.");

		// Remove previous entity from board
		BoardEntity previousEntity = entityMap[row][col];
		if (previousEntity != null)
			getChildren().remove(previousEntity.icon);

		// Add new entity to board.
		entityMap[row][col] = entity;
		getChildren().add(entity.icon);

		setPos(entity, row, col);

		return previousEntity;

	}

	private void setPos(BoardEntity entity, int row, int col) {
		// Every time these methods are called, the previous binds are overridden. (I
		// think.)
		entity.icon.layoutXProperty().bind(widthProperty.multiply(col).divide(getBoardSize()));
		entity.icon.layoutYProperty().bind(heightProperty.multiply(row).divide(getBoardSize()));
	}

}
