package screen;

import engine.Core;
import engine.GameSettings;
import engine.GameState;
import entity.Wallet;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Implements the Two player mode screen, where the action happens.
 */
public class TwoPlayerScreen extends Screen {
    /** Thread pool executor **/
    private final ExecutorService executor;
    /** List of game difficulty settings **/
    private List<GameSettings> gameSettings;
    /** Current game wallet **/
    private final Wallet wallet;

    /** Game states for each players **/
    private GameState gameStates[] = new GameState[2];

    /** Players game task **/
    private Future<GameState> players[] = new Future[2];

    /** Player game finished flags **/
    private boolean gameFinished[] = new boolean[2];

    /** Player 1's number**/
    private int PLAYER1_NUMBER = 0;
    /** Player 2's number**/
    private int PLAYER2_NUMBER = 1;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param gameSettings Current game settings.
     * @param width        Screen width.
     * @param height       Screen height.
     * @param fps          Frames per second, frame rate at which the game is run.
     */
    public TwoPlayerScreen(final GameState gameState, final List<GameSettings> gameSettings,
                           final int width, final int height, final int fps, Wallet wallet) {
        super(width * 2, height, fps * 2);

        this.gameSettings = gameSettings;
        this.gameStates[PLAYER1_NUMBER] = new GameState(gameState);
        this.gameStates[PLAYER2_NUMBER] = new GameState(gameState);
        this.wallet = wallet;
        executor = Executors.newFixedThreadPool(2);
        this.returnCode = 1;
        gameFinished[PLAYER1_NUMBER] = false;
        gameFinished[PLAYER2_NUMBER] = false;
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public int run(){
        try {
            runGameScreen(PLAYER1_NUMBER);
            runGameScreen(PLAYER2_NUMBER);
        }
        catch (Exception e) {
            // TODO handle exception
            e.printStackTrace();
        }
        super.run();
        return returnCode;
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);
        drawManager.mergeDrawing(this);
        drawManager.completeDrawing(this);
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        try {
            if (players[PLAYER1_NUMBER].isDone()) {
                gameStates[PLAYER1_NUMBER] = players[PLAYER1_NUMBER].get();
                gameStates[PLAYER1_NUMBER] = new GameState(gameStates[PLAYER1_NUMBER], gameStates[PLAYER1_NUMBER].getLevel() + 1);
                runGameScreen(PLAYER1_NUMBER);
            }
            if (players[PLAYER2_NUMBER].isDone()) {
                gameStates[PLAYER2_NUMBER] = players[PLAYER2_NUMBER].get();
                gameStates[PLAYER2_NUMBER] = new GameState(gameStates[PLAYER2_NUMBER], gameStates[PLAYER2_NUMBER].getLevel() + 1);
                runGameScreen(PLAYER2_NUMBER);
            }

            if (gameFinished[PLAYER1_NUMBER] && gameFinished[PLAYER2_NUMBER]) {
                isRunning = false;
                executor.shutdown();
            }

            draw();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Player 1's game progression logic.
     */
    private  void runGameScreen(int playerNumber){
        GameState gameState = playerNumber == 0 ? gameStates[PLAYER1_NUMBER] : gameStates[PLAYER2_NUMBER];

        if (gameState.getLivesRemaining() > 0) {
            if (gameState.getLevel() <= Core.NUM_LEVELS) {
                boolean bonusLife = gameState.getLevel()
                        % Core.EXTRA_LIFE_FRECUENCY == 0
                        && gameState.getLivesRemaining() < Core.MAX_LIVES;
                GameScreen gameScreen = new GameScreen(gameState, gameSettings.get(gameState.getLevel()-1),
                        bonusLife, width / 2, height, fps / 2, wallet, playerNumber);
                gameScreen.initialize();
                players[playerNumber] = executor.submit(gameScreen);
            }
        }
        else gameFinished[playerNumber] = true;
    }

    public GameState getWinnerGameState() {
        return gameStates[getWinnerNumber() - 1];
    }

    public int getWinnerNumber() {
        return ((gameStates[PLAYER1_NUMBER].getScore() >= gameStates[PLAYER2_NUMBER].getScore()) ? PLAYER1_NUMBER : PLAYER2_NUMBER) + 1;
    }
}
