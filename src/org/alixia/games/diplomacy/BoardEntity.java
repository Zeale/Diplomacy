package org.alixia.games.diplomacy;

import org.alixia.games.diplomacy.Board.Team;

import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public final class BoardEntity {

	private static final DropShadow DEFAULT_SELECTION_EFFECT = new DropShadow(80, Color.GOLD);

	/**
	 * Used to determine if any arbitrarily obtained {@link ImageView} belongs to a
	 * {@link BoardEntity}.
	 */
	private static final Object BOARD_ENTITY_IMAGE_VIEW_KEY = new Object();

	public enum Type {
		RED_PIECE("pieces/red_piece.png"), BLUE_PIECE("pieces/blue_piece.png"), WHITE_PIECE(
				"pieces/white_piece.png"), RED_TOWER("towers/red_tower.png", true), BLUE_TOWER("towers/blue_tower.png",
						true), WHITE_TOWER("towers/white_tower.png",
								true), UNCLAIMED_TOWER("towers/unclaimed_tower.png", true);
		private Type(String subLoc) {
			this(subLoc, false);
		}

		private Type(String subLoc, boolean tower) {
			image = new Image("/org/alixia/games/diplomacy/_resources/graphics/" + subLoc);
			this.tower = tower;
		}

		private final boolean tower;

		private Type(Image image) {
			this.image = image;
			tower = false;
		}

		public boolean isTower() {
			return tower;
		}

		private final Image image;
	}

	public BoardEntity(Type type) {
		this.type = type;
		icon.setImage(type.image);
	}

	private final Type type;
	protected final ImageView icon = new ImageView();

	{
		icon.getProperties().put(BOARD_ENTITY_IMAGE_VIEW_KEY, this);
	}

	public static boolean isBoardEntityImageView(ImageView n) {
		return n.getProperties().containsKey(BOARD_ENTITY_IMAGE_VIEW_KEY);
	}

	public static BoardEntity getBoardEntity(Node n) throws ClassCastException {
		return (BoardEntity) n.getProperties().get(BOARD_ENTITY_IMAGE_VIEW_KEY);
	}

	public Type getType() {
		return type;
	}

	private Team team;
	private boolean selected;

	public void select() {
		selected = true;
		updateEffects();
	}

	void selectTeam(Team team) {
		this.team = team;
		updateEffects();
	}

	void deselectTeam() {
		team = null;
		updateEffects();
	}

	void deselect() {
		selected = false;
		updateEffects();
	}

	private void updateEffects() {
		if (selected)
			icon.setEffect(DEFAULT_SELECTION_EFFECT);
		else if (team != null)
			icon.setEffect(team.getSelectionEffect());
		else
			icon.setEffect(null);
	}

}
