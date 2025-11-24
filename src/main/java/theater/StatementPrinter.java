package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import static theater.Constants.*;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.setInvoice(invoice);
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result = new StringBuilder("Statement for " + getInvoice().getCustomer()
                + System.lineSeparator());

        for (Performance performance : getInvoice().getPerformances()) {

            // add volume credits
            volumeCredits += getVolumeCredits(performance);

            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(performance).getName(),
                    usd(getAmount(performance)),
                    performance.getAudience()));

            totalAmount += getAmount(performance);
        }

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));

        return result.toString();
    }

    /**
     * Helper method to look up the Play for a given Performance.
     */
    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Helper method to calculate the base amount for a given performance.
     */
    private int getAmount(Performance performance) {
        int result;
        final Play play = getPlay(performance);

        switch (play.getType()) {
            case "tragedy":
                result = TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += HISTORY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - BASE_VOLUME_CREDIT_THRESHOLD);
                }
                break;

            case "comedy":
                result = COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > COMEDY_AUDIENCE_THRESHOLD) {
                    result += COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - COMEDY_AUDIENCE_THRESHOLD));
                }
                result += COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;

            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }

    /**
     * Helper method to calculate the volume credits for a given performance.
     */
    private int getVolumeCredits(Performance performance) {
        int result = 0;

        result += Math.max(performance.getAudience() - BASE_VOLUME_CREDIT_THRESHOLD, 0);

        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience() / COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return result;
    }

    /**
     * Converts an amount in cents to a US currency string.
     */
    private String usd(int amount) {
        NumberFormat usdFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        return usdFormatter.format(amount / (double) PERCENT_FACTOR);
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
