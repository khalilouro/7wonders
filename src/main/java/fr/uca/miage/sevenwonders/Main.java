package fr.uca.miage.sevenwonders;

import fr.uca.miage.sevenwonders.core.GameEngine;
import fr.uca.miage.sevenwonders.io.ConsoleReporter;
import fr.uca.miage.sevenwonders.stats.GameResult;
import fr.uca.miage.sevenwonders.stats.StatisticsAnalyst;
import fr.uca.miage.sevenwonders.utils.Config;
import fr.uca.miage.sevenwonders.utils.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        String mode = (args.length > 0) ? args[0] : "une";

        GameEngine engine = new GameEngine();
        ConsoleReporter reporter = new ConsoleReporter();
        StatisticsAnalyst stats = new StatisticsAnalyst();

        switch (mode) {
            case "une" :
                Log.setSimulationMode(true);
                String main = Config.getInstance().getValueANSI(Config.getInstance().getMainColor());
                String reset = Config.getInstance().getValueANSI("reset");
                System.out.println(main + "Running single game detailed mode..." + reset);
                GameResult result = engine.runGame(1, true);
                reporter.printSingleGameSummary(result.getResults());
                break;

            case "plusieurs" :
                Log.setSimulationMode(false);
                Config config = Config.getInstance();
                String cMain = config.getValueANSI(config.getMainColor());
                String cAccent = config.getValueANSI(config.getAccentColor());
                String cReset = config.getValueANSI("reset");

                int gameCount = config.getGamesToPlay();
                int threadCount = config.getNumberOfThreads();
                System.out.println(cMain + "Simulating " + cAccent + gameCount + cMain + " games with " + cAccent
                        + threadCount + cMain + " threads..." + cReset);

                long startTime = System.currentTimeMillis();

                ExecutorService executor = Executors.newFixedThreadPool(threadCount);
                AtomicInteger completedGames = new AtomicInteger(0);

                for (int i = 0; i < gameCount; i++) {
                    final int gameId = i + 1;
                    executor.submit(() -> {
                        // Run game (false = quiet mode)
                        GameResult res = engine.runGame(gameId, false);
                        stats.recordGame(res);
                        fr.uca.miage.sevenwonders.utils.ProgressBar.update(completedGames.incrementAndGet(), gameCount);
                    });
                }

                executor.shutdown();
                try {
                    if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }

                long endTime = System.currentTimeMillis();
                System.out.println(
                        "\n" + cMain + "Simulation completed in " + cAccent + (endTime - startTime) + "ms." + cReset);

                reporter.printAggregateStats(stats);
                break;

            default :
                System.err.println("Unknown mode: " + mode);
                System.out.println("Usage: java Main [une|plusieurs]");
        }
    }
}
