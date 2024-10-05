package screen;

import engine.Core;
import engine.GameSettings;
import engine.GameState;
import entity.EnemyShipFormation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Implements the game screen, where the action happens.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class TwoPlayerGameScreen extends Screen {
    private TmpScreen player1Screen;
    private TmpScreen player2Screen;
    private ExecutorService executor;

    /** Milliseconds until the screen accepts user input. */
    private static final int INPUT_DELAY = 6000;

    /** Current game difficulty settings. */
    private GameSettings gameSettings;
    /** Formation of enemy ships. */
    private EnemyShipFormation enemyShipFormation;


    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param gameState
     *            Current game state.
     * @param gameSettings
     *            Current game settings.
     * @param bonusLife
     *            Checks if a bonus life is awarded this level.
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     */
    public TwoPlayerGameScreen(final GameState gameState,
                               final GameSettings gameSettings, final boolean bonusLife,
                               final int width, final int height, final int fps) {
        super(width, height, fps);

        player1Screen = new TmpScreen(gameState, gameSettings, bonusLife, width, height, fps
        );
        player2Screen = new TmpScreen(gameState, gameSettings, bonusLife, width, height, fps
        );
        executor = Executors.newFixedThreadPool(2);

        this.gameSettings = gameSettings;
    }

    public void show() throws Exception {
        Future<Integer> player1Result = executor.submit(player1Screen);
        Future<Integer> player2Result = executor.submit(player2Screen);
    }

    /**
     * Initializes basic screen properties, and adds necessary elements.
     */
    public final void initialize() {
        super.initialize();
        player1Screen.initialize();
        player2Screen.initialize();

        enemyShipFormation = new EnemyShipFormation(this.gameSettings);
        enemyShipFormation.attach(this);
        // Special input delay / countdown.
        this.inputDelay = Core.getCooldown(INPUT_DELAY);
        this.inputDelay.reset();
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public int run(){
        try {
            show();
        } catch (Exception e){
            e.printStackTrace();
        }
        super.run();

        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {

        player1Screen.update();
        player2Screen.update();

        draw();
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {

    }
}