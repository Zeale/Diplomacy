package org.alixia.games.diplomacy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.alixia.games.diplomacy.BoardEntity.Type;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public final class Board extends Pane {

	private final Object BOARD_ENTITY_CLICK_EVENT_HANDLER_KEY = new Object();

	// Configure BoardEntities' image views to work with this board.
	{
		// TODO Add checkup to see if an image view is already bound to another board.
		getChildren().addListener(new ListChangeListener<Node>() {

			@Override
			@SuppressWarnings("unchecked")
			public void onChanged(Change<? extends Node> c) {
				while (c.next())
					if (c.wasAdded())
						for (Node n : c.getAddedSubList())
							if (n instanceof ImageView && BoardEntity.isBoardEntityImageView((ImageView) n)) {
								ImageView img = (ImageView) n;
								img.fitWidthProperty().bind(widthProperty.divide(getBoardSize()));
								img.fitHeightProperty().bind(heightProperty.divide(getBoardSize()));

								EventHandler<MouseEvent> handler = event -> {
									BoardEntity clickedEntity = BoardEntity.getBoardEntity(n);
									if (event.getButton().equals(MouseButton.PRIMARY)) {
										handleEntityClicked(event, clickedEntity);
										event.consume();
									}
								};
								n.addEventFilter(MouseEvent.MOUSE_CLICKED, handler);

								n.getProperties().put(BOARD_ENTITY_CLICK_EVENT_HANDLER_KEY, handler);

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

								// And remove event handlers
								n.removeEventFilter(MouseEvent.MOUSE_CLICKED, (EventHandler<MouseEvent>) n
										.getProperties().remove(BOARD_ENTITY_CLICK_EVENT_HANDLER_KEY));

							}
			}
		});

	}

	private final DoubleProperty widthProperty = new SimpleDoubleProperty(),
			heightProperty = new SimpleDoubleProperty();

	private final ImageView grid = new ImageView("/org/alixia/games/diplomacy/_resources/graphics/grid.png"),
			background = new ImageView("/org/alixia/games/diplomacy/_resources/graphics/bg.png"),
			hit_tint = new ImageView("/org/alixia/games/diplomacy/_resources/graphics/frame_dmg.png");

	// Setup pane to distribute mouse events to handler methods
	{
		addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getButton().equals(MouseButton.PRIMARY))
				handleBoardClicked(event, (int) (event.getSceneY() / heightProperty.get() * getBoardSize()),
						(int) (event.getSceneX() / widthProperty.get() * getBoardSize()));
		});
	}

	protected final List<BoardEntity> getEntities() {
		List<BoardEntity> entities = new LinkedList<>();
		for (BoardEntity[] beArr : entityMap)
			for (BoardEntity be : beArr)
				if (be != null)
					entities.add(be);

		return Collections.unmodifiableList(entities);
	}

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

		initBoard();

	}

	protected void initBoard() {
		// Adding default pieces ~ this can only be done after entityMap is set and,
		// therefore, can't be done during object initialization.

		// blue pieces in top left
		put(new BoardEntity(Type.BLUE_TOWER), 0, 0);// This is NOT the first square, since board indices start @ 0.
		put(new BoardEntity(Type.BLUE_PIECE), 0, 1);
		put(new BoardEntity(Type.BLUE_PIECE), 1, 0);

		// white pieces in bottom left
		put(new BoardEntity(Type.WHITE_TOWER), getBoardSize() - 1, 0);
		put(new BoardEntity(Type.WHITE_PIECE), getBoardSize() - 2, 0);
		put(new BoardEntity(Type.WHITE_PIECE), getBoardSize() - 1, 1);

		// red pieces in bottom right
		put(new BoardEntity(Type.RED_TOWER), getBoardSize() - 1, getBoardSize() - 1);
		put(new BoardEntity(Type.RED_PIECE), getBoardSize() - 2, getBoardSize() - 1);
		put(new BoardEntity(Type.RED_PIECE), getBoardSize() - 1, getBoardSize() - 2);

		nextTurn();

	}

	private Team currentTeam;

	protected Team getCurrentTeam() {
		return currentTeam;
	}

	protected void selectTeam(Team team) {
		if (currentTeam != null)
			for (BoardEntity be : getEntities())
				if (be.getType() == currentTeam.boardEntityType)
					be.deselectTeam();
		currentTeam = team;
		for (BoardEntity be : getEntities())
			if (be.getType() == team.boardEntityType)
				be.selectTeam(team);
	}

	protected void nextTurn() {
		if (currentTeam == null) {
			selectTeam(Team.RED);
			return;
		}

		Team[] teams = Team.values();
		int point = currentTeam.ordinal();

		selectTeam(teams[point + 1 == teams.length ? 0 : point + 1]);
	}

	protected enum Team {

		RED(BoardEntity.Type.RED_PIECE, Color.RED), WHITE(BoardEntity.Type.WHITE_PIECE,
				Color.WHITE), BLUE(BoardEntity.Type.BLUE_PIECE, Color.BLUE);

		private final BoardEntity.Type boardEntityType;
		private final DropShadow selectionEffect = new DropShadow();

		public DropShadow getSelectionEffect() {
			return selectionEffect;
		}

		{
			selectionEffect.setRadius(80);
		}

		private Team(Type boardEntityType, Color shadowColor) {
			this.boardEntityType = boardEntityType;
			selectionEffect.setColor(shadowColor);
		}

		public BoardEntity.Type getBoardEntityType() {
			return boardEntityType;
		}

	}

	protected void swap(int row0, int col0, int row1, int col1) {
		put(put(getEntity(row0, col0), row1, col1), row0, col0);
	}

	protected void swap(BoardEntity first, BoardEntity second) {
		swap(getRow(first), getCol(first), getRow(second), getCol(second));
	}

	protected int getRow(BoardEntity entity) {
		for (int i = 0; i < entityMap.length; i++) {
			for (int j = 0; j < entityMap[i].length; j++) {
				if (entityMap[i][j] == entity)
					return i;
			}
		}
		return -1;
	}

	protected int getCol(BoardEntity entity) {
		for (int i = 0; i < entityMap.length; i++) {
			for (int j = 0; j < entityMap[i].length; j++) {
				if (entityMap[i][j] == entity)
					return i = j;
			}
		}
		return -1;
	}

	public Board() {
		this(8);
	}

	private boolean outsideBorders(int row, int col) {
		return row > entityMap.length || row < 0 || col > entityMap.length || col < 0;
	}

	protected boolean hasEntity(int row, int col) {
		return getEntity(row, col) != null;
	}

	protected BoardEntity getEntity(int row, int col) {
		return entityMap[row][col];
	}

	protected BoardEntity put(BoardEntity entity, int row, int col) {
		Objects.requireNonNull(entity);
		if (outsideBorders(row, col))
			throw new RuntimeException("Position outside of board borders.");

		// Remove previous entity from board
		BoardEntity previousEntity = entityMap[row][col];
		if (previousEntity != null)
			getChildren().remove(previousEntity.icon);

		// Remove new entity from its previous position.
		if (containsEntity(entity))
			entityMap[getRow(entity)][getCol(entity)] = null;

		// Add new entity to board.
		entityMap[row][col] = entity;
		if (!getChildren().contains(entity.icon))
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

	protected boolean containsEntity(BoardEntity entity) {
		return getRow(entity) != -1;
	}

	/*
	 * Board implementation code
	 * 
	 * The following code should act as if it is a subclass implementing Board (and,
	 * honestly should be written in a subclass). It must, therefore, not access any
	 * private methods above this comment. You know, the general implementation
	 * contract...
	 * 
	 * This impl code can still be overridden by subclasses to provide more features
	 * & stuff.
	 */

	private BoardEntity selectedEntity;

	protected BoardEntity selectEntity(BoardEntity entity) {
		if (entity == selectedEntity)
			return selectedEntity;
		if (entity != null)
			entity.select();

		BoardEntity currEntity = unselectEntity();
		selectedEntity = entity;
		return currEntity;
	}

	protected boolean isEntitySelected() {
		return selectedEntity != null;
	}

	protected BoardEntity unselectEntity() {
		if (selectedEntity != null)
			selectedEntity.deselect();
		BoardEntity currEntity = selectedEntity;
		selectedEntity = null;
		return currEntity;
	}

	protected BoardEntity deselectEntity() {
		return unselectEntity();
	}

	protected void handleEntityClicked(MouseEvent event, BoardEntity entity) {
		// Testing code
		if (getSelectedEntity() == entity)
			unselectEntity();
		else {
			if (isEntitySelected()) {
				swap(getSelectedEntity(), entity);
				unselectEntity();
				nextTurn();
				System.out.println(currentTeam);
			} else if (entity.getType() == getCurrentTeam().boardEntityType) {
				selectEntity(entity);
			}
		}

		// if(selectedEntity.getType()==Type.RED)// Or something better to get a type,
		// specifically, something that acknowledges that subclass types can exist.

	}

	protected BoardEntity getSelectedEntity() {
		return selectedEntity;
	}

	protected void handleBoardClicked(MouseEvent event, int row, int col) {

		// Debug code
		// System.out.println("Board Clicked @ [row=" + (row + 1) + ", col=" + (col + 1)
		// + "]");

		if (isEntitySelected())
			if (!hasEntity(row, col)) {
				put(getSelectedEntity(), row, col);
				unselectEntity();
				nextTurn();
			}

	}

}
