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

        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance performance : getInvoice().getPerformances()) {

            // add volume credits
            volumeCredits += Math.max(performance.getAudience() - BASE_VOLUME_CREDIT_THRESHOLD, 0);
            // add extra credit for every five comedy attendees
            if ("comedy".equals(getPlay(performance).getType())) {
                volumeCredits += performance.getAudience() / COMEDY_EXTRA_VOLUME_FACTOR;
            }

            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(performance).getName(),
                    frmt.format(getAmount(performance) / PERCENT_FACTOR),
                    performance.getAudience()));

            totalAmount += getAmount(performance);
        }
        result.append(String.format("Amount owed is %s%n", frmt.format(totalAmount / PERCENT_FACTOR)));
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
     * Helper method to calculate the base amount for a given performance and play.
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
