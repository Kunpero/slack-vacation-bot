package rs.kunpero.vacation.util;

public enum ActionId {
    ADD_VACATION, SET_FROM, SET_SUBSTITUTION, SET_TO, OTHER;

    public static ActionId safeValueOf(String value) {
        try {
            return ActionId.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }
}
