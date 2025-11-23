package theater;

/**
 * Represents a play with a name and type (e.g., tragedy, comedy).
 * Both fields are non-null strings.
 */
public class Play {

    private String name;
    private String type;

    /**
     * Construct a Play with the given name and type.
     *
     * @param name the name of the play (non-null)
     * @param type the type of the play (non-null)
     */
    public Play(String name, String type) {
        this.setName(name);
        this.setType(type);
    }

    public String getName() {
        return name;
    }

    /**
     * Set the name of the play.
     *
     * @param name the name to set (non-null)
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    /**
     * Set the type of the play.
     *
     * @param type the type to set (non-null)
     */
    public void setType(String type) {
        this.type = type;
    }
}
