package fr.uca.miage.sevenwonders.ai;

import fr.uca.miage.sevenwonders.core.Session;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotTest {

    @Mock
    private Strategy strategy;

    @Mock
    private Bank bank;

    @Mock
    private Session session;

    @Mock
    private Card card1;

    @Mock
    private Card card2;

    private Bot bot;

    @BeforeEach
    void setUp() {
        bot = new Bot("TestBot", strategy);
    }

    @Test
    void testConstructor() {
        assertNotNull(bot);
        assertEquals("TestBot", bot.getName());
    }

    @Test
    void testApplyStrategyDelegatesToStrategy() {
        int expectedResult = 15;
        when(strategy.applyStrategy(bot, bank)).thenReturn(expectedResult);

        int result = bot.applyStrategy(bot, bank);

        assertEquals(expectedResult, result);
        verify(strategy, times(1)).applyStrategy(bot, bank);
    }

    @Test
    void testApplyStrategyThrowsIfStrategyIsNull() {
        bot.setStrategy(null);
        assertThrows(NullPointerException.class, () -> bot.applyStrategy(bot, bank));
    }

    @Test
    void testSetStrategy() {
        Strategy newStrategy = mock(Strategy.class);
        when(newStrategy.applyStrategy(bot, bank)).thenReturn(99);

        bot.setStrategy(newStrategy);
        int result = bot.applyStrategy(bot, bank);

        assertEquals(99, result);
        verify(newStrategy).applyStrategy(bot, bank);
        verify(strategy, never()).applyStrategy(any(), any());
    }

    @Test
    void testChooseAndPlayFromDiscard_NoAbility() {
        bot.chooseAndPlayFromDiscard(session, false);
        verifyNoInteractions(session);
    }

    @Test
    void testChooseAndPlayFromDiscard_EmptyPile() {
        bot.addPlayFromDiscard(1);
        when(session.getDiscardPile()).thenReturn(Collections.emptyList());

        bot.chooseAndPlayFromDiscard(session, false);

        assertTrue(bot.canPlayFromDiscard());
        verify(session, times(1)).getDiscardPile();
        verify(session, never()).removeFromDiscardPile(any());
    }

    @Test
    void testChooseAndPlayFromDiscard_PlaysBestVPCard() {
        // Arrange
        bot.addPlayFromDiscard(1);

        // Card 1: 2 VP (Low)
        // Note: We ONLY stub getEffect() because getName/getColor are never called for
        // the rejected card.
        Effect.VictoryPoints vpEffectLow = new Effect.VictoryPoints(2);
        when(card1.getEffect()).thenReturn(vpEffectLow);

        // Card 2: 5 VP (High) - This will be chosen
        Effect.VictoryPoints vpEffectHigh = new Effect.VictoryPoints(5);
        when(card2.getEffect()).thenReturn(vpEffectHigh);
        when(card2.getName()).thenReturn("CardHigh");
        when(card2.getColor()).thenReturn(Card.Color.RED);

        List<Card> discardPile = new ArrayList<>();
        discardPile.add(card1);
        discardPile.add(card2);

        when(session.getDiscardPile()).thenReturn(discardPile);

        // Act
        bot.chooseAndPlayFromDiscard(session, false);

        // Assert
        verify(session).removeFromDiscardPile(card2);
        verify(session, never()).removeFromDiscardPile(card1);

        assertFalse(bot.canPlayFromDiscard());
        assertEquals(5, bot.getTotalVictoryPoints());
        assertTrue(bot.getAlreadyBuilt().contains("CardHigh"));

        Map<String, Integer> board = bot.getBoardElement();
        assertEquals(1, board.get("RED"));
    }

    @Test
    void testChooseAndPlayFromDiscard_PlaysFirstIfNoVPCard() {
        // Arrange
        bot.addPlayFromDiscard(1);

        // Card 1: No VP, Green
        Effect.Science scienceEffect = new Effect.Science(Effect.Science.ScienceSymbol.TABLET);
        when(card1.getEffect()).thenReturn(scienceEffect);
        when(card1.getName()).thenReturn("CardScience");
        when(card1.getColor()).thenReturn(Card.Color.GREEN);

        List<Card> discardPile = new ArrayList<>();
        discardPile.add(card1);

        when(session.getDiscardPile()).thenReturn(discardPile);

        // Act
        bot.chooseAndPlayFromDiscard(session, false);

        // Assert
        verify(session).removeFromDiscardPile(card1);
        assertTrue(bot.getAlreadyBuilt().contains("CardScience"));
    }
}
