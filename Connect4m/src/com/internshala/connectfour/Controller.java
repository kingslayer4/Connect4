package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int DIAM = 80;

	private static final String disc1 = "#8B0000";
	private static final String disc2 = "#0000FF";

	private boolean player1turn = true;
	private Disc[][] insertedDiscsArray = new Disc[ROWS][COLUMNS];

	private static String PLAYER_ONE = "Player 1";
	private static String PLAYER_TWO = "Player 2";
	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertedDiscsPane;

	@FXML
	public Label playerNameLabel;
	@FXML
	public Button setNamesButton;
	@FXML
	public TextField playerOneTextField;
	@FXML
	public TextField playerTwoTextField;


	private boolean isAllowed=true;

	public void createPlay() {
		Platform.runLater(() -> setNamesButton.requestFocus());
		Shape rectangleWithHoles = createGameStructuralGrid();

		rootGridPane.add(rectangleWithHoles, 0, 1);
		List<Rectangle> rectangleList = createclick();

		for (Rectangle rectangle : rectangleList) {
			rootGridPane.add(rectangle, 0, 1);
		}
		setNamesButton.setOnAction(event -> {
			PLAYER_ONE = playerOneTextField.getText();
			PLAYER_TWO = playerTwoTextField.getText();
			playerNameLabel.setText(player1turn? PLAYER_ONE : PLAYER_TWO);
		});
	}

	private Shape createGameStructuralGrid() {
		Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * DIAM, (ROWS + 1) * DIAM);
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				Circle circle = new Circle();
				circle.setRadius(DIAM / 2);
				circle.setCenterX(DIAM / 2);
				circle.setCenterY(DIAM / 2);
				circle.setSmooth(true);
				circle.setTranslateX(col * (DIAM + 5) + DIAM / 4);
				circle.setTranslateY(row * (DIAM + 5) + DIAM / 4);
				rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
			}
		}
		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}

	private List<Rectangle> createclick() {
		List<Rectangle> rectangleList = new ArrayList<>();
		for (int col = 0; col < COLUMNS; col++) {
			Rectangle rectangle = new Rectangle(DIAM, (ROWS + 1) * DIAM);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (DIAM + 5) + DIAM / 4);
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));
			final int column = col;
			rectangle.setOnMouseClicked(event -> {
				if (isAllowed){
					isAllowed=false;
					insertDisc(new Disc(player1turn), column);
				}
			});
			rectangleList.add(rectangle);
		}

		return rectangleList;
	}

	private void insertDisc(Disc disc, int column) {
		int row = ROWS - 1;
		while (row >= 0) {
			if (getDiscIfPresent(row, column) == null)
				break;

			row--;
		}
		if (row < 0)
			return;


		insertedDiscsArray[row][column] = disc;
		insertedDiscsPane.getChildren().add(disc);
		disc.setTranslateX(column * (DIAM + 5) + DIAM / 4);
		int currentRow = row;
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), disc);
		translateTransition.setToY(row * (DIAM + 5) + DIAM / 4);
		translateTransition.setOnFinished(event -> {
isAllowed=true;
			if (gameEnded(currentRow, column)) {
				gameOver();
				return;
			}

			player1turn = !player1turn;
			playerNameLabel.setText(player1turn ? PLAYER_ONE : PLAYER_TWO);
		});
		translateTransition.play();
	}

	private boolean gameEnded(int row, int column) {

		List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3, row + 3)
				.mapToObj(r -> new Point2D(r, column))
				.collect(Collectors.toList());

		List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3, column + 3)
				.mapToObj(col -> new Point2D(row, col))
				.collect(Collectors.toList());

Point2D startPoint1= new Point2D(row-3,column+3);
		List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startPoint2= new Point2D(row-3,column-3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints)
				|| checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);
		return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {
		int chain = 0;
		for (Point2D point : points) {

			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();

			Disc disc = getDiscIfPresent(rowIndexForArray,columnIndexForArray);
			if (disc != null && disc.player1move == player1turn) {
				chain++;
				if (chain == 4) {
					return true;
				}
			}
				else {
					chain = 0;
				}
			}
			return false;
		}
		private Disc getDiscIfPresent(int row, int column){
		if (row>=ROWS || row<0 || column>=COLUMNS || column<0)
			return null;
		return  insertedDiscsArray[row][column];

		}
	private  void gameOver(){
String winner = player1turn? PLAYER_ONE: PLAYER_TWO;
System.out.println("Winner is " + winner);
		Alert alert=new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is " + winner);
		alert.setContentText("Want to play again ? ");

		ButtonType yesBtn=new ButtonType("Yes");
		ButtonType noBtn=new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesBtn,noBtn);

Platform.runLater(() -> {
	Optional<ButtonType> btnClicked=alert.showAndWait();
	if (btnClicked.isPresent() && btnClicked.get()==yesBtn){
		resetGame();
	}
	else {
		Platform.exit();
		System.exit(0);
	}
});

}

	public void resetGame() {
		insertedDiscsPane.getChildren().clear();
		for (int row = 0; row <insertedDiscsArray.length ; row++) {
			for (int col=0;col<insertedDiscsArray[row].length;col++){
				insertedDiscsArray[row][col]=null;
			}
		}
player1turn=true;
		playerNameLabel.setText(PLAYER_ONE);
		createPlay();
	}


	private static class Disc extends Circle {
		private final boolean player1move;

		public Disc(boolean player1move) {
			this.player1move = player1move;
			setRadius(DIAM / 2);
			setFill(player1move ? Color.valueOf(disc1) : Color.valueOf(disc2));
			setCenterX(DIAM / 2);
			setCenterY(DIAM / 2);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
}