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

		addEventHandler(MouseEvent.MOUSE_MOVED, this::handleMouseMove);
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
				for (BoardEntity.Type be_t : getCurrentTeam().getBoardEntityTypes())
					if (be.getType() == be_t)
						be.deselectTeam();
		currentTeam = team;
		for (BoardEntity be : getEntities())
			for (BoardEntity.Type be_t : team.getBoardEntityTypes())
				if (be.getType() == be_t)
					be.selectTeam(team);
	}

	protected void nextTurn() {
		if (currentTeam == null) {
			selectTeam(Team.RED);
			return;
		}

		Team[] teams = Team.values();
		int point = currentTeam.ordinal();
		if (++point == teams.length) {
			point = 0;
			nextRound();
		}

		selectTeam(teams[point]);

		if (startPlacingPieces) {
			BoardEntity entity;
			switch (getCurrentTeam()) {
			case RED:
				entity = new BoardEntity(Type.RED_PIECE);
				break;
			case WHITE:
				entity = new BoardEntity(Type.WHITE_PIECE);
				break;
			case BLUE:
				entity = new BoardEntity(Type.BLUE_PIECE);
				break;
			default:
				entity = new BoardEntity(Type.RED_PIECE);
			}
			queuePiecePlace(entity);
		}

	}

	protected void nextRound() {
		startPlacingPieces = true;
	}

	private boolean startPlacingPieces = false;

	protected void queuePiecePlace(BoardEntity piece) {
		if (isPieceQueued())
			unQueuePiece();
		piece.icon.setOpacity(0.35);
		piece.icon.setMouseTransparent(true);
		getChildren().add(piece.icon);
		queuedPiece = piece;
	}

	private BoardEntity queuedPiece;

	protected BoardEntity getQueuedPiece() {
		return queuedPiece;
	}

	protected boolean isPieceQueued() {
		return queuedPiece != null;
	}

	protected BoardEntity unQueuePiece() {
		BoardEntity entity = queuedPiece;
		queuedPiece = null;
		getChildren().remove(entity.icon);
		entity.icon.setLayoutX(0);
		entity.icon.setLayoutY(0);
		entity.icon.setOpacity(1);
		entity.icon.setMouseTransparent(false);
		return entity;
	}

	private void handleMouseMove(MouseEvent event) {
		if (!isPieceQueued())
			return;
		queuedPiece.icon.setLayoutX(event.getSceneX() - queuedPiece.icon.getFitWidth() / 2);
		queuedPiece.icon.setLayoutY(event.getSceneY() - queuedPiece.icon.getFitHeight() / 2);
	}

	protected enum Team {

		RED(Color.RED, BoardEntity.Type.RED_PIECE, BoardEntity.Type.RED_TOWER), WHITE(Color.WHITE,
				BoardEntity.Type.WHITE_PIECE, BoardEntity.Type.WHITE_TOWER), BLUE(Color.BLUE,
						BoardEntity.Type.BLUE_PIECE, BoardEntity.Type.BLUE_TOWER);

		private final BoardEntity.Type[] boardEntityTypes;
		private final DropShadow selectionEffect = new DropShadow();

		public DropShadow getSelectionEffect() {
			return selectionEffect;
		}

		{
			selectionEffect.setRadius(80);
		}

		private Team(Color shadowColor, BoardEntity.Type... types) {
			boardEntityTypes = types;
			selectionEffect.setColor(shadowColor);
		}

		public BoardEntity.Type[] getBoardEntityTypes() {
			return boardEntityTypes;
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

	protected void handleEntityClicked(MouseEvent event, BoardEntity clickedEntity) {

		if (isPieceQueued()) {
			if (clickedEntity.getType() == Type.UNCLAIMED_TOWER) {
				Type towerType;
				Team team;
				switch (queuedPiece.getType()) {
				case BLUE_PIECE:
					towerType = Type.BLUE_TOWER;
					team = Team.BLUE;
					break;
				case RED_PIECE:
					towerType = Type.RED_TOWER;
					team = Team.RED;
					break;
				case WHITE_PIECE:
					towerType = Type.WHITE_TOWER;
					team = Team.WHITE;
				default:
					towerType = Type.UNCLAIMED_TOWER;
					team = Team.RED;
					break;
				}
				put(new BoardEntity(towerType), getRow(clickedEntity), getCol(clickedEntity));
				unQueuePiece();
				selectTeam(team);
			}
		} else if (getSelectedEntity() == clickedEntity)
			unselectEntity();
		else if (isEntitySelected()) {

			BoardEntity selectedEntity = getSelectedEntity();

			if (selectedEntity.getType().isTower()) {
				Type pieceType;
				switch (selectedEntity.getType()) {
				case RED_TOWER:
					pieceType = Type.RED_PIECE;
					break;
				case BLUE_TOWER:
					pieceType = Type.BLUE_PIECE;
					break;
				case WHITE_TOWER:
					pieceType = Type.WHITE_PIECE;
					break;
				// Should never happen
				default:
					pieceType = Type.UNCLAIMED_TOWER;
				}
				put(new BoardEntity(Type.UNCLAIMED_TOWER), getRow(selectedEntity), getCol(selectedEntity));
				put(new BoardEntity(pieceType), getRow(clickedEntity), getCol(clickedEntity));
			} else {
				put(getSelectedEntity(), getRow(clickedEntity), getCol(clickedEntity));
			}
			nextTurn();
			unselectEntity();
		} else
			for (BoardEntity.Type be_t : getCurrentTeam().getBoardEntityTypes())
				if (be_t == clickedEntity.getType())
					selectEntity(clickedEntity);

	}

	protected BoardEntity getSelectedEntity() {
		return selectedEntity;
	}

	protected void handleBoardClicked(MouseEvent event, int row, int col) {

		if (hasEntity(row, col))// Handled in #handleEntityClicked(...)
			return;
		else if (isPieceQueued()) {
			BoardEntity unqueuedPiece = unQueuePiece();
			put(unqueuedPiece, row, col);
			unqueuedPiece.selectTeam(getCurrentTeam());
		} else if (isEntitySelected()) {
			if (getSelectedEntity().getType().isTower()) {
				Type piece;
				switch (getSelectedEntity().getType()) {
				case RED_TOWER:
					piece = Type.RED_PIECE;
					break;
				case BLUE_TOWER:
					piece = Type.BLUE_PIECE;
					break;
				case WHITE_TOWER:
					piece = Type.WHITE_PIECE;
					break;
				// Should never happen
				default:
					piece = Type.UNCLAIMED_TOWER;
				}
				put(new BoardEntity(Type.UNCLAIMED_TOWER), getRow(getSelectedEntity()), getCol(getSelectedEntity()));
				put(new BoardEntity(piece), row, col);
			} else
				put(getSelectedEntity(), row, col);
			unselectEntity();
			nextTurn();
		}

	}

}
