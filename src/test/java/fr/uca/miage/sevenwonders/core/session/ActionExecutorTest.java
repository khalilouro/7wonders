package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.core.Session;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.player.PlayerBoard;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.wonder.WonderStage;
import fr.uca.miage.sevenwonders.utils.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionExecutorTest {

    @Mock
    private Session session;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private Bank bank;
    @Mock
    private Bot mockBot;
    @Mock
    private Card mockCard;
    @Mock
    private Wonder mockWonder;
    @Mock
    private PlayerBoard mockBoard;

    private MockedStatic<Log> logMock;

    @InjectMocks
    private ActionExecutor actionExecutor;

    private List<Card> hand;

    @BeforeEach
    void setUp() {
        logMock = mockStatic(Log.class);

        hand = new ArrayList<>();
        hand.add(mockCard);

        lenient().when(session.getPlayers()).thenReturn(new Player[]{mockBot});
        lenient().when(session.getBank()).thenReturn(bank);
        lenient().when(mockBot.getHand()).thenReturn(hand);
        lenient().when(mockBot.getName()).thenReturn("TestBot");
        lenient().when(mockBot.getCard(anyInt())).thenReturn(mockCard);

        // Stub Board to prevent NPE during updateBoardElement
        lenient().when(mockBot.getBoard()).thenReturn(mockBoard);

        lenient().when(mockBot.getWonder()).thenReturn(mockWonder);
        lenient().when(mockCard.getName()).thenReturn("Mock Card");

        // FIX: Stub getColor() to return a valid enum to prevent NPE on toString()
        lenient().when(mockCard.getColor()).thenReturn(Card.Color.BLUE);
    }

    @AfterEach
    void tearDown() {
        logMock.close();
    }

    @Test
    @DisplayName("Action 0: Execute Discard - Should discard card and gain 3 coins")
    void testExecuteDiscard() {
        when(mockBot.applyStrategy(any(), any())).thenReturn(0);
        when(mockBot.discard(0)).thenReturn(mockCard);

        actionExecutor.executeTurn(0);

        verify(mockBot).discard(0);
        verify(session).addToDiscardPile(mockCard);
        verify(bank).withdraw(3, mockBot);
        logMock.verify(() -> Log.logTurn(any(), anyInt(), contains("discards")));
    }

    @Test
    @DisplayName("Action 1: Build Structure - Success")
    void testExecuteBuildStructure_Success() {
        when(mockBot.applyStrategy(any(), any())).thenReturn(10);

        // Mock Concrete Cost (Gold)
        Cost mockCost = mock(Cost.Gold.class);
        when(mockBot.canBuild(mockCard)).thenReturn(Optional.of(mockCost));

        // Mock Effect.VictoryPoints (Permitted Subclass) instead of sealed interface
        Effect.VictoryPoints mockEffect = mock(Effect.VictoryPoints.class);
        when(mockCard.getEffect()).thenReturn(mockEffect);

        actionExecutor.executeTurn(0);

        verify(transactionManager).payCost(mockCost, mockBot);
        verify(mockBot).removeCardFromHand(0);

        // This line previously caused NPE because getColor() was null
        verify(mockBoard).updateBoardElement(eq("BLUE"), eq(1));

        verify(mockBot).addAlreadyBuilt(mockCard);
        verify(mockEffect).apply(mockBot);
    }

    @Test
    @DisplayName("Action 1: Build Structure - Fail (Cannot Afford) -> Fallback to Discard")
    void testExecuteBuildStructure_Fail_CannotAfford() {
        when(mockBot.applyStrategy(any(), any())).thenReturn(10);
        when(mockBot.canBuild(mockCard)).thenReturn(Optional.empty());
        when(mockBot.discard(0)).thenReturn(mockCard);

        actionExecutor.executeTurn(0);

        verify(transactionManager, never()).payCost(any(), any());
        verify(mockBot, never()).addAlreadyBuilt(mockCard);
        verify(mockBot).discard(0);
        verify(bank).withdraw(3, mockBot);
    }

    @Test
    @DisplayName("Action 1: Build Structure - Fail (Transaction Exception) -> Fallback to Discard")
    void testExecuteBuildStructure_Fail_TransactionException() {
        when(mockBot.applyStrategy(any(), any())).thenReturn(10);

        Cost mockCost = mock(Cost.Gold.class);
        when(mockBot.canBuild(mockCard)).thenReturn(Optional.of(mockCost));

        doThrow(new IllegalStateException("Not enough funds")).when(transactionManager).payCost(mockCost, mockBot);
        when(mockBot.discard(0)).thenReturn(mockCard);

        actionExecutor.executeTurn(0);

        verify(mockBot).discard(0);
        logMock.verify(() -> Log.logTurn(any(), anyInt(), contains("Build failed")));
    }

    @Test
    @DisplayName("Action 2: Build Wonder - Success")
    void testExecuteBuildWonder_Success() {
        when(mockBot.applyStrategy(any(), any())).thenReturn(20);

        WonderStage mockStage = mock(WonderStage.class);
        Cost mockStageCost = mock(Cost.Gold.class);

        when(mockWonder.getCurrentStage()).thenReturn(mockStage);
        when(mockStage.getCosts()).thenReturn(mockStageCost);
        when(mockBot.canBuild(mockStageCost)).thenReturn(Optional.of(mockStageCost));

        actionExecutor.executeTurn(0);

        verify(transactionManager).payCost(mockStageCost, mockBot);
        verify(mockWonder).buildStage(mockBot);
        verify(mockBoard).updateBoardElement(eq("BUILT_WONDER_STAGES"), eq(1));
        verify(mockBot).removeCardFromHand(0);
    }

    @Test
    @DisplayName("Action 2: Build Wonder - Fail (No Stages Left) -> Fallback to Discard")
    void testExecuteBuildWonder_Fail_NoStages() {
        when(mockBot.applyStrategy(any(), any())).thenReturn(20);
        when(mockWonder.getCurrentStage()).thenReturn(null);
        when(mockBot.discard(0)).thenReturn(mockCard);

        actionExecutor.executeTurn(0);

        verify(mockWonder, never()).buildStage(any());
        verify(mockBot).discard(0);
    }

    @Test
    @DisplayName("Invalid Action Code -> Fallback to Discard")
    void testInvalidActionCode() {
        when(mockBot.applyStrategy(any(), any())).thenReturn(99);
        when(mockBot.getCard(0)).thenReturn(mockCard);
        when(mockBot.discard(0)).thenReturn(mockCard);

        actionExecutor.executeTurn(0);

        verify(mockBot).discard(0);
        logMock.verify(() -> Log.logTurn(any(), anyInt(), contains("Invalid action")));
    }
}
