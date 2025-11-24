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
        final StringBuilder result = new StringBuilder("Statement for " + getInvoice().getCustomer()
                + System.lineSeparator());

        // Loop for building the lines of the statement (no totals, no credits)
        for (Performance performance : getInvoice().getPerformances()) {
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(performance).getName(),
                    usd(getAmount(performance)),
                    performance.getAudience()));
        }

        int totalAmount = getTotalAmount();
        int volumeCredits = getTotalVolumeCredits();

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

        // base volume credits
        result += Math.max(performance.getAudience() - BASE_VOLUME_CREDIT_THRESHOLD, 0);

        // extra credit for every five comedy attendees
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience() / COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return result;
    }

    /**
     * Helper method to compute the total volume credits for the invoice.
     */
    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance performance : getInvoice().getPerformances()) {
            result += getVolumeCredits(performance);
        }
        return result;
    }

    /**
     * Helper method to compute the total amount for the invoice.
     */
    private int getTotalAmount() {
        int result = 0;
        for (Performance performance : getInvoice().getPerformances()) {
            result += getAmount(performance);
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
