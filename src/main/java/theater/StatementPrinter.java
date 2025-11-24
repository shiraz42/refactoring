package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

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

        final int totalAmount = getTotalAmount();
        final int volumeCredits = getTotalVolumeCredits();

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    /**
     * Helper method to look up the {@link Play} for a given {@link Performance}.
     *
     * @param performance the performance whose play is requested
     * @return the play associated with the given performance
     */
    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Helper method to calculate the base amount for a given performance.
     *
     * @param performance the performance whose amount should be calculated
     * @return the amount in cents for the given performance
     * @throws RuntimeException if the play type for the performance is unknown
     */
    private int getAmount(Performance performance) {
        final Play play = getPlay(performance);
        final String type = play.getType();
        final int audience = performance.getAudience();

        int result;
        switch (type) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (audience > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.HISTORY_OVER_BASE_CAPACITY_PER_PERSON
                            * (audience - Constants.BASE_VOLUME_CREDIT_THRESHOLD);
                }
                break;

            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (audience > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (audience - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * audience;
                break;

            default:
                throw new RuntimeException(String.format("unknown type: %s", type));
        }
        return result;
    }

    /**
     * Helper method to calculate the volume credits for a given performance.
     *
     * @param performance the performance for which to compute volume credits
     * @return the volume credits earned from the given performance
     */
    private int getVolumeCredits(Performance performance) {
        int result = 0;
        final int audience = performance.getAudience();

        // base volume credits
        result += Math.max(audience - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);

        // extra credit for every five comedy attendees
        if ("comedy".equals(getPlay(performance).getType())) {
            result += audience / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return result;
    }

    /**
     * Helper method to compute the total volume credits for the invoice.
     *
     * @return the total volume credits for all performances in the invoice
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
     *
     * @return the total amount, in cents, for all performances in the invoice
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
     *
     * @param amount the amount in cents
     * @return a string representing the amount in US dollars
     */
    private String usd(int amount) {
        final NumberFormat usdFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        return usdFormatter.format(amount / (double) Constants.PERCENT_FACTOR);
    }

    /**
     * Returns the invoice associated with this statement printer.
     *
     * @return the invoice used by this statement printer
     */
    public Invoice getInvoice() {
        return invoice;
    }

    /**
     * Sets the invoice associated with this statement printer.
     *
     * @param invoice the invoice to associate with this statement printer
     */
    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
