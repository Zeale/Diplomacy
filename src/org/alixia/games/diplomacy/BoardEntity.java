package org.alixia.games.diplomacy;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BoardEntity {

	/**
	 * Used to determine if any arbitrarily obtained {@link ImageView} belongs to a
	 * {@link BoardEntity}.
	 */
	private static final Object BOARD_ENTITY_IMAGE_VIEW_KEY = new Object();

	public enum Type {
		RED_PIECE("pieces/red_piece.png"), BLUE_PIECE("pieces/blue_piece.png"), WHITE_PIECE(
				"pieces/white_piece.png"), RED_TOWER("towers/red_tower.png"), BLUE_TOWER(
						"towers/blue_tower.png"), WHITE_TOWER(
								"towers/white_tower.png"), UNCLAIMED_TOWER("towers/unclaimed_tower.png");
		private Type(String subLoc) {
			image = new Image("/org/alixia/games/diplomacy/_resources/graphics/" + subLoc);
		}

		private Type(Image image) {
			this.image = image;
		}

		private final Image image;
	}

	public BoardEntity(Type type) {
		this(type.image);
	}

	protected BoardEntity(Image image) {
		icon.setImage(image);
	}

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

}
