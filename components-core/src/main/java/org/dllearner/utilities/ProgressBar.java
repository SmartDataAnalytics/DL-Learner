package org.dllearner.utilities;

/**
 * Ascii progress meter. On completion this will reset itself,
 * so it can be reused
 * <br /><br />
 * [================>                                 ]   33%
 *
 * From: http://www.avanderw.co.za/command-line-progress-bar/
 */
public class ProgressBar {
    int lastPercent;

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done an int representing the work done so far
     * @param total an int representing the total work
     */
    public void update(int done, int total) {
        int percent = (int) Math.round(done/(double)total * 100);
        if (Math.abs(percent - lastPercent) >= 1) {
            StringBuilder template = new StringBuilder("\r[");
            for (int i = 0; i < 50; i++) {
                if (i < percent * .5) {
                    template.append("=");
                } else if (i == percent * .5) {
                    template.append(">");
                } else {
                    template.append(" ");
                }
            }
            template.append("] %s   ");
            if (percent >= 100) {
                template.append("%n");
            }
            System.out.printf(template.toString(), percent + "%");
            lastPercent = percent;
        }
        if (done == total) {
            lastPercent = 0;
            System.out.flush();
            System.out.println();
        }
    }

}