package fr.uca.miage.sevenwonders.utils;

public class ProgressBar {
    private static final int TOTAL_WIDTH = 50;

    public static synchronized void update(int current, int total) {
        Config config = Config.getInstance();
        String accent = config.getValueANSI(config.getAccentColor());
        String secondary = config.getValueANSI(config.getSecondaryColor());
        String reset = config.getValueANSI("reset");

        double percent = (double) current / total;
        int progress = (int) (percent * TOTAL_WIDTH);

        StringBuilder bar = new StringBuilder();
        bar.append("\r").append(secondary).append(" ");

        bar.append(accent);
        for (int i = 0; i < TOTAL_WIDTH; i++) {
            if (i < progress) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }

        bar.append(secondary).append(" ");
        bar.append(String.format("%d%% (%d/%d)", (int) (percent * 100), current, total));
        bar.append(reset);

        System.out.print(bar.toString());

        if (current == total) {
            System.out.println();
        }
    }
}
